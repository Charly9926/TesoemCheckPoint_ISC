package com.example.tesoemcheckpoint_isc;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QRScannerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView textViewResult;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private boolean qrCodeRead = false;
    private boolean isCooldownActive = false;
    private Handler handler = new Handler();
    private String lastScannedBarcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        textViewResult = findViewById(R.id.qr_result_text);
        surfaceView = findViewById(R.id.qr_surface_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS) //el programa puede leer ya sea codigos QR o de BARRA
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1280, 720)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(30.0f)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    if (!isCooldownActive) {
                        isCooldownActive = true;
                        Barcode barcode = barcodes.valueAt(0);
                        if (barcode != null) {
                            // Verificar si el código de barras ya ha sido escaneado
                            if (!barcode.rawValue.equals(lastScannedBarcode)) {
                                lastScannedBarcode = barcode.rawValue;
                                if (barcode.format == Barcode.QR_CODE) {
                                    String qrCodeText = barcode.rawValue;
                                    Log.d("QRCode", "Código QR: " + qrCodeText);
                                    getQRCodeFromFirestore(qrCodeText);
                                } else if (barcode.format == Barcode.CODE_128) {
                                    String barcodeText = barcode.rawValue;
                                    Log.d("Barcode", "Código de barras: " + barcodeText);
                                    runOnUiThread(() ->
                                            Toast.makeText(QRScannerActivity.this, "Código de barras escaneado: " + barcodeText, Toast.LENGTH_SHORT).show()
                                    );
                                    insertarAsistencia(barcodeText);
                                }
                            } else {
                                Log.d("Barcode", "Código de barras repetido: " + barcode.rawValue);
                            }
                            // Iniciar cooldown de 5 segundos
                            handler.postDelayed(() -> {
                                isCooldownActive = false;
                            }, 5000);
                        }
                    }
                }
            }
        });
    }

    private void getQRCodeFromFirestore(String qrCode) {
        CollectionReference classesRef = firestore.collection("Clases");
        Query query = classesRef.whereEqualTo(FieldPath.documentId(), qrCode);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot!= null &&!querySnapshot.isEmpty()) {
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    ClassModel classModel = documentSnapshot.toObject(ClassModel.class);
                    String className = classModel.getClassName();
                    String classId = documentSnapshot.getId();
                    Log.d("Firestore", "Clase encontrada: " + className + " con ID: " + classId);
                    Log.d("Firestore", "Código QR: " + qrCode);
                    Log.d("Firestore", "User ID: " + mAuth.getCurrentUser().getUid());
                    insertDataIntoFirestore(mAuth.getCurrentUser().getUid(), classId); // Pasar el classId en lugar de qrCode
                } else {
                    Log.e("Error", "No se encontró una clase asociada al código QR");
                    textViewResult.setText("No se encontró una clase asociada al código QR");
                }
            } else {
                Log.e("Error", "Error al obtener la clase");
                textViewResult.setText("Error al obtener la clase");
            }
        });
    }

    private void insertDataIntoFirestore(String userUid, String classId) {
        // Obtener la referencia a la clase asociada al código QR
        CollectionReference classesRef = firestore.collection("Clases");
        DocumentReference classRef = classesRef.document(classId); // Utilizar el classId correcto

        // Verificar si el usuario ya es parte de la clase
        classRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data.containsKey("members")) {
                        ArrayList<String> members = (ArrayList<String>) data.get("members");
                        if (members.contains(userUid)) {
                            // El usuario ya es parte de la clase
                            Toast.makeText(QRScannerActivity.this, "El usuario ya es parte de la clase", Toast.LENGTH_SHORT).show();
                        } else {
                            // Agregar el usuario a la clase
                            members.add(userUid);
                            data.put("members", members);
                            classRef.update(data).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    Toast.makeText(QRScannerActivity.this, "Datos insertados correctamente", Toast.LENGTH_SHORT).show();
                                    qrCodeRead = false; // Resetear la bandera para que se pueda leer otro código QR
                                    finish(); // Regresa a la pantalla anterior
                                } else {
                                    Toast.makeText(QRScannerActivity.this, "Error al insertar datos", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // La clase puede agregar el usuario por que no se encunetra en la lista
                        ArrayList<String> members = new ArrayList<>();
                        members.add(userUid);
                        data.put("members", members);
                        classRef.update(data).addOnCompleteListener(taskUpdate -> {
                            if (taskUpdate.isSuccessful()) {
                                Toast.makeText(QRScannerActivity.this, "Datos insertados correctamente", Toast.LENGTH_SHORT).show();
                                qrCodeRead = false; // Resetear la bandera para que se pueda leer otro código QR
                                finish(); // Regresa a la pantalla anterior
                            } else {
                                Toast.makeText(QRScannerActivity.this, "Error al insertar datos", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(QRScannerActivity.this, "No se encontró la clase", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(QRScannerActivity.this, "Error al obtener la clase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertarAsistencia(String barcodeText) {
        // Obtener el classId a partir del barcodeText en la colección "Sesion_Asistencia"
        CollectionReference sesionAsistenciaRef = firestore.collection("Sesion_Asistencia");
        Query query = sesionAsistenciaRef.whereEqualTo(FieldPath.documentId(), barcodeText);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    String classIdAsistencia = documentSnapshot.getString("classId");

                    if (classIdAsistencia != null) {
                        // Verificar si ya existe un documento en "Control_Asistencia" con el mismo classId y userId
                        CollectionReference controlAsistenciaRef = firestore.collection("Control_Asistencia");
                        Query asistenciaQuery = controlAsistenciaRef
                                .whereEqualTo("classId", classIdAsistencia)
                                .whereEqualTo("userId", mAuth.getCurrentUser().getUid());

                        asistenciaQuery.get().addOnCompleteListener(asistenciaTask -> {
                            if (asistenciaTask.isSuccessful()) {
                                QuerySnapshot asistenciaSnapshot = asistenciaTask.getResult();
                                if (asistenciaSnapshot != null && !asistenciaSnapshot.isEmpty()) {
                                    // Documento encontrado, incrementar el contador de asistencias
                                    DocumentSnapshot asistenciaDoc = asistenciaSnapshot.getDocuments().get(0);
                                    String asistenciaDocId = asistenciaDoc.getId();
                                    int currentAsistencias = asistenciaDoc.getLong("asistencias").intValue();
                                    controlAsistenciaRef.document(asistenciaDocId)
                                            .update("asistencias", currentAsistencias + 1)
                                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "Asistencia actualizada"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error al actualizar asistencia", e));
                                } else {
                                    // No existe documento, crear uno nuevo
                                    Map<String, Object> asistenciaData = new HashMap<>();
                                    asistenciaData.put("classId", classIdAsistencia);
                                    asistenciaData.put("userId", mAuth.getCurrentUser().getUid());
                                    asistenciaData.put("asistencias", 1);

                                    controlAsistenciaRef.add(asistenciaData)
                                            .addOnSuccessListener(documentReference -> Log.d("Firestore", "Asistencia registrada"))
                                            .addOnFailureListener(e -> Log.e("Firestore", "Error al registrar asistencia", e));
                                }
                            } else {
                                Log.e("Firestore", "Error al verificar asistencia", asistenciaTask.getException());
                            }
                        });
                    } else {
                        Log.e("Firestore", "classId no encontrado en el documento de Sesion_Asistencia");
                    }
                } else {
                    Log.e("Firestore", "No se encontró un documento en Sesion_Asistencia para el código de barras");
                }
            } else {
                Log.e("Firestore", "Error al obtener el documento de Sesion_Asistencia", task.getException());
            }
        });
    }


}