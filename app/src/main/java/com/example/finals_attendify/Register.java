package com.example.finals_attendify;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finals_attendify.databinding.ActivityRegisterBinding;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backArrow.setOnClickListener(v -> finish());

        binding.registerButton.setOnClickListener(v -> {
            String studentNumber = binding.studentNumber.getText().toString().trim();
            String fullName = binding.fullName.getText().toString().trim();
            String yearLevel = binding.yearLevel.getText().toString().trim();
            String section = binding.section.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (TextUtils.isEmpty(studentNumber) || TextUtils.isEmpty(fullName) || TextUtils.isEmpty(yearLevel) || TextUtils.isEmpty(section) || TextUtils.isEmpty(password)) {
                new AlertDialog.Builder(this)
                        .setTitle("All Fields Required")
                        .setMessage("Please fill in all the fields.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }

            if (password.length() < 8) {
                new AlertDialog.Builder(this)
                        .setTitle("Invalid Password")
                        .setMessage("Password must be at least 8 characters long.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }

            if (!binding.termsAndConditions.isChecked()) {
                new AlertDialog.Builder(this)
                        .setTitle("Terms and Conditions")
                        .setMessage("You must agree to the terms and conditions.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return;
            }

            // TODO: Implement actual registration logic
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
