package com.example.finals_attendify;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finals_attendify.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;

    private RadioButton studentRB, teacherRB;
    private Drawable whitePill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        studentRB = binding.studentRadioButton;
        teacherRB = binding.teacherRadioButton;
        RadioGroup radioGroup = binding.userType;

        whitePill = ContextCompat.getDrawable(this, R.drawable.rbwhitebg);

        updateSelection(radioGroup.getCheckedRadioButtonId());

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId != -1) {
                    updateSelection(checkedId);

                    // Also update the label
                    if (checkedId == R.id.studentRadioButton) {
                        binding.employeeNumberLabel.setText("Student Number");
                    } else {
                        binding.employeeNumberLabel.setText("Employee Number");
                    }
                }
            }
        });

        binding.backArrow.setOnClickListener(v -> finish());

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

            String email;
            if (studentRB.isChecked()) {
                email = userNumber + "@attendify.com";
            } else {
                email = userNumber + "@attendify.com";
            }

            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        if (studentRB.isChecked()) {
                            startActivity(new Intent(Login.this, MainActivity_Students.class));
                        } else {
                            startActivity(new Intent(Login.this, MainActivity_Employee.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(Login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateSelection(int checkedId) {
        studentRB.setBackground(null);
        teacherRB.setBackground(null);

        if (checkedId == R.id.studentRadioButton) {
            studentRB.setBackground(whitePill);
        } else if (checkedId == R.id.teacherRadioButton) {
            teacherRB.setBackground(whitePill);
        }
    }
}