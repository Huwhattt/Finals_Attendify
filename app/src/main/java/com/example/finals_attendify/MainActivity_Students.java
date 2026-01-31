package com.example.finals_attendify;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity_Students extends AppCompatActivity {

    TextView txt1;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RecyclerView recyclerView;
    private StudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_students);

        txt1 = findViewById(R.id.wcstudent);
        recyclerView = findViewById(R.id.srow);
        ImageView splus = findViewById(R.id.splus);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadJoinedClasses();

        db.collection("student_register").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        String surname = extractSurname(fullName);
                        txt1.setText("Welcome, " + surname + " !");
                    } else {
                        txt1.setText("Welcome!");
                        Toast.makeText(this, "Student info not found (document missing)", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    txt1.setText("Welcome!");
                    Toast.makeText(this, "Error fetching student info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        splus.setOnClickListener(v -> showJoinClassDialog());
    }

    private void showJoinClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.joinclass, null);
        builder.setView(view);

        EditText etClassCode = view.findViewById(R.id.etClassCode);
        Button btnJoin = view.findViewById(R.id.btnJoin);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnJoin.setOnClickListener(v -> {
            String code = etClassCode.getText().toString().trim().toUpperCase();
            if (code.isEmpty()) {
                etClassCode.setError("Enter class code");
                return;
            }

            db.collection("subjects").document(code).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String subjectName = documentSnapshot.getString("subjectName");
                            joinClass(code, subjectName);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Invalid class code", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void joinClass(String classCode, String subjectName) {
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> enrollmentData = new HashMap<>();
        enrollmentData.put("subjectName", subjectName);

        db.collection("student_register")
                .document(uid)
                .collection("enrolledClasses")
                .document(classCode)
                .set(enrollmentData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Joined " + subjectName, Toast.LENGTH_SHORT).show();
                    loadJoinedClasses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to join class: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadJoinedClasses() {
        String uid = auth.getCurrentUser().getUid();

        TextView tvNoClasses = findViewById(R.id.tvNoClasses);

        db.collection("student_register")
                .document(uid)
                .collection("enrolledClasses")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading classes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    List<String> classList = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : value) {
                        String subjectName = doc.getString("subjectName");
                        if (subjectName != null) {
                            classList.add(subjectName);
                        }
                    }

                    if (classList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvNoClasses.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoClasses.setVisibility(View.GONE);
                        adapter.updateData(classList);
                    }
                });
    }


    private String extractSurname(String fullName) {
        if (fullName == null || fullName.isEmpty()) return "";
        String[] parts = fullName.trim().split(" ");
        return parts[parts.length - 1];
    }
}
