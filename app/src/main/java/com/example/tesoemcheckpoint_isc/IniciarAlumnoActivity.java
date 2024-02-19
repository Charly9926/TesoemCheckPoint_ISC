package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class IniciarAlumnoActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;

    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_alumno);
        // Inicializar vistas
        emailInputLayout = findViewById(R.id.email_input_layout);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);

        // OnClickListener para boton
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidInput()) {
                    // Respuesta correcta

                } else {
                    // Respuesta incorrecta malos datos
                    emailInputLayout.setError("Se necesita un correo");
                    passwordInputLayout.setError("Se necesita una contraseña");
                }
            }
        });
    }

    //Validar si los campos no estan vacios
    private boolean isValidInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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