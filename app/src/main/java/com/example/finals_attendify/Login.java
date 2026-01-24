package com.example.finals_attendify;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finals_attendify.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.backArrow.setOnClickListener(v -> finish());

        binding.userType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.studentRadioButton) {
                binding.employeeNumberLabel.setText("Student Number");
            } else {
                binding.employeeNumberLabel.setText("Employee Number");
            }
        });

        binding.loginButton.setOnClickListener(v -> {
            String userNumber = binding.employeeNumber.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (TextUtils.isEmpty(userNumber) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!binding.termsAndConditions.isChecked()) {
                Toast.makeText(Login.this, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bypassing authentication for demonstration.
            // In a real app, you would validate credentials against a backend.
            if (binding.studentRadioButton.isChecked()) {
                startActivity(new Intent(Login.this, MainActivity_Students.class));
                finish();
            } else {
                startActivity(new Intent(Login.this, MainActivity_Employee.class));
                finish();
            }
        });
    }
}
