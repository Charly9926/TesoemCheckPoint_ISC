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

        Query query1 = FirebaseFirestore.getInstance()
                .collection("Clases")
                .whereIn("admin", userIds);

        Query query2 = FirebaseFirestore.getInstance()
                .collection("Clases")
                .whereIn("members", userIds);

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(query1.get());
        tasks.add(query2.get());

        Tasks.whenAllSuccess(tasks.toArray(new Task[0]))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ClassModel> classList = new ArrayList<>();
                        for (Task<QuerySnapshot> queryTask : tasks) {
                            for (DocumentSnapshot document : queryTask.getResult()) {
                                classList.add(new ClassModel(document));
                            }
                        }
                        ClassAdapter classAdapter = new ClassAdapter(classList);
                        classesRecyclerView.setAdapter(classAdapter);
                    } else {
                        Log.e("HomeFragment", "Error getting documents: ", task.getException());
                    }
                });

        return view;
    }
}