package com.example.tesoemcheckpoint_isc;

import com.google.firebase.firestore.DocumentSnapshot;

public class ClassModel {
    private String className;
    private String membersCount;

    public ClassModel() {
    }

    public ClassModel(int classImage, String className, String membersCount) {
        this.className = className;
        this.membersCount = membersCount;
    }

    public ClassModel(DocumentSnapshot documentSnapshot) {
        className = documentSnapshot.getString("class_name");
        membersCount = documentSnapshot.getString("members_count");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(String membersCount) {
        this.membersCount = membersCount;
    }
}