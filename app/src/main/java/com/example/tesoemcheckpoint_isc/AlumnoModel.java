package com.example.tesoemcheckpoint_isc;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;


public class AlumnoModel implements Serializable {
    private String nombre;
    private int asistencias; // Nuevo campo para las asistencias

    public AlumnoModel() {}

    public AlumnoModel(String nombre, int asistencias) {
        this.nombre = nombre;
        this.asistencias = asistencias;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public void setAsistencias(int asistencias) {
        this.asistencias = asistencias;
    }
}