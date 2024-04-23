package com.example.tesoemcheckpoint_isc;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.HashMap;
import java.util.Map;

public class QRScannerActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private TextView textViewResult;
    private Button qrScanButton;
    private CameraPreview cameraPreview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        textViewResult = findViewById(R.id.qr_result_text);
        qrScanButton = findViewById(R.id.qr_scan_button);
        cameraPreview = findViewById(R.id.qr_surface_view);

        qrScanButton.setOnClickListener(v -> {
            // Inicia el escáner de QR
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Resultado del escáner de QR
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result!= null) {
            if (result.getContents()!= null) {
                String qrCode = result.getContents();

                // Obtiene el usuario de Firebase y el nombre de la clase desde Firestore
                String currentUserUid = mAuth.getCurrentUser().getUid();
                String className = getClassNameFromFirestore(currentUserUid, qrCode);

                // Inserta la información en Firestore
                if (className!= null) {
                    insertDataIntoFirestore(currentUserUid, className);
                    textViewResult.setText("Datos insertados correctamente");
                } else {
                    textViewResult.setText("Error al obtener la clase");
                }
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private String getClassNameFromFirestore(String userUid, String qrCode) {
        // Ejemplo de implementación para obtener el nombre de la clase desde Firestore
        try {
            DocumentReference classRef = firestore.collection("Clases").document(qrCode);
            // Supongamos que tienes un campo "className" en el documento de la clase
            return classRef.get().getResult().getString("className");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void insertDataIntoFirestore(String userUid, String className) {
        // Ejemplo de implementación para insertar datos en Firestore
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("memberUid", userUid);
            data.put("joinedOn", System.currentTimeMillis()); // Timestamp actual

            firestore.collection("Clases").document(className)
                    .collection("members").document(userUid)
                    .set(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}