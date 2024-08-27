package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class ClassDetailsActivity extends AppCompatActivity {
    // Declaración de variables para los TextView
    private TextView classNameTextView;
    private TextView membersCountTextView;
    private TextView adminIdTextView;
    private TextView classIdTextView;
    private Button showQRCodeButton;
    private Button editarClaseButton;


    //Declaracion de iniciar sesion de asistencia
    private Button startAttendanceButton;
    private Button stopAttendanceButton;
    private FirebaseFirestore db;
    private Handler handler = new Handler();
    private Runnable endSessionRunnable;
    private String barcodeAsistencia;
    private String tiempoServer;

    // Declaración del RecyclerView y el AlumnoAdapter
    private RecyclerView alumnosRecyclerView;
    private AlumnoAdapter alumnoAdapter;
    private List<AlumnoModel> alumnoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        // Inicialización de los TextView
        classNameTextView = findViewById(R.id.classNameTextView);
        membersCountTextView = findViewById(R.id.membersCountTextView);
        adminIdTextView = findViewById(R.id.adminIdTextView);
        //classIdTextView = findViewById(R.id.classIdTextView);
        //Botones
        showQRCodeButton = findViewById(R.id.showQRCodeButton);
        // Instancia de FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Inicialización del RecyclerView y el AlumnoAdapter
        alumnosRecyclerView = findViewById(R.id.alumnosRecyclerView);
        alumnoAdapter = new AlumnoAdapter(alumnoList);
        alumnosRecyclerView.setAdapter(alumnoAdapter);
        alumnosRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Obtención del modelo de clase desde el Intent
        Intent intent = getIntent();
        ClassModel classModel = (ClassModel) intent.getSerializableExtra("classModel");

        // Imprimir el valor del objeto ClassModel en el log
        Log.d("ClassDetailsActivity", "classModel: " + classModel.toString());
        // Imprimir solo ciertos campos del objeto ClassModel en el log
        Log.d("ClassDetailsActivity", "classId: " + classModel.getClassId());
        Log.d("ClassDetailsActivity", "className: " + classModel.getClassName());
        Log.d("ClassDetailsActivity", "membersCount: " + classModel.getMembersCount());
        Log.d("ClassDetailsActivity", "adminId: " + classModel.getAdminId());
        Log.d("ClassDetailsActivity", "Qr code: " + classModel.getQrCode());

        // Obtención del ID del usuario actual
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);

        // Obtención del documento del usuario desde la colección "Usuarios"
        DocumentReference userRef = db.collection("Usuarios").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Obtención del rol del usuario
                    String userRole = document.getString("EsDocente");
                    displayClassDetails(classModel, userRole, db);
                } else {
                    Log.d("ClassDetailsActivity", "No such document");
                }
            } else {
                Log.d("ClassDetailsActivity", "get failed with ", task.getException());
            }
        });

        //Obtener Codigo ID de la clase y mostrar el codigo.
        //String qrCode = classModel.getClassId();
        showQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrCode = classModel.getClassId(); // Obtener el código ID de la clase para convertirlo a QR
                showQRCode(classModel, qrCode);
            }
        });

        //iniciar session de asistencia
        startAttendanceButton = findViewById(R.id.startAttendanceButton);
        startAttendanceButton.setVisibility(View.GONE);
        startAttendanceButton.setOnClickListener(v -> startAttendanceSession(classModel));

        //Editar Clase
        editarClaseButton = findViewById(R.id.editarClaseButton);
        editarClaseButton.setVisibility(View.GONE);
        // Botón de editar clase
        editarClaseButton.setOnClickListener(v -> {
            // Crear un nuevo Intent para EditarClaseActivity
            Intent editarClaseIntent = new Intent(ClassDetailsActivity.this, EditarClaseActivity.class);

            // Pasar el classId al Intent
            editarClaseIntent.putExtra("classId", classModel.getClassId());

            // Iniciar la actividad EditarClaseActivity
            startActivity(editarClaseIntent);
        });
    }

    /**
     * Muestra los detalles de la clase según el rol del usuario
     */

    private boolean isAdmin(String userId, String classAdminId) {
        return userId != null && userId.equals(classAdminId);
    }

    private void displayClassDetails(ClassModel classModel, String userRole, FirebaseFirestore db) {
        Log.d("displayClassDetails", "EsDocente: " + userRole);
        if ("1".equals(userRole)) {
            // El usuario es profesor
            displayClassDetailsAsAdmin(classModel, db);
            startAttendanceButton.setVisibility(View.VISIBLE);
            editarClaseButton.setVisibility(View.VISIBLE);
            // Cargar la lista de alumnos
            loadAlumnos(classModel.getMembers(), classModel);
        } else {
            // El usuario es estudiante
            displayClassDetailsAsStudent(classModel, db);
            // Cargar la lista de alumnos
            loadAlumnos(classModel.getMembers(), classModel);
        }
    }

    /**
     * Muestra los detalles de la clase como profesor
     */
    private void displayClassDetailsAsAdmin(ClassModel classModel, FirebaseFirestore db) {
        // Obtención del documento de la clase desde la colección "Clases"
        db.collection("Clases")
                .document(classModel.getClassId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            ClassModel classDetails = document.toObject(ClassModel.class);
                            classDetails.setClassId(classModel.getClassId()); // Populate the classId field
                            displayClassDetails(classDetails);

                            String adminId = classDetails.getAdminId();
                            if (adminId != null) {
                                fetchAdminName(adminId, db);
                                // Verificar si el usuario actual es el admin
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (isAdmin(user.getUid(), adminId)) {
                                    // Mostrar el botón para iniciar la sesión de asistencia
                                    startAttendanceButton = findViewById(R.id.startAttendanceButton);
                                    startAttendanceButton.setVisibility(View.VISIBLE);
                                } else {
                                    // Ocultar el botón si el usuario no es el admin
                                    startAttendanceButton.setVisibility(View.GONE);
                                }
                            } else {
                                Log.d("ClassDetailsActivity", "Admin ID is null");
                            }
                        } else {
                            Log.d("ClassDetailsActivity", "No such document");
                        }
                    } else {
                        Log.d("ClassDetailsActivity", "get failed with ", task.getException());
                    }
                });
    }

    /**
     * Muestra los detalles de la clase como estudiante
     */
    private void displayClassDetailsAsStudent(ClassModel classModel, FirebaseFirestore db) {
        // Obtención del documento de la clase desde la colección "Clases"
        db.collection("Clases")
                .document(classModel.getClassId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            List<String> members = (List<String>) document.get("members");
                            if (members.contains(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                String adminId = document.getString("admin"); // Obtener el adminId del documento de la clase
                                ClassModel classDetails = document.toObject(ClassModel.class);
                                classDetails.setClassId(classModel.getClassId()); // Populate the classId field
                                classDetails.setAdminId(adminId); // Populate the adminId field
                                displayClassDetails(classDetails);
                                if (adminId != null) {
                                    fetchAdminName(adminId, db);
                                } else {
                                    Log.d("ClassDetailsActivity", "Admin ID is null");
                                }
                            } else {
                                Log.d("ClassDetailsActivity", "User is not a member of this class");
                            }
                        } else {
                            Log.d("ClassDetailsActivity", "No such document");
                        }
                    } else {
                        Log.d("ClassDetailsActivity", "get failed with ", task.getException());
                    }
                });
    }

    /**
     * Muestra los detalles de la clase
     */
    private void displayClassDetails(ClassModel classModel) {
        classNameTextView.setText(classModel.getClassName());
        membersCountTextView.setText("Numero de alumnos: " + classModel.getMembersCount());
        //classIdTextView.setText("Class ID: " + classModel.getClassId());
    }

    /**
     * Obtiene el nombre del administrador de la clase
     */
    private void fetchAdminName(String adminId, FirebaseFirestore db) {
        // Obtención del documento del administrador desde la colección "Usuarios"
        db.collection("Usuarios")
                .document(adminId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot adminDoc = task.getResult();
                        if (adminDoc.exists()) {
                            String adminName = adminDoc.getString("Nombre");
                            adminIdTextView.setText("Profesor: " + adminName); //+ " (ID: " + adminId + ")"
                        } else {
                            Log.d("ClassDetailsActivity", "No admin found with ID " + adminId);
                        }
                    } else {
                        Log.d("ClassDetailsActivity", "get failed with ", task.getException());
                    }
                });
    }


    private void showQRCode(ClassModel classModel, String qrCode) {
        showQRCodeDialog(classModel, qrCode);
    }

    private void startAttendanceSession(ClassModel classModel) {
        // Generar un código de barras único para la sesión que contiene el classId concatenado con una cadena random
        barcodeAsistencia = UUID.randomUUID().toString().substring(0, 8);

        // Crear un nuevo documento en la colección "Sesion_Asistencia"
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("classId", classModel.getClassId());
        sessionData.put("startTime", FieldValue.serverTimestamp());
        sessionData.put("status", "active");
        sessionData.put("barcode", barcodeAsistencia);

        db.collection("Sesion_Asistencia").document(barcodeAsistencia).set(sessionData)
                .addOnSuccessListener(aVoid -> {
                    // Mostrar el código de barras en un diálogo
                    showBarcodeDialog(barcodeAsistencia);

                    // Iniciar el temporizador para detener la sesión después de 15 minutos
                    endSessionRunnable = this::stopAttendanceSession;
                    handler.postDelayed(endSessionRunnable, 15 * 60 * 1000); // 15 minutos
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClassDetailsActivity.this, "Error al iniciar la sesión de asistencia", Toast.LENGTH_SHORT).show();
                });
    }

    private void stopAttendanceSession() {
        if (barcodeAsistencia == null) return;

        // Actualizar el documento de la sesión de asistencia para marcarlo como inactivo
        db.collection("Sesion_Asistencia").document(barcodeAsistencia)
                .update("endTime", FieldValue.serverTimestamp(), "status", "inactive")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ClassDetailsActivity.this, "Sesión de asistencia detenida", Toast.LENGTH_SHORT).show();

                    // Cancelar el temporizador si la sesión se detuvo manualmente
                    handler.removeCallbacks(endSessionRunnable);
                    barcodeAsistencia = null; // Reiniciar el sessionId para la próxima sesión
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ClassDetailsActivity.this, "Error al detener la sesión de asistencia", Toast.LENGTH_SHORT).show();
                });
    }

    private void showBarcodeDialog(String barcodeAsistencia) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_asistencia_barcode);

        // Generar el código de barras con la cadena sessionId
        Bitmap bitmap = generateBarcode(barcodeAsistencia);
        ImageView qrCodeImage = dialog.findViewById(R.id.barcode_image);
        qrCodeImage.setImageBitmap(bitmap);

        dialog.show();

        // Botón de descarga del código de barras
        Button stopAttendanceButton = dialog.findViewById(R.id.stop_session_button);
        Button downloadButton = dialog.findViewById(R.id.download_button);
        stopAttendanceButton.setOnClickListener(v -> stopAttendanceSession());
        downloadButton.setOnClickListener(v -> downloadBarcode(bitmap));
    }


    private void showQRCodeDialog(ClassModel classModel, String qrCode) {
        // Crear el dialog box
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_code);  // Asegúrate de que dialog_qr_code es el layout correcto

        // Generar el código QR con la cadena de texto classModel.getQrCode()
        String qrCodeText = classModel.getClassId();
        Bitmap bitmap = generateQRCode(qrCodeText);

        // Obtener la imagen del código QR
        ImageView qrCodeImage = dialog.findViewById(R.id.qr_code_image);
        if (qrCodeImage != null) {
            qrCodeImage.setImageBitmap(bitmap);
        } else {
            Log.e("QRCodeDialog", "ImageView qr_code_image not found in layout");
        }

        // Mostrar el dialog box
        dialog.show();

        // Agregar listener al botón de descargar
        Button downloadButton = dialog.findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Descargar la imagen del código QR
                downloadQRCode(bitmap);
            }
        });
    }

    private Bitmap generateBarcode(String barcodeText) {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(barcodeText, BarcodeFormat.CODE_128, 1200, 600);
        } catch (WriterException e) {
            Log.e("BarcodeGeneration", "Error generating barcode", e);
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }


    private Bitmap generateQRCode(String qrCodeText) {
        // Utilizar la biblioteca ZXing para generar el código QR
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(qrCodeText, BarcodeFormat.QR_CODE, 600, 600);
        } catch (WriterException e) {
            // Handle the exception, for example, by logging an error message
            Log.e("QRCodeGeneration", "Error generating QR code", e);
            return null; // or some default bitmap
        }
        Bitmap bitmap = Bitmap.createBitmap(matrix.getWidth(), matrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < matrix.getWidth(); x++) {
            for (int y = 0; y < matrix.getHeight(); y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    private void downloadQRCode(Bitmap bitmap) {
        // Crear un archivo para la imagen del código QR
        File file = new File(getExternalCacheDir(), "qr_code.png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Código QR descargado con éxito", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Error", "Error al descargar el código QR", e);
        }
    }

    private void downloadBarcode(Bitmap bitmap) {
        File file = new File(getExternalCacheDir(), "bar_code.png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Código de barras descargado con éxito", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Error", "Error al descargar el código de barras", e);
        }
    }

    private void loadAlumnos(List<Object> memberIds, ClassModel classModel) {
        for (Object memberId : memberIds) {
            db.collection("Usuarios").document((String) memberId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nombre = document.getString("Nombre");
                        // Obtener el contador de asistencia para este alumno
                        db.collection("Control_Asistencia")
                                .whereEqualTo("userId", memberId)
                                .whereEqualTo("classId", classModel.getClassId())
                                .get()
                                .addOnCompleteListener(assistTask -> {
                                    if (assistTask.isSuccessful()) {
                                        int asistencias = 0;
                                        if (!assistTask.getResult().isEmpty()) {
                                            DocumentSnapshot assistDoc = assistTask.getResult().getDocuments().get(0);
                                            asistencias = assistDoc.getLong("asistencias").intValue();
                                        }

                                        AlumnoModel alumno = new AlumnoModel(nombre, asistencias);
                                        alumnoList.add(alumno);  // Agrega el alumno con el contador de asistencias
                                        alumnoAdapter.notifyDataSetChanged();  // Notifica al adaptador de los cambios
                                    } else {
                                        Log.d("ClassDetailsActivity", "Error fetching asistencia data", assistTask.getException());
                                    }
                                });
                    } else {
                        Log.d("ClassDetailsActivity", "No such document");
                    }
                } else {
                    Log.d("ClassDetailsActivity", "get failed with ", task.getException());
                }
            });
        }
    }
}