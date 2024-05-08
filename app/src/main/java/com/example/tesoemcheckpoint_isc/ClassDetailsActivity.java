package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ClassDetailsActivity extends AppCompatActivity {
    // Declaración de variables para los TextView
    private TextView classNameTextView;
    private TextView membersCountTextView;
    private TextView adminIdTextView;
    private TextView classIdTextView;
    private Button showQRCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        // Inicialización de los TextView
        classNameTextView = findViewById(R.id.classNameTextView);
        membersCountTextView = findViewById(R.id.membersCountTextView);
        adminIdTextView = findViewById(R.id.adminIdTextView);
        classIdTextView = findViewById(R.id.classIdTextView);
        //Botones
        showQRCodeButton = findViewById(R.id.showQRCodeButton);

        // Instancia de FirebaseFirestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        //Obtener Codigo QR de la clase y mostrar el codigo.
        String qrCode = classModel.getQrCode();
        showQRCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrCode = classModel.getQrCode(); // Obtener el código QR de la clase
                showQRCode(qrCode);
            }
        });
    }

    /**
     * Muestra los detalles de la clase según el rol del usuario
     */
    private void displayClassDetails(ClassModel classModel, String userRole, FirebaseFirestore db) {
        if ("1".equals(userRole)) {
            // El usuario es profesor
            displayClassDetailsAsAdmin(classModel, db);
        } else {
            // El usuario es estudiante
            displayClassDetailsAsStudent(classModel, db);
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
                            if (adminId!= null) {
                                fetchAdminName(adminId, db);
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
                                if (adminId!= null) {
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
        membersCountTextView.setText("Members Count: " + classModel.getMembersCount());
        classIdTextView.setText("Class ID: " + classModel.getClassId());
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
                            adminIdTextView.setText("Profesor: " + adminName + " (ID: " + adminId + ")");
                        } else {
                            Log.d("ClassDetailsActivity", "No admin found with ID " + adminId);
                        }
                    } else {
                        Log.d("ClassDetailsActivity", "get failed with ", task.getException());
                    }
                });
    }


    private void showQRCode(String qrCode) {
        showQRCodeDialog(qrCode);
    }

    private void showQRCodeDialog(String qrCode) {
        // Crear el dialog box
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_qr_code);

        // Obtener la imagen del código QR
        ImageView qrCodeImage = dialog.findViewById(R.id.qr_code_image);
        Bitmap bitmap = decodeBase64ToBitmap(qrCode);
        qrCodeImage.setImageBitmap(bitmap);

        // Agregar listener al botón de descargar
        Button downloadButton = dialog.findViewById(R.id.download_button);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Descargar la imagen del código QR
                downloadQRCode(bitmap);
            }
        });

        // Mostrar el dialog box
        dialog.show();
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

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}