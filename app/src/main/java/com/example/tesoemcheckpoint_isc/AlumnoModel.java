package com.example.tesoemcheckpoint_isc;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;


public class AlumnoModel implements Serializable {
    private String nombre;

    public AlumnoModel() {}

    public AlumnoModel(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}