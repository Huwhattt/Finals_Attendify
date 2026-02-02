package com.example.finals_attendify;

public class EmployeeClass {

    public String subjectName;
    public String section;
    public String createdBy;

    // Empty constructor needed for Firestore
    public EmployeeClass() { }

    // Constructor for saving
    public EmployeeClass(String subjectName, String section, String createdBy) {
        this.subjectName = subjectName;
        this.section = section;
        this.createdBy = createdBy;
    }

    // Constructor for reading
    public EmployeeClass(String subjectName, String section) {
        this.subjectName = subjectName;
        this.section = section;
    }
}
