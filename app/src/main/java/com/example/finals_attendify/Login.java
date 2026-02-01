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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    private ActivityLoginBinding binding;


    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private RadioButton studentRB, teacherRB;
    private Drawable whitePill;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();      // students
        db = FirebaseFirestore.getInstance();  // employees

        studentRB = binding.studentRadioButton;
        teacherRB = binding.teacherRadioButton;

        whitePill = ContextCompat.getDrawable(this, R.drawable.rbwhitebg);

        setupRadioGroup();
        setupLogin();
        setupInsets();

        binding.backArrow.setOnClickListener(v -> finish());
    }

    //--------------------------
    private void setupRadioGroup() {

        RadioGroup group = binding.userType;

        updateSelection(group.getCheckedRadioButtonId());

        group.setOnCheckedChangeListener((g, checkedId) -> {
            updateSelection(checkedId);

            if (checkedId == R.id.studentRadioButton) {
                binding.employeeNumberLabel.setText("Student Number");
            } else {
                binding.employeeNumberLabel.setText("Employee Number");
            }
        });
    }

    private void updateSelection(int checkedId) {
        studentRB.setBackground(null);
        teacherRB.setBackground(null);

        if (checkedId == R.id.studentRadioButton) {
            studentRB.setBackground(whitePill);
        } else {
            teacherRB.setBackground(whitePill);
        }
    }

    //---------------------------
    private void setupLogin() {

        binding.loginButton.setOnClickListener(v -> {

            String userNumber = binding.employeeNumber.getText().toString().trim();
            String password = binding.password.getText().toString().trim();

            if (TextUtils.isEmpty(userNumber) || TextUtils.isEmpty(password)) {
                toast("All fields are required");
                return;
            }

            if (!binding.termsAndConditions.isChecked()) {
                toast("You must agree to the terms");
                return;
            }

            // STUDENT
            if (studentRB.isChecked()) {

                String email = userNumber + "@attendify.com";

                auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {
                            startActivity(new Intent(this, MainActivity_Students.class));
                            finish();
                        })
                        .addOnFailureListener(e ->
                                toast("Login failed: " + e.getMessage())
                        );
            }
            // EMPLOYEE
            else {

                db.collection("employee") // collection
                        .document(userNumber)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> handleEmployeeLogin(documentSnapshot, password))
                        .addOnFailureListener(e ->
                                toast("Login failed: " + e.getMessage())
                        );
            }
        });
    }

    //EMPLOYEE CHECK-------------------------
    private void handleEmployeeLogin(DocumentSnapshot doc, String inputPassword) {

        if (!doc.exists()) {
            toast("Employee ID not found");
            return;
        }

        String dbPassword = doc.getString("password");

        if (dbPassword != null && dbPassword.equals(inputPassword)) {

            toast("Login successful");

            // ✅ Pass employeeId to MainActivity_Employee
            Intent intent = new Intent(this, MainActivity_Employee.class);
            intent.putExtra("employeeId", doc.getId()); // Pass the Firestore document ID
            startActivity(intent);
            finish();

        } else {
            toast("Incorrect password");
        }
    }


    //------------------------
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }
}
