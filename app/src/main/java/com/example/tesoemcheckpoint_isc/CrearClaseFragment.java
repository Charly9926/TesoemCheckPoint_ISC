package com.example.tesoemcheckpoint_isc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
public class CrearClaseFragment extends Fragment {
    private static final String TAG = "CrearClaseFragment";
    private EditText class_name_input;
    private Button create_class_button, show_qr_code_button;
    private ImageView qr_code_image;
    private String classId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_crear_clase, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        class_name_input = view.findViewById(R.id.class_name_input);
        create_class_button = view.findViewById(R.id.create_class_button);
        show_qr_code_button = view.findViewById(R.id.show_qr_code_button);
        qr_code_image = view.findViewById(R.id.qr_code_image);

        create_class_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nombreClase = String.valueOf(class_name_input.getText());
                if (!nombreClase.isEmpty()) {
                    crearClase(nombreClase);
                } else {
                    Toast.makeText(requireContext(), "Ingrese un nombre de clase válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        show_qr_code_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String qrCode = qr_code_image.getTag().toString();
                if (!qrCode.isEmpty()) {
                    showQRCode(qrCode);
                } else {
                    Toast.makeText(requireContext(), "No hay código QR para mostrar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void crearClase(String nombreClase) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        // Generate QR code
        String qrCode = generateQRCode(userId + ";" + nombreClase);

        // Create class data as a HashMap
        Map<String, Object> classData = new HashMap<>();
        classData.put("className", nombreClase);
        classData.put("admin", userId);
        classData.put("members", new ArrayList<>());
        classData.put("qrCode", qrCode);

        // Add class to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Clases")
                .add(classData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        classId = documentReference.getId();
                        Log.d(TAG, "Clase creada con ID: " + classId);
                        Toast.makeText(requireContext(), "Clase creada exitosamente", Toast.LENGTH_SHORT).show();
                        qr_code_image.setTag(qrCode); // Save the QR code for later use
                        showQRCode(classId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error creating class", e);
                        Toast.makeText(requireContext(), "Error creando clase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showQRCode(String qrCode) {
        Bitmap bitmap = decodeBase64ToBitmap(qrCode);
        qr_code_image.setImageBitmap(bitmap);
    }

    private Bitmap decodeBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    private String generateQRCode(String data) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return encodeAsBase64String(bmp); // Convert Bitmap to Base64 string
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encodeAsBase64String(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}