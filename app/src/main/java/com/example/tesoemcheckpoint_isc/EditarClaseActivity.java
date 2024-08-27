package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

public class EditarClaseActivity extends AppCompatActivity {

    private EditText editClassNameEditText;
    private Button saveClassButton;
    private Button deleteClassButton;
    private FirebaseFirestore db;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_clase);

        // Obtener el classId de la intención
        classId = getIntent().getStringExtra("classId");

        // Inicializar FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Referencias de las vistas
        editClassNameEditText = findViewById(R.id.editClassNameEditText);
        saveClassButton = findViewById(R.id.saveClassButton);
        deleteClassButton = findViewById(R.id.deleteClassButton);

        // Cargar la información actual de la clase
        db.collection("Clases").document(classId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String className = documentSnapshot.getString("className");
                editClassNameEditText.setText(className);
            }
        });

        // Guardar los cambios al hacer clic en el botón
        saveClassButton.setOnClickListener(v -> {
            String newClassName = editClassNameEditText.getText().toString();
            if (!newClassName.isEmpty()) {
                db.collection("Clases").document(classId)
                        .update("className", newClassName)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditarClaseActivity.this, "Clase actualizada", Toast.LENGTH_SHORT).show();
                            finish(); // Cerrar la actividad después de guardar los cambios
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditarClaseActivity.this, "Error al actualizar", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(EditarClaseActivity.this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });

        // Eliminar la clase al hacer clic en el botón
        deleteClassButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar Clase")
                    .setMessage("¿Estás seguro de que deseas eliminar esta clase?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        db.collection("Clases").document(classId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(EditarClaseActivity.this, "Clase eliminada", Toast.LENGTH_SHORT).show();
                                    finish(); // Cerrar la actividad después de eliminar la clase
                                })
                                .addOnFailureListener(e -> Toast.makeText(EditarClaseActivity.this, "Error al eliminar", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}