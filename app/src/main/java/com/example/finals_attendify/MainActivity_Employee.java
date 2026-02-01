package com.example.finals_attendify;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity_Employee extends AppCompatActivity {

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
        adapter = new EmployeeClassAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadEmployeeName();
        loadCreatedClasses();

        plusBtn.setOnClickListener(v -> showCreateClassDialog());
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
}
