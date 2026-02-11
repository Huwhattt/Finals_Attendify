package com.example.finals_attendify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class scanner_part extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private static final int CAMERA_PERMISSION_REQUEST = 101;
    private FirebaseFirestore db; // Add Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_masterlist); // NOTE: Ensure this layout matches your activity

        barcodeView = findViewById(R.id.barcodeScanner);
        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST
            );
        } else {
            barcodeView.decodeContinuous(callback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                barcodeView.decodeContinuous(callback);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Camera Required")
                        .setMessage("Camera permission is required to scan QR codes.")
                        .setPositiveButton("OK", (d, i) -> finish())
                        .show();
            }
        }
    }

    private final BarcodeCallback callback = result -> {
        if (result.getText() == null) return;

        barcodeView.pause();
        String scannedText = result.getText(); // This should be the student ID

        // --- NEW LOGIC: CHECK IF STUDENT IS SUSPENDED ---
        db.collection("attendance_records")
                .document(scannedText) // Assuming document ID is the student number
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean isSuspended = documentSnapshot.getBoolean("suspended");

                        if (isSuspended != null && isSuspended) {
                            // Student is suspended, block access
                            new AlertDialog.Builder(this)
                                    .setTitle("Access Denied")
                                    .setMessage("This student is suspended and cannot scan.")
                                    .setPositiveButton("OK", (d, i) -> barcodeView.resume()) // Resume scanning
                                    .show();
                        } else {
                            // Student is active, proceed
                            proceedToMain(scannedText);
                        }
                    } else {
                        // Document doesn't exist, handle accordingly (maybe student isn't registered)
                        Toast.makeText(this, "Student not found in records.", Toast.LENGTH_SHORT).show();
                        barcodeView.resume();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    barcodeView.resume();
                });
    };

    private void proceedToMain(String sessionId) {
        Intent intent = new Intent(scanner_part.this, MainActivity_Students.class);
        intent.putExtra("SESSION_ID", sessionId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }
}