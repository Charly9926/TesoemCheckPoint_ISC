package com.example.tesoemcheckpoint_isc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.ZXingScannerView;

import java.util.HashMap;
import java.util.Map;

public class QRScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    private ZXingScannerView barcodeView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String classId;
    private String className;

    public QRScanFragment(String classId, String className) {
        this.classId = classId;
        this.className = className;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_qr_scan, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        barcodeView = view.findViewById(R.id.barcode_view);
        Button captureButton = view.findViewById(R.id.capture_button);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                barcodeView.resume();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.setResultHandler(this);
        barcodeView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.stopCamera();
    }

    @Override
    public void handleResult(BarcodeResult result) {
        String qrCodeContent = result.getText();
        saveUserToClass(qrCodeContent);
    }

    private void saveUserToClass(String qrCodeContent) {
        // Decode the base64 QR code content and retrieve the user ID
        String userId = extractUserIdFromQRCode(qrCodeContent);

        if (userId != null) {
            DocumentReference classRef = db.collection("Classes").document(classId);
            classRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("user", userId);

                            db.collection("Classes").document(classId).collection("members").add(userMap);
                            Toast.makeText(getContext(), "Usuario agregado a la clase " + className, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "La clase no existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Error al obtener la clase", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(getContext(), "Código QR inválido", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractUserIdFromQRCode(String qrCodeContent) {
        // Implement your logic to extract the user ID from the base64 QR code content
        // For example, if the user ID is the first 24 characters of the QR code content
        if (qrCodeContent.length() >= 24) {
            return qrCodeContent.substring(0, 24);
        }
        return null;
    }
}