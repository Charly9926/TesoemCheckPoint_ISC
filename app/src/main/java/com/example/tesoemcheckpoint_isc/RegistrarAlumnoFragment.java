package com.example.tesoemcheckpoint_isc;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tesoemcheckpoint_isc.databinding.ActivityDocenteDashboardBinding;
import com.example.tesoemcheckpoint_isc.databinding.ActivityRegistrarDocenteBinding;
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

public class RegistrarAlumnoFragment extends Fragment {

    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmpasswordInputLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmpasswordInput;
    private Button registerButton;


    //Firebase
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registrar_alumno, container, false);

        // Inicializar vistas
        emailInputLayout = view.findViewById(R.id.email_input_layout);
        passwordInputLayout = view.findViewById(R.id.password_input_layout);
        confirmpasswordInputLayout = view.findViewById(R.id.confirmpassword_input_layout);
        emailInput = view.findViewById(R.id.email_input);
        passwordInput = view.findViewById(R.id.password_input);
        confirmpasswordInput = view.findViewById(R.id.confirmpassword_input);
        registerButton = view.findViewById(R.id.register_button);

        //firebase codigo
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // OnClickListener para boton
        registerButton.setOnClickListener(new View.OnClickListener() {
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
                            Toast.makeText(getActivity(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                            //registrar usuario en base de datos
                            Map<String,Object> usuarioInfo = new HashMap<>();
                            usuarioInfo.put("Nombre", "Alumno"); // You can change this to get the user's name from another EditText
                            usuarioInfo.put("Correo",emailInput.getText().toString());
                            usuarioInfo.put("EsDocente","0"); // Set the user as an alumno

                            df.set(usuarioInfo);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Respuesta incorrecta malos datos
                            //mensaje
                            Toast.makeText(getActivity(), "Fallo a crear cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });
        return view;
    }

    //Validar si los campos no estan vacios
    private boolean isValidInput() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmpassword = confirmpasswordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("El correo es requerido");
            return false;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("La contraseña es requerida");
            return false;
        }

        if (!password.equals(confirmpassword)) {
            confirmpasswordInputLayout.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
}