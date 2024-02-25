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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
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
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();

                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            //guardar id del usuario creado
                            FirebaseUser usuario = authResult.getUser();
                            DocumentReference df = firebaseFirestore.collection("Usuarios").document(usuario.getUid());
                            //mensaje
                            Toast.makeText(getActivity(), "Registro exitoso", Toast.LENGTH_SHORT).show();
                            //registrar usuario en base de datos
                            Map<String,Object> usuarioInfo = new HashMap<>();
                            usuarioInfo.put("Nombre", "Alumno");
                            usuarioInfo.put("Correo",email);
                            usuarioInfo.put("EsDocente","0"); // Usuario como Alumno

                            df.set(usuarioInfo);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessage = null;
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                errorMessage = "Ya existe una cuenta registrada con este correo electrónico.";
                            } else if (e instanceof FirebaseAuthWeakPasswordException) {
                                errorMessage = "La contraseña debe tener al menos 6 caracteres.";
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "El formato del correo electrónico es inválido.";
                            } else {
                                errorMessage = "Ha ocurrido un error desconocido. Inténtalo de nuevo más tarde.";
                            }

                            if (errorMessage != null) {
                                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Fallo a crear cuenta", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
        return view;
    }

    //Validar si los campos estan correctos
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

        if (password.length() < 6) {
            passwordInputLayout.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        if (!password.equals(confirmpassword)) {
            confirmpasswordInputLayout.setError("Las contraseñas no coinciden");
            return false;
        }

        return true;
    }
}