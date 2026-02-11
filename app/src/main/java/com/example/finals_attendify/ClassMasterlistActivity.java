package com.example.finals_attendify;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassMasterlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MasterlistAdapter adapter;
    private List<StudentStatus> studentList;
    private FirebaseFirestore db;
    private Button btnSuspend, btnReactivate;
    private StudentStatus selectedStudent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_masterlist);

        btnReactivate = findViewById(R.id.reactivate);
        btnSuspend = findViewById(R.id.suspend);
        recyclerView = findViewById(R.id.recyclerMaster);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();
        adapter = new MasterlistAdapter(studentList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "No session ID provided", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchAttendanceRecords(sessionId);

        btnSuspend.setOnClickListener(v -> {
            selectedStudent = adapter.getSelectedStudent();
            if (selectedStudent != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Suspension")
                        .setMessage("Student " + selectedStudent.studentNumber + " will be blocked and marked as SUSPENDED.")
                        .setPositiveButton("Suspend", (dialog, which) -> {
                            // Pass true for suspendState and the new status string
                            updateStudentStatus(selectedStudent, true, "SUSPENDED");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Select a student from the list first", Toast.LENGTH_SHORT).show();
            }
        });

        btnReactivate.setOnClickListener(v -> {
            selectedStudent = adapter.getSelectedStudent();
            if (selectedStudent != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Reactivate")
                        .setMessage("Allow student " + selectedStudent.studentNumber + " to log in again?")
                        .setPositiveButton("Reactivate", (dialog, which) -> {
                            // Pass false for suspendState and reset status to ACTIVE (or PRESENT/LATE if you prefer)
                            updateStudentStatus(selectedStudent, false, "ACTIVE");
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Select a student from the list first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceRecords(String sessionId) {
        db.collection("attendance_records")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    studentList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String studentNumber = doc.getString("studentNumber");
                        String scannedAt = doc.getString("scannedAt");
                        String status = doc.getString("status");

                        Boolean isSuspended = doc.getBoolean("suspended");
                        if (isSuspended == null) isSuspended = false;

                        if (studentNumber != null && scannedAt != null && status != null) {
                            studentList.add(new StudentStatus(studentNumber, scannedAt, status, isSuspended));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // UPDATED METHOD: Now accepts a status string to update the UI text
    private void updateStudentStatus(StudentStatus student, boolean suspendState, String newStatusText) {

        // Prepare multiple fields to update
        Map<String, Object> updates = new HashMap<>();
        updates.put("suspended", suspendState);
        updates.put("status", newStatusText);

        db.collection("attendance_records")
                .whereEqualTo("studentNumber", student.studentNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Student record not found in DB", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().update(updates)
                                .addOnSuccessListener(aVoid -> {
                                    // Update the local object so the RecyclerView changes immediately
                                    student.suspended = suspendState;
                                    student.status = newStatusText;

                                    adapter.notifyDataSetChanged();

                                    String message = suspendState ? "Student Suspended" : "Student Reactivated";
                                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}