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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity_Employee extends AppCompatActivity implements EmployeeClassAdapter.OnItemClickListener {

    private FirebaseFirestore db;
    private TextView tvGreeting, tvNoClasses;
    private RecyclerView recyclerView;
    private EmployeeClassAdapter adapter;
    private String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_employee);

        db = FirebaseFirestore.getInstance();

        tvGreeting = findViewById(R.id.tvGreeting);
        recyclerView = findViewById(R.id.srow);
        tvNoClasses = findViewById(R.id.tvNoClasses);
        ImageView plusBtn = findViewById(R.id.splus);

        employeeId = getIntent().getStringExtra("employeeId");
        if (employeeId == null) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmployeeClassAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        loadEmployeeName();
        loadCreatedClasses();

        plusBtn.setOnClickListener(v -> showCreateClassDialog());

        Button btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {

            View view = getLayoutInflater().inflate(R.layout.logout, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity_Employee.this);
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
                Intent intent = new Intent(MainActivity_Employee.this, MainActivity.class);
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


    private void loadEmployeeName() {
        db.collection("employee")
                .document(employeeId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String fullName = doc.getString("name");
                        if (fullName != null && !fullName.isEmpty()) {
                            tvGreeting.setText("Hello, " + fullName.split(" ")[0] + "!");
                        }
                    }
                })
                .addOnFailureListener(e -> tvGreeting.setText("Hello, Employee!"));
    }


    private void showCreateClassDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.employee_joinclass, null);
        builder.setView(view);

        EditText etClassName = view.findViewById(R.id.etClassName);
        EditText etSection = view.findViewById(R.id.etSection);
        EditText etCustomCode = view.findViewById(R.id.etCustomCode);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );
        dialog.show();

        view.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String className = etClassName.getText().toString().trim();
            String section = etSection.getText().toString().trim();
            String customCode = etCustomCode.getText().toString().trim();

            if (className.isEmpty()) {
                etClassName.setError("Enter class name");
                return;
            }
            if (section.isEmpty()) {
                etSection.setError("Enter section");
                return;
            }


            String classCode = customCode.isEmpty() ? generateCode() : customCode;

            createClass(className, section, classCode);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
    }


    private void createClass(String subjectName, String section, String classCode) {
        EmployeeClass newClass = new EmployeeClass(subjectName, section, employeeId);

        db.collection("subjects")
                .document(classCode)
                .set(newClass)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Class created! Code: " + classCode, Toast.LENGTH_LONG).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void loadCreatedClasses() {
        db.collection("subjects")
                .whereEqualTo("createdBy", employeeId)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    List<EmployeeClass> classList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {
                        String name = doc.getString("subjectName");
                        String section = doc.getString("section");
                        if (name != null && section != null) {
                            classList.add(new EmployeeClass(name, section));
                        }
                    }

                    if (classList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        tvNoClasses.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        tvNoClasses.setVisibility(View.GONE);
                    }

                    adapter.updateData(classList);
                });
    }

    private String generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(r.nextInt(chars.length())));
        }
        return code.toString();
    }
    @Override
    public void onItemClick(EmployeeClass employeeClass) {
        Intent intent = new Intent(MainActivity_Employee.this, employee_class_dashboard.class);
        intent.putExtra("subjectName", employeeClass.subjectName);
        intent.putExtra("section", employeeClass.section);

        // Pass these for persistence
        intent.putExtra("qrData", employeeClass.qrData);
        intent.putExtra("expiryTimestamp", employeeClass.expiryTimestamp);

        startActivity(intent);
    }
}