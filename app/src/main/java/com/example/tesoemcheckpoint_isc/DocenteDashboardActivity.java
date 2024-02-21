package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.tesoemcheckpoint_isc.databinding.ActivityDocenteDashboardBinding;

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
                replaceFragment(new LogoutFragment());
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