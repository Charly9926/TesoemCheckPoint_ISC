package com.example.tesoemcheckpoint_isc;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private RecyclerView classesRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        classesRecyclerView = view.findViewById(R.id.classes_recycler_view);
        classesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> userIds = new ArrayList<>();
        userIds.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Verificar si el usuario es administrador o alumno
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Usuarios").document(userIds.get(0));
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    String userRole = documentSnapshot.getString("EsDocente");
                    if ("1".equals(userRole)) {
                        // Usuario es administrador

                        Query query1 = FirebaseFirestore.getInstance()
                                .collection("Clases")
                                .whereIn("admin", userIds);

                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                        tasks.add(query1.get());

                        Tasks.whenAllSuccess(tasks.toArray(new Task[0]))
                                .addOnCompleteListener(taskAll -> {
                                    if (taskAll.isSuccessful()) {
                                        List<ClassModel> classList = new ArrayList<>();
                                        for (Task<QuerySnapshot> queryTask : tasks) {
                                            for (DocumentSnapshot document : queryTask.getResult()) {
                                                classList.add(new ClassModel(document));
                                            }
                                        }
                                        ClassAdapter classAdapter = new ClassAdapter(classList);
                                        classesRecyclerView.setAdapter(classAdapter);
                                    } else {
                                        Log.e("HomeFragment", "Error getting documents: ", taskAll.getException());
                                    }
                                });

                    } else if ("0".equals(userRole)){
                        // Usuario es alumno

                        Query query2 = FirebaseFirestore.getInstance()
                                .collection("Clases")
                                .whereArrayContainsAny("members", userIds);

                        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
                        tasks.add(query2.get());

                        Tasks.whenAllSuccess(tasks.toArray(new Task[0]))
                                .addOnCompleteListener(taskAll -> {
                                    if (taskAll.isSuccessful()) {
                                        List<ClassModel> classList = new ArrayList<>();
                                        for (Task<QuerySnapshot> queryTask : tasks) {
                                            for (DocumentSnapshot document : queryTask.getResult()) {
                                                classList.add(new ClassModel(document));
                                            }
                                        }
                                        ClassAdapter classAdapter = new ClassAdapter(classList);
                                        classesRecyclerView.setAdapter(classAdapter);
                                    } else {
                                        Log.e("HomeFragment", "Error getting documents: ", taskAll.getException());
                                    }
                                });
                    }
                } else {
                    Log.e("HomeFragment", "Document does not exist");
                }
            } else {
                Log.e("HomeFragment", "Error getting document: ", task.getException());
            }
        });

        return view;
    }
}