package com.example.finals_attendify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finals_attendify.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.backArrow.setOnClickListener(v -> finish());

        binding.registerButton.setOnClickListener(v -> registerStudent());
    }

    private void registerStudent() {
        String studentNumber = binding.studentNumber.getText().toString().trim();
        String fullName = binding.fullName.getText().toString().trim();
        String yearLevel = binding.yearLevel.getText().toString().trim();
        String section = binding.section.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        // Basic validations
        if (TextUtils.isEmpty(studentNumber) || TextUtils.isEmpty(fullName) ||
                TextUtils.isEmpty(yearLevel) || TextUtils.isEmpty(section) || TextUtils.isEmpty(password)) {
            showAlert("All Fields Required", "Please fill in all the fields.");
            return;
        }

        if (password.length() < 8) {
            showAlert("Invalid Password", "Password must be at least 8 characters long.");
            return;
        }

        if (!binding.termsAndConditions.isChecked()) {
            showAlert("Terms and Conditions", "You must agree to the terms and conditions.");
            return;
        }

        String email = studentNumber + "@attendify.com";

        // Check if email already exists
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean isNewUser = task.getResult().getSignInMethods().isEmpty();
                if (!isNewUser) {
                    Toast.makeText(this, "This student number is already registered!", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> saveToDB(studentNumber, fullName, yearLevel, section))
                        .addOnFailureListener(e -> Toast.makeText(this, "Registration error: " + e.getMessage(), Toast.LENGTH_LONG).show());

            } else {
                Toast.makeText(this, "Error checking existing account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveToDB(String studentNumber, String fullName, String yearLevel, String section) {
        String uid = auth.getCurrentUser().getUid();

        Map<String, Object> student = new HashMap<>();
        student.put("studentNumber", studentNumber);
        student.put("fullName", fullName);
        student.put("yearLevel", yearLevel);
        student.put("section", section);

        db.collection("student_register")
                .document(uid)
                .set(student)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Login.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Firestore error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void showAlert(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
