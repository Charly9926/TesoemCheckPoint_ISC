package com.example.tesoemcheckpoint_isc;

import com.google.firebase.firestore.DocumentSnapshot;

import java.io.Serializable;
import java.io.StringReader;
import java.util.List;

public class ClassModel implements Serializable {
    private String className;
    private List<Object> members;
    private String adminId;
    private String classId;
    private String qrCode;

    public ClassModel(){}

    public ClassModel(String className, List<Object> members, String adminId, String classId, String qrCode) {
        this.className = className;
        this.members = members;
        this.adminId = adminId;
        this.classId = classId;
        this.qrCode = qrCode;
    }

    public ClassModel(DocumentSnapshot documentSnapshot) {
        className = documentSnapshot.getString("className");
        List<Object> membersList = (List<Object>) documentSnapshot.get("members");
        members = membersList;
        adminId = documentSnapshot.getString("admin");
        classId = documentSnapshot.getId();
        qrCode = documentSnapshot.getString("qrCode");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Object> getMembers() {
        return members;
    }

    public void setMembers(List<Object> members) {
        this.members = members;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getClassId() {
        return classId;
    }

    public void setClassId(String classId) {
        this.classId = classId;
    }

    public int getMembersCount() {
        return members.size();
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

}