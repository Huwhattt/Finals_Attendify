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
import java.util.List;

public class ClassMasterlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MasterlistAdapter adapter;
    private List<StudentStatus> studentList;
    private FirebaseFirestore db;
    private Button btnSuspend, btnReactivate;
    private StudentStatus selectedStudent; // This will hold the student you click on

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_masterlist);

        btnReactivate = findViewById(R.id.reactivate);
        btnSuspend = findViewById(R.id.suspend);
        recyclerView = findViewById(R.id.recyclerMaster);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentList = new ArrayList<>();

        // IMPORTANT: In your MasterlistAdapter, you should implement an OnItemClickListener
        // so that when a row is clicked, it sets the 'selectedStudent' variable.
        adapter = new MasterlistAdapter(studentList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        String sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            Toast.makeText(this, "No session ID provided", Toast.LENGTH_SHORT).show();
            return;
        }

        fetchAttendanceRecords(sessionId);

        // --- SUSPEND BUTTON LOGIC ---
        btnSuspend.setOnClickListener(v -> {
            if (selectedStudent != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Suspension")
                        .setMessage("This item will be temporarily disabled and cannot be used until reactivated.")
                        .setPositiveButton("Suspend", (dialog, which) -> {
                            updateStudentStatus(selectedStudent, true);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                Toast.makeText(this, "Select a student from the list first", Toast.LENGTH_SHORT).show();
            }
        });

        // --- REACTIVATE BUTTON LOGIC ---
        btnReactivate.setOnClickListener(v -> {
            if (selectedStudent != null) {
                new AlertDialog.Builder(this)
                        .setTitle("Reactivate")
                        .setMessage("This will make the item active and usable again.")
                        .setPositiveButton("Reactivate", (dialog, which) -> {
                            updateStudentStatus(selectedStudent, false);
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

    // THIS IS THE METHOD THAT WAS MISSING (The part that was red)
    private void updateStudentStatus(StudentStatus student, boolean suspendState) {
        // We find the document by studentNumber and update the 'suspended' field
        db.collection("attendance_records")
                .whereEqualTo("studentNumber", student.studentNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().update("suspended", suspendState)
                                .addOnSuccessListener(aVoid -> {
                                    student.suspended = suspendState; // Update local object
                                    adapter.notifyDataSetChanged(); // Refresh list view
                                    String message = suspendState ? "Student Suspended" : "Student Reactivated";
                                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}