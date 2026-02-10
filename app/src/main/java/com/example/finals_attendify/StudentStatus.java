package com.example.finals_attendify;


public class StudentStatus {
    public String studentNumber;
    public String scannedAt;
    public String status;


    public StudentStatus() {
        // Empty constructor required for Firestore
    }


    public StudentStatus(String studentNumber, String scannedAt, String status) {
        this.studentNumber = studentNumber;
        this.scannedAt = scannedAt;
        this.status = status;
    }
}
