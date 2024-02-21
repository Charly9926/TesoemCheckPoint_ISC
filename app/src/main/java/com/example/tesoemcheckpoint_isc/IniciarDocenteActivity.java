package com.example.tesoemcheckpoint_isc;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IniciarDocenteActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_iniciar_docente);

        //firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

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
                    firebaseAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //mensaje
                            Toast.makeText(IniciarDocenteActivity.this, "Inicio exitoso", Toast.LENGTH_SHORT).show();
                            //verificamos si es docente
                            esDocente(authResult.getUser().getUid());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Respuesta incorrecta malos datos
                            emailInputLayout.setError("Se necesita un correo");
                            passwordInputLayout.setError("Se necesita una contraseña");
                        }
                    });
                }
            }
        });

        //Texto clickeable para llevar a actividad para registrar docente
        TextView Tregdocente = findViewById(R.id.Tregdocente);
        String texto = getString(R.string.aunnoreg);
        Spannable spannable = (Spannable) HtmlCompat.fromHtml(texto, HtmlCompat.FROM_HTML_MODE_LEGACY);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IniciarDocenteActivity.this, RegistrarDocenteActivity.class);
                startActivity(intent);
            }
        };
        spannable.setSpan(clickableSpan, texto.indexOf("Registrarse"), texto.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        Tregdocente.setMovementMethod(LinkMovementMethod.getInstance());
        Tregdocente.setText(spannable);
    }
    //Validar si el usuario es docente
    private void esDocente(String uid) {
        //agarramos el id del usuario
        DocumentReference df = firebaseFirestore.collection("Usuarios").document(uid);
        //leemos en la base de datos si es docente
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("TAG","onSuccess: " + documentSnapshot.getData());
                if ("1".equals(documentSnapshot.getString("EsDocente"))){
                    startActivity(new Intent(getApplicationContext(), DocenteDashboardActivity.class));
                    finish();
                } else {
                    //El usuario no es docente lo manda al menu principal
                    Toast.makeText(getApplicationContext(), "El usuario no es docente", Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }
                    }, 3000); //  3 segundos
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