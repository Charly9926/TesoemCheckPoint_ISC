package com.example.tesoemcheckpoint_isc;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class ClassDetailsActivity extends AppCompatActivity {
    private TextView classNameTextView;
    private TextView membersCountTextView;
    private TextView adminIdTextView;
    private TextView classIdTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_details);

        classNameTextView = findViewById(R.id.classNameTextView);
        membersCountTextView = findViewById(R.id.membersCountTextView);
        adminIdTextView = findViewById(R.id.adminIdTextView);
        classIdTextView = findViewById(R.id.classIdTextView);



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        ClassModel classModel = (ClassModel) intent.getSerializableExtra("classModel");


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        List<String> userIds = new ArrayList<>();
        userIds.add(userId);

        DocumentReference userRef = db.collection("Usuarios").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String userRole = document.getString("EsDocente");
                    if ("1".equals(userRole)) {
                        // User is an administrator
                        db.collection("Clases")
                                .document(classModel.getClassId())
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document2 = task2.getResult();
                                        if (document2.exists()) {
                                            ClassModel classDetails = document2.toObject(ClassModel.class);
                                            displayClassDetails(classDetails);
                                        } else {
                                            Log.d("ClassDetailsActivity", "No such document");
                                        }
                                    } else {
                                        Log.d("ClassDetailsActivity", "get failed with ", task2.getException());
                                    }
                                });
                    } else {
                        // User is a student
                        db.collection("Clases")
                                .document(classModel.getClassId())
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot document2 = task2.getResult();
                                        if (document2.exists()) {
                                            List<String> members = (List<String>) document2.get("members");
                                            if (members.contains(userId)) {
                                                ClassModel classDetails = document2.toObject(ClassModel.class);
                                                displayClassDetails(classDetails);
                                            } else {
                                                Log.d("ClassDetailsActivity", "User is not a member of this class");
                                            }
                                        } else {
                                            Log.d("ClassDetailsActivity", "No such document");
                                        }
                                    } else {
                                        Log.d("ClassDetailsActivity", "get failed with ", task2.getException());
                                    }
                                });
                    }
                } else {
                    Log.d("ClassDetailsActivity", "No such document");
                }
            } else {
                Log.d("ClassDetailsActivity", "get failed with ", task.getException());
            }
        });
    }

    private void displayClassDetails(ClassModel classModel) {
        classNameTextView.setText(classModel.getClassName());
        membersCountTextView.setText("Members Count: " + classModel.getMembersCount());
        adminIdTextView.setText("Admin ID: " + classModel.getAdminId());
        classIdTextView.setText("Class ID: " + classModel.getClassId());
    }
}