package com.example.finals_attendify;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class scanner_part extends AppCompatActivity {

    private DecoratedBarcodeView barcodeView;
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_part);

        barcodeView = findViewById(R.id.barcodeScanner);

        // Check Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            barcodeView.decodeContinuous(callback);
        }
    }

    // Handle Permission Result
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

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {

            if (result.getText() != null) {

                barcodeView.pause(); // stop scanning after success


                String teacherEndTime = "09:00"; // later from Firebase

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(new Date());

                try {
                    Date end = sdf.parse(teacherEndTime);
                    Date now = sdf.parse(currentTime);

                    if (now.before(end)) {
                        showStatus("You are ON TIME!");
                    } else {
                        showStatus("You are LATE!");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void showStatus(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Attendance Status")
                .setMessage(message)
                .setPositiveButton("OK", (d, i) -> {
                    startActivity(new Intent(scanner_part.this, MainActivity_Students.class));
                    finish();
                })
                .show();
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
