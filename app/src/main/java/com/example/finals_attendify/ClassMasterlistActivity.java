package com.example.finals_attendify;


import android.os.Bundle;
import android.widget.Toast;


import androidx.annotation.Nullable;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_masterlist);


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
    }


    private void fetchAttendanceRecords(String sessionId) {
        studentList.clear();


        db.collection("attendance_records")
                .whereEqualTo("sessionId", sessionId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String studentNumber = doc.getString("studentNumber");
                        String scannedAt = doc.getString("scannedAt");
                        String status = doc.getString("status");


                        if (studentNumber != null && scannedAt != null && status != null) {
                            studentList.add(new StudentStatus(studentNumber, scannedAt, status));
                        }
                    }


                    if (studentList.isEmpty()) {
                        Toast.makeText(this, "No records found", Toast.LENGTH_SHORT).show();
                    }


                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch records: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
