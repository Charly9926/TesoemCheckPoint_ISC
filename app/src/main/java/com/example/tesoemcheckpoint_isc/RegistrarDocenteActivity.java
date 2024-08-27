package com.example.tesoemcheckpoint_isc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrarDocenteActivity extends AppCompatActivity {

    private TextInputLayout nombreInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmpasswordInputLayout;
    private TextInputEditText nombreInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmpasswordInput;
    private Button loginButton;

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_registrar_docente);
        // Inicializar vistas
        nombreInputLayout = findViewById(R.id.nombre_input_layout);
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        confirmpasswordInputLayout = findViewById(R.id.confirmpassword_input_layout);
        nombreInput = findViewById(R.id.nombre_input);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmpasswordInput = findViewById(R.id.confirmpassword_input);
        loginButton = findViewById(R.id.login_button);

        //firebase codigo
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // OnClickListener para boton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    // Respuesta correcta
                    firebaseAuth.createUserWithEmailAndPassword(emailInput.getText().toString(),passwordInput.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //guardar id del usuario creado
                            FirebaseUser usuario = firebaseAuth.getCurrentUser();
                            DocumentReference df = firebaseFirestore.collection("Usuarios").document(usuario.getUid());
                            //mensaje
                            Toast.makeText(RegistrarDocenteActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                            //registrar usuario en base de datos
                            Map<String,Object> usuarioInfo = new HashMap<>();
                            usuarioInfo.put("Nombre", nombreInput.getText().toString());
                            usuarioInfo.put("Correo",emailInput.getText().toString());

                            //Nivel de acceso de docente
                            usuarioInfo.put("EsDocente","1");

                            df.set(usuarioInfo);

                            //iniciar actividad para iniciar session
                            startActivity(new Intent(getApplicationContext(), IniciarDocenteActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Respuesta incorrecta malos datos
                            //mensaje
                            Toast.makeText(RegistrarDocenteActivity.this, "Fallo a crear cuenta", Toast.LENGTH_SHORT).show();
                            emailInputLayout.setError("Se necesita un correo");
                            passwordInputLayout.setError("Se necesita una contraseña");
                            confirmpasswordInputLayout.setError("Las contraseñas no coinciden");
                        }
                    });

                }
            }
        });
    }

    //Validar si los campos no estan vacios
    private boolean isValidInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String nombre = nombreInput.getText().toString().trim();

        if (nombre.isEmpty()) {
            nombreInputLayout.setError("El nombre es requerido");
            return false;
        }

        if (email.isEmpty()) {
            emailInputLayout.setError("El correo es requerido");
            return false;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("La contraseña es requerida");
            return false;
        }

        return true;
    }
}

