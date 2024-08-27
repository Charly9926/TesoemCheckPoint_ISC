package com.example.tesoemcheckpoint_isc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class PerfilFragment extends Fragment {

    private TextView textViewNombre, textViewUserId, textViewDocenteStatus, textViewCorreo;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Button eliminarUsuarioButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        textViewNombre = view.findViewById(R.id.textViewNombre);
        textViewUserId = view.findViewById(R.id.textViewUserId);
        textViewDocenteStatus = view.findViewById(R.id.textViewDocenteStatus);
        textViewCorreo = view.findViewById(R.id.textViewCorreo);
        eliminarUsuarioButton = view.findViewById(R.id.eliminarUsuarioButton);

        cargarPerfilUsuario();
        eliminarUsuarioButton.setOnClickListener(v -> mostrarConfirmacionEliminar());

        return view;
    }

    private void cargarPerfilUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = db.collection("Usuarios").document(userId);
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String nombre = document.getString("Nombre");
                        String esDocente = document.getString("EsDocente"); // Obtén el valor como cadena
                        String correo = user.getEmail();

                        textViewNombre.setText(nombre);
                        textViewUserId.setText(userId);

                        // Comparar el valor de esDocente
                        if ("1".equals(esDocente)) {
                            textViewDocenteStatus.setText("Docente");
                        } else if ("0".equals(esDocente)) {
                            textViewDocenteStatus.setText("Estudiante");
                        } else {
                            textViewDocenteStatus.setText("Desconocido");
                        }

                        textViewCorreo.setText(correo);
                    } else {
                        Toast.makeText(getContext(), "No se encontró la información del usuario", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener la información del usuario", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    private void mostrarConfirmacionEliminar() {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Usuario")
                .setMessage("¿Seguro que quieres eliminar tu usuario? Esta opción no se puede revertir.")
                .setPositiveButton("Sí", (dialog, which) -> eliminarUsuario())
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DocumentReference docRef = db.collection("Usuarios").document(userId);
            docRef.delete().addOnSuccessListener(aVoid -> {
                user.delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Usuario eliminado con éxito", Toast.LENGTH_SHORT).show();
                        // Redirigir al usuario a la pantalla de inicio de sesión o cerrar la actividad
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish(); //Cierra la actividad actual
                    } else {
                        Toast.makeText(getContext(), "Error al eliminar el usuario", Toast.LENGTH_SHORT).show();
                    }
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error al eliminar el documento del usuario", Toast.LENGTH_SHORT).show();
            });
        }
    }
}