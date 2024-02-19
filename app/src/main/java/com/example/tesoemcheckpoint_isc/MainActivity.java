package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        // Obtener una instancia de FirebaseAuth
//        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
//
//        // Cerrar sesión en Firebase
//        firebaseAuth.signOut();
    }

    public void onDocenteClick(View view) {
        Intent intent = new Intent(this, IniciarDocenteActivity.class);
        startActivity(intent);
    }

    public void onAlumnoClick(View view) {
        Intent intent = new Intent(this, IniciarAlumnoActivity.class);
        startActivity(intent);
    }
}