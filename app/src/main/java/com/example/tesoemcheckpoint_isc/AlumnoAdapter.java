package com.example.tesoemcheckpoint_isc;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AlumnoAdapter extends RecyclerView.Adapter<AlumnoAdapter.AlumnoViewHolder> {
    private List<AlumnoModel> alumnoList;

    public AlumnoAdapter(List<AlumnoModel> alumnoList) {
        this.alumnoList = alumnoList;
    }

    @NonNull
    @Override
    public AlumnoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alumno_item, parent, false);
        return new AlumnoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlumnoViewHolder holder, int position) {
        AlumnoModel currentAlumno = alumnoList.get(position);
        holder.alumnoName.setText(currentAlumno.getNombre());
        holder.asistenciasTextView.setText(String.valueOf(currentAlumno.getAsistencias())); // Mostrar el número de asistencias
    }

    @Override
    public int getItemCount() {
        return alumnoList.size();
    }

    public static class AlumnoViewHolder extends RecyclerView.ViewHolder {
        public TextView alumnoName;
        public TextView asistenciasTextView; // Nuevo TextView para mostrar las asistencias

        public AlumnoViewHolder(View itemView) {
            super(itemView);
            alumnoName = itemView.findViewById(R.id.alumno_name);
            asistenciasTextView = itemView.findViewById(R.id.asistencia_count);
        }
    }
}
