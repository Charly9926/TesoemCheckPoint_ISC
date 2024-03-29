package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.tesoemcheckpoint_isc.databinding.ActivityDocenteDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DocenteDashboardActivity extends AppCompatActivity {
    ActivityDocenteDashboardBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDocenteDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Boton flotante.
        binding.flotante.setOnClickListener(v -> replaceFragment(new CrearClaseFragment()));

        replaceFragment(new HomeFragment());
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.perfil) {
                replaceFragment(new PerfilFragment());
            } else if (item.getItemId() == R.id.addalumno) {
                replaceFragment(new RegistrarAlumnoFragment());
            } else if (item.getItemId() == R.id.logout) {
                // Mostrar alerta antes de cerrar sesión
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Cerrar sesión");
                builder.setMessage("¿Estás seguro que quieres cerrar sesión?");
                builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Cerrar sesión
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });
                builder.setNegativeButton("No", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}

//    //Validar si no hay usuarios conectados al iniciar actividad si no hay mandar a inicio
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (FirebaseAuth.getInstance().getCurrentUser() == null){
//            startActivity(new Intent(getApplicationContext(), MainActivity.class));
//            finish();
//        }
//    }

//    //firebase
//    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
//
//    //Cerrar session
//    Button logout = findViewById(R.id.ButtonLogout);
//        logout.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            FirebaseAuth.getInstance().signOut();
//            startActivity(new Intent(getApplicationContext(),MainActivity.class));
//            finish();
//        }
//    });