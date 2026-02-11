package com.example.finals_attendify;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity_Students extends AppCompatActivity {

    TextView txt1;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private  String scannedQrText;
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

        String sessionId = getIntent().getStringExtra("SESSION_ID");
        if (sessionId != null) {
            markAttendance(sessionId);
        }

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            View view = getLayoutInflater().inflate(R.layout.logout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_Students.this);
            builder.setView(view);

            AlertDialog dialog = builder.create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(Color.TRANSPARENT)
                );
            }

            Button btnYes = view.findViewById(R.id.btnYes);
            Button btnNo = view.findViewById(R.id.btnNo);

            btnYes.setOnClickListener(v1 -> {
                Intent intent = new Intent(MainActivity_Students.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });

            btnNo.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                    }
                });

    }

    // method to add sa firebase
    private void markAttendance(String sessionId) {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("student_register")
                .document(uid)
                .get()
                .addOnSuccessListener(studentDoc -> {

                    if (!studentDoc.exists()) {
                        Toast.makeText(this, "Student record not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String studentNumber = studentDoc.getString("studentNumber");

                    db.collection("attendance_sessions")
                            .document(sessionId)
                            .get()
                            .addOnSuccessListener(sessionDoc -> {

                                if (!sessionDoc.exists()) {
                                    Toast.makeText(this, "Invalid or expired QR", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String lateStr = sessionDoc.getString("lateTime");
                                String endStr  = sessionDoc.getString("endTime");

                                if (lateStr == null || endStr == null) {
                                    Toast.makeText(this, "Session time data invalid", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                try {
                                    SimpleDateFormat sdf =
                                            new SimpleDateFormat("hh:mm a", Locale.getDefault());

                                    Date now = sdf.parse(sdf.format(new Date()));
                                    Date lateTime = sdf.parse(lateStr);
                                    Date endTime  = sdf.parse(endStr);

                                    String status;
                                    if (now.before(lateTime) || now.equals(lateTime)) {
                                        status = "PRESENT";
                                    } else if (now.before(endTime) || now.equals(endTime)) {
                                        status = "LATE";
                                    } else {
                                        status = "ABSENT";
                                    }

                                    SimpleDateFormat sa = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.getDefault());
                                    String scannedAt = sa.format(new Date());

                                    Map<String, Object> record = new HashMap<>();
                                    record.put("sessionId", sessionId);
                                    record.put("studentNumber", studentNumber);
                                    record.put("status", status);
                                    record.put("scannedAt", scannedAt);

                                    db.collection("attendance_records")
                                            .add(record)
                                            .addOnSuccessListener(doc -> {

                                                View view = getLayoutInflater()
                                                        .inflate(R.layout.attendance_recorded, null);

                                                AlertDialog dialog = new AlertDialog.Builder(this)
                                                        .setView(view)
                                                        .setCancelable(false)
                                                        .create();

                                                dialog.getWindow().setBackgroundDrawable(
                                                        new ColorDrawable(Color.TRANSPARENT)
                                                );

                                                TextView tvMessage = view.findViewById(R.id.tvMessage);
                                                Button btnOk = view.findViewById(R.id.btnOk);

                                                tvMessage.setText("Status: " + status);

                                                btnOk.setOnClickListener(v -> dialog.dismiss());

                                                dialog.show();
                                            });

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this,
                                            "Time parsing error",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }
    private void showJoinClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.joinclass, null);
        builder.setView(view);

        EditText etClassCode = view.findViewById(R.id.etClassCode);
        Button btnJoin = view.findViewById(R.id.btnJoin);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );
        dialog.show();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnJoin.setOnClickListener(v -> {
            String code = etClassCode.getText().toString().trim();
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