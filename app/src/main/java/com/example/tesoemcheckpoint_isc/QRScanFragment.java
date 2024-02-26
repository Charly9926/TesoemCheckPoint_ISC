package com.example.tesoemcheckpoint_isc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class QRScanFragment extends Fragment {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        barcodeView = view.findViewById(R.id.barcode_view);

        // Configura el ResultCallback en el DecoratedBarcodeView
        barcodeView.setBarcodeViewInitializedListener(new BarcodeCallback());

        capture = new CaptureManager(getActivity(), barcodeView);
        capture.initializeFromIntent(getActivity().getIntent(), savedInstanceState);
        capture.decode();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    // Cambia el nombre de la clase a BarcodeCallback
    private class BarcodeCallback implements DecoratedBarcodeView.TorchListener, DecoratedBarcodeView.BarcodeCallback {
        @Override
        public void barcodeResult(BarcodeResult result) {
            Result rawResult = result.getResult();
            String qrCode = rawResult.getText();

            if (qrCode != null) {
                // Tu lógica para manejar el resultado del escaneo
                String userId = decodeQRCode(qrCode);

                // Resto del código...
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Puedes dejar esto vacío o manejar eventos adicionales si es necesario
        }

        @Override
        public void onTorchOn() {
            // Puedes manejar eventos relacionados con la linterna encendida si es necesario
        }

        @Override
        public void onTorchOff() {
            // Puedes manejar eventos relacionados con la linterna apagada si es necesario
        }

        private String decodeQRCode(String qrCode) {
            // Implementa la decodificación del código QR en base 64 y obtén el userId
            // Puedes usar una biblioteca como ZXing o Google Mobile Vision para decodificar el código QR
            // En este ejemplo, asumimos que el código QR ya está decodificado y contiene el userId
            return qrCode;
        }
    }
}