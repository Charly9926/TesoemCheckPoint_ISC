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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        textViewResult = findViewById(R.id.qr_result_text);
        surfaceView = findViewById(R.id.qr_surface_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
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
                if (barcodes.size()!= 0) {
                    if (!isCooldownActive) {
                        isCooldownActive = true;
                        Barcode barcode = barcodes.valueAt(0);
                        if (barcode!= null) {
                            String qrCodeText = barcode.rawValue;
                            Log.d("QRCode", "Código QR: " + qrCodeText);
                            getQRCodeFromFirestore(qrCodeText);
                            qrCodeRead = true; // Indicar que se ha leído un código QR

                            // Verificar y registrar la asistencia
                            verifyAndRegisterAttendance(qrCodeText);

                            // Iniciar cooldown de 5 segundos
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isCooldownActive = false;
                                }
                            }, 5000); // 5000 milisegundos = 5 segundos
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
                    //insertDataIntoFirestore(mAuth.getCurrentUser().getUid(), classId); // Pasar el classId en lugar de qrCode
                    // Agregar usuario a la clase y insertar en subcoleccion con contador de asistencia en 0
                    addUserToClassAndInitializeAttendance(mAuth.getCurrentUser().getUid(), classId);
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

    // Nuevo codigo para insertar usuario
    private void addUserToClassAndInitializeAttendance(String userUid, String classId) {
        // Obtener la referencia al documento de la clase
        DocumentReference classRef = firestore.collection("Clases").document(classId);

        // Agregar el usuario a la clase y crear/actualizar el contador de asistencia
        classRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    Map<String, Object> data = documentSnapshot.getData();
                    if (data.containsKey("members")) {
                        ArrayList<String> members = (ArrayList<String>) data.get("members");
                        if (members.contains(userUid)) {
                            // El usuario ya es parte de la clase, sólo actualizamos el contador de asistencia
                            Toast.makeText(QRScannerActivity.this, "El usuario ya es parte de la clase", Toast.LENGTH_SHORT).show();
                        } else {
                            // Agregar el usuario a la lista de miembros y actualizar Firestore
                            members.add(userUid);
                            data.put("members", members);
                            classRef.update(data).addOnCompleteListener(taskUpdate -> {
                                if (taskUpdate.isSuccessful()) {
                                    Toast.makeText(QRScannerActivity.this, "Usuario agregado a la clase", Toast.LENGTH_SHORT).show();
                                    initializeAttendanceCount(classRef, userUid);
                                } else {
                                    Toast.makeText(QRScannerActivity.this, "Error al agregar usuario a la clase", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        // No hay miembros, inicializamos la lista de miembros
                        ArrayList<String> members = new ArrayList<>();
                        members.add(userUid);
                        data.put("members", members);
                        classRef.update(data).addOnCompleteListener(taskUpdate -> {
                            if (taskUpdate.isSuccessful()) {
                                Toast.makeText(QRScannerActivity.this, "Usuario agregado a la clase", Toast.LENGTH_SHORT).show();
                                initializeAttendanceCount(classRef, userUid);
                            } else {
                                Toast.makeText(QRScannerActivity.this, "Error al agregar usuario a la clase", Toast.LENGTH_SHORT).show();
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

    private void initializeAttendanceCount(DocumentReference classRef, String userUid) {
        // Crear o actualizar el documento en la subcolección "AttendanceCounts" con el contador inicial
        CollectionReference attendanceCountsRef = classRef.collection("AttendanceCounts");
        DocumentReference userAttendanceRef = attendanceCountsRef.document(userUid);

        userAttendanceRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (!documentSnapshot.exists()) {
                    // El documento no existe, crear un nuevo documento con el contador inicial
                    Map<String, Object> initialData = new HashMap<>();
                    initialData.put("count", 0);  // Inicializamos el contador en 0
                    userAttendanceRef.set(initialData).addOnCompleteListener(taskSet -> {
                        if (taskSet.isSuccessful()) {
                            Log.d("Firestore", "Contador de asistencia inicializado para el usuario: " + userUid);
                        } else {
                            Log.e("Firestore", "Error al inicializar el contador de asistencia", taskSet.getException());
                        }
                    });
                }
            } else {
                Log.e("Firestore", "Error al verificar el documento de asistencia del usuario", task.getException());
            }
        });
    }

    private void verifyAndRegisterAttendance(String sessionId) {
        String userId = mAuth.getCurrentUser().getUid();

        // Buscar la sesión de asistencia en Firestore
        firestore.collection("Sesion_Asistencia").document(sessionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && "active".equals(documentSnapshot.getString("status"))) {
                        String classId = documentSnapshot.getString("classId");
                        if (classId != null) {
                            // Registrar la asistencia
                            registerAttendance(sessionId, userId, classId);
                        }
                    } else {
                        showToast("Sesión de asistencia no válida o ya cerrada");
                        qrCodeRead = false;
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Error al verificar la sesión de asistencia");
                    qrCodeRead = false;
                });
    }

    private void registerAttendance(String sessionId, String userId, String classId) {
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("sessionId", sessionId);
        attendanceData.put("studentId", userId);
        attendanceData.put("timestamp", FieldValue.serverTimestamp());

        // Verificar si el usuario ya ha registrado su asistencia para esta sesión
        firestore.collection("Asistencias")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("studentId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No se ha registrado asistencia antes, registrar ahora
                            firestore.collection("Asistencias").add(attendanceData)
                                    .addOnSuccessListener(documentReference -> {
                                        showToast("Asistencia registrada con éxito");
                                        qrCodeRead = false;
                                        // Actualizar el contador de asistencia
                                        updateAttendanceCount(classId, userId);
                                        // Activar un cooldown para evitar lecturas repetidas
                                        activateCooldown();
                                    })
                                    .addOnFailureListener(e -> {
                                        showToast("Error al registrar la asistencia");
                                        qrCodeRead = false;
                                    });
                        } else {
                            showToast("Ya has registrado tu asistencia para esta sesión");
                            qrCodeRead = false;
                        }
                    } else {
                        showToast("Error al verificar la asistencia");
                        qrCodeRead = false;
                    }
                });
    }

    private void updateAttendanceCount(String classId, String userId) {
        DocumentReference attendanceCountRef = firestore.collection("Clases")
                .document(classId)
                .collection("AttendanceCounts")
                .document(userId);

        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(attendanceCountRef);
            long newCount = 1;
            if (snapshot.exists()) {
                Long currentCount = snapshot.getLong("count");
                if (currentCount != null) {
                    newCount = currentCount + 1;
                }
            }
            transaction.set(attendanceCountRef, Collections.singletonMap("count", newCount), SetOptions.merge());
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("QRScannerActivity", "Contador de asistencia actualizado correctamente");
        }).addOnFailureListener(e -> {
            Log.e("QRScannerActivity", "Error al actualizar el contador de asistencia", e);
        });
    }

    private void activateCooldown() {
        isCooldownActive = true;
        handler.postDelayed(() -> isCooldownActive = false, 5000); // Cooldown de 5 segundos
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(QRScannerActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void handleError() {
        // Maneja el error aquí
        Log.e("Error", "Error al obtener la clase");
        textViewResult.setText("Error al obtener la clase");
    }
}