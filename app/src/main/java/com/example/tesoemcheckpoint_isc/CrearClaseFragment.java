package com.example.tesoemcheckpoint_isc;

import android.app.Dialog;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
                if (classId == null || classId.isEmpty()) {
                    Toast.makeText(requireContext(), "No hay código QR para mostrar", Toast.LENGTH_SHORT).show();
                } else {
                    showQRCodeDialog(classId);
                }
            }
        });
    }

    private void crearClase(String nombreClase) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        // Crear datos de la clase
        Map<String, Object> classData = new HashMap<>();
        classData.put("className", nombreClase);
        classData.put("admin", userId);
        classData.put("members", new ArrayList<>());

        // Agregar la clase a Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Clases")
                .add(classData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        classId = documentReference.getId();
                        Log.d(TAG, "Clase creada con ID: " + classId);
                        Toast.makeText(requireContext(), "Clase creada exitosamente", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error creando clase", e);
                        Toast.makeText(requireContext(), "Error creando clase", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showQRCodeDialog(String qrCodeText) {
        // Crear el dialog box
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_qr_code);  // Asegúrate de que dialog_qr_code es el layout correcto

        // Generar el código QR con la cadena de texto
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

    private Bitmap generateQRCode(String qrCodeText) {
        // Utilizar la biblioteca ZXing para generar el código QR
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix;
        try {
            matrix = writer.encode(qrCodeText, BarcodeFormat.QR_CODE, 600, 600);
        } catch (WriterException e) {
            // Manejar la excepción, por ejemplo, registrando un mensaje de error
            Log.e("QRCodeGeneration", "Error generating QR code", e);
            return null; // o algún bitmap por defecto
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
        // Crear el archivo en el directorio externo
        File file = new File(requireContext().getExternalCacheDir(), "qr_code.png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(requireContext(), "Código QR descargado con éxito", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Error", "Error al descargar el código QR", e);
        }
    }
}