package com.example.finals_attendify;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.app.Dialog;
import java.text.ParseException;
import java.util.Map;

public class employee_class_dashboard extends AppCompatActivity {

    TextView day, date, tvGreet, classLbl, sectionLbl, status;
    ImageView ivQr;
    Button createBtn, cancelBtn, backBtn, qrBtn;
    private Bitmap generatedQrBitmap = null; // Stores the generated QR

    private String endTimeStr = "";
    private final Calendar calendar = Calendar.getInstance();
    private long classExpiryTime = 0;
    private long expiryTimestamp = 0;
    private String subjectName;
    private String qrData = "";
    private FirebaseFirestore db;
    private String sessionId;
    private Dialog qrDialog;
    private final android.os.Handler statusHandler = new android.os.Handler();
    private Runnable statusRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_class_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        status = findViewById(R.id.status);
        sectionLbl = findViewById(R.id.sectionLbl);
        classLbl = findViewById(R.id.classLbl);
        tvGreet = findViewById(R.id.tvGreet);
        date = findViewById(R.id.date);
        day = findViewById(R.id.day);
        ivQr = findViewById(R.id.iv_qr);
        createBtn = findViewById(R.id.createBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        backBtn = findViewById(R.id.backBtn);
        qrBtn = findViewById(R.id.qrBtn);
        db = FirebaseFirestore.getInstance();


        // initialize date
        Date current = new Date();
        //day format
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        String dayString = dayFormat.format(current);
        //day format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String dateString = dateFormat.format(current);


        // get data from the Intent
        String subject = getIntent().getStringExtra("subjectName");
        String section = getIntent().getStringExtra("section");
        String greetText = getIntent().getStringExtra("tvGreet");

        //display data
        if (subject != null && section != null) {
            classLbl.setText(subject);
            sectionLbl.setText(section);
        }

        if (greetText != null && !greetText.isEmpty()) {
            tvGreet.setText(greetText);
        }

        expiryTimestamp = getIntent().getLongExtra("expiryTimestamp", 0);
        String qrData = getIntent().getStringExtra("qrData");

        // INITIAL CHECK: Set status when opening the activity
        if (qrData != null && System.currentTimeMillis() < expiryTimestamp) {
            status.setText("Attendance is ACTIVE");
            status.setTextColor(android.graphics.Color.GREEN);
        } else {
            status.setText("Attendance is INACTIVE");
            status.setTextColor(android.graphics.Color.RED);
        }

        //Display day and date
        day.setText(dayString);
        date.setText(dateString);

        qrBtn.setOnClickListener(v -> handleqr());

        classExpiryTime = getIntent().getLongExtra("expiryTimestamp", 0);
        String existingQr = getIntent().getStringExtra("qrData");

        db.collection("attendance_records")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("status", "PRESENT");

        db.collection("attendance_records")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("status", "LATE");

        db.collection("attendance_records")
                .whereEqualTo("sessionId", sessionId)
                .whereEqualTo("status", "ABSENT");

        startStatusUpdateLoop();
    }

    private void startStatusUpdateLoop() {
        statusRunnable = new Runnable() {
            @Override
            public void run() {
                if (expiryTimestamp != 0) {
                    if (System.currentTimeMillis() < expiryTimestamp) {
                        status.setText("Attendance is ACTIVE");
                        status.setTextColor(android.graphics.Color.GREEN);
                        // Check again in 1 second
                        statusHandler.postDelayed(this, 1000);
                    } else {
                        status.setText("Attendance is INACTIVE");
                        status.setTextColor(android.graphics.Color.RED);
                        generatedQrBitmap = null; // Clear the expired QR
                    }
                }
            }
        };
        statusHandler.post(statusRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the loop when activity is destroyed to prevent memory leaks
        if (statusHandler != null && statusRunnable != null) {
            statusHandler.removeCallbacks(statusRunnable);
        }
    }
    private void handleqr() {
        long currentTime = System.currentTimeMillis();

        // Condition: Bitmap must exist AND current time must be less than expiry
        if (generatedQrBitmap != null && currentTime < expiryTimestamp) {
            // Show the generated QR
            showQrCodeLayout();
        } else {
            // Either first time or QR expired, show input popup
            showInputPopup();
        }
    }

    private void showInputPopup() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.qrlayout);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etDate = dialog.findViewById(R.id.etDate);
        EditText etStart = dialog.findViewById(R.id.etStart);
        EditText etLate = dialog.findViewById(R.id.etLate);
        EditText etEnd = dialog.findViewById(R.id.etEnd);
        Button createBtn = dialog.findViewById(R.id.createBtn);
        Button cancelBtnInDialog = dialog.findViewById(R.id.cancelBtn);

        // Pickers
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etStart.setOnClickListener(v -> showTimePicker(etStart));
        etLate.setOnClickListener(v -> showTimePicker(etLate));
        etEnd.setOnClickListener(v -> showTimePicker(etEnd));


        cancelBtnInDialog.setOnClickListener(v -> {
            dialog.dismiss();
        });


        createBtn.setOnClickListener(v -> {
            String dateInput = etDate.getText().toString().trim();
            String startInput = etStart.getText().toString().trim();
            String lateInput = etLate.getText().toString().trim();
            String currentEndStr = etEnd.getText().toString().trim(); // Use a local variable name

            if (dateInput.isEmpty() || startInput.isEmpty() || lateInput.isEmpty() || currentEndStr.isEmpty()) {
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());

                Date selectedDate = sdfDate.parse(dateInput); // .parse needs java.util.Date

                // Normalize Today for comparison
                Calendar calToday = Calendar.getInstance();
                calToday.set(Calendar.HOUR_OF_DAY, 0);
                calToday.set(Calendar.MINUTE, 0);
                calToday.set(Calendar.SECOND, 0);
                calToday.set(Calendar.MILLISECOND, 0);
                Date todayDate = calToday.getTime();

                // --- VALIDATIONS ---
                if (selectedDate.before(todayDate)) {
                    Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedDate.equals(todayDate)) {
                    Date startTime = sdfTime.parse(startInput);
                    Calendar now = Calendar.getInstance();
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(startTime);
                    startCal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));

                    if (startCal.before(now)) {
                        Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (startInput.equals(lateInput) || startInput.equals(currentEndStr) || lateInput.equals(currentEndStr)) {
                    Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
                    return;
                }

                // --- SUCCESS ---
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(selectedDate);
                Date endTimeDate = sdfTime.parse(currentEndStr); // This is where .parse is used
                Calendar timeParts = Calendar.getInstance();
                timeParts.setTime(endTimeDate);

                endCal.set(Calendar.HOUR_OF_DAY, timeParts.get(Calendar.HOUR_OF_DAY));
                endCal.set(Calendar.MINUTE, timeParts.get(Calendar.MINUTE));

                // Update Global Variables (Class scope)
                expiryTimestamp = endCal.getTimeInMillis();

                // for sessionId format: classCode_date_time
                String subjectCode = classLbl.getText().toString();

                SimpleDateFormat sdfDateId = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                SimpleDateFormat sdfTimeId = new SimpleDateFormat("HHmm", Locale.getDefault());

                String idDate = sdfDateId.format(new Date());
                String idTime = sdfTimeId.format(new Date());

                sessionId = subjectCode + "_" + idDate + "_" + idTime;
                db.collection("employee")
                        .document("20260113")
                        .get()
                        .addOnSuccessListener(empDoc -> {

                            if (!empDoc.exists()) {
                                Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String employeeName = empDoc.getString("name");

                            Map<String, Object> session = new HashMap<>();
                            session.put("subjectId", classLbl.getText().toString());
                            session.put("startTime", startInput);
                            session.put("lateTime", lateInput);
                            session.put("endTime", currentEndStr);
                            session.put("status", "ACTIVE");
                            session.put("createdBy", employeeName);

                            db.collection("attendance_sessions")
                                    .document(sessionId)
                                    .set(session)
                                    .addOnSuccessListener(unused -> {
                                        qrData = sessionId;
                                        generatedQrBitmap = generateQRCode(sessionId);
                                        startStatusUpdateLoop();
                                        dialog.dismiss();
                                        showQrCodeLayout();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to create session", Toast.LENGTH_SHORT).show()
                                    );
                        });

                statusHandler.removeCallbacks(statusRunnable);
                startStatusUpdateLoop();

                dialog.dismiss();
                showQrCodeLayout();

            } catch (ParseException e) {
                // This handles the "Red Parse" error if the date format is wrong
                Toast.makeText(this, "Invalid Input", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedExpiry", expiryTimestamp);
        resultIntent.putExtra("updatedQr", qrData);
        resultIntent.putExtra("subjectName", subjectName); // To identify which item changed
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showQrCodeLayout() {
        // 1. Create the Dialog instance
        final Dialog qrDialog = new Dialog(this);

        // 2. Set the content to your specific XML layout
        qrDialog.setContentView(R.layout.qrcodeshow);

        // 3. Make the dialog background transparent
        // This ensures that if your qrcodeshow.xml has rounded corners (CardView),
        // you won't see a white square behind it.
        if (qrDialog.getWindow() != null) {
            qrDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // 4. Initialize the views from INSIDE the dialog
        // You MUST use qrDialog.findViewById, not just findViewById
        ImageView iv_qr = qrDialog.findViewById(R.id.iv_qr);
        Button backBtn = qrDialog.findViewById(R.id.backBtn);

        // 5. Display the generated QR bitmap
        if (generatedQrBitmap != null) {
            iv_qr.setImageBitmap(generatedQrBitmap);
        }

        // 6. Set the button to close the popup
        backBtn.setOnClickListener(v -> {
            qrDialog.dismiss(); // This closes the popup and reveals the dashboard again
        });

        // 7. Show the popup
        qrDialog.show();
    }

    private boolean isBeforeEndTime(String endTime) {
        // Implement time comparison logic here
        // Return true if current time < endTime
        return true;
    }

    private void rebindMainButtons() {
        Button btn7 = findViewById(R.id.qrBtn);
        btn7.setOnClickListener(v -> handleqr());
    }


    private void showMainLayout() {
        setContentView(R.layout.activity_employee_class_dashboard); //
        Button btn7 = findViewById(R.id.qrBtn); //
        btn7.setOnClickListener(v -> {
            if (generatedQrBitmap != null && !isTimeExpired(endTimeStr)) {
                showQrCodeShowLayout();
            } else {
                showQrLayout();
            }
        });
    }

    private void showQrLayout() {
        setContentView(R.layout.qrlayout);

        EditText etDate = findViewById(R.id.etDate);
        EditText etStart = findViewById(R.id.etStart);
        EditText etLate = findViewById(R.id.etLate);
        EditText etEnd = findViewById(R.id.etEnd);
        Button crtBtn = findViewById(R.id.createBtn);
        Button CancelBtn = findViewById(R.id.cancelBtn);

        // Set up Pickers for the EditText fields
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etStart.setOnClickListener(v -> showTimePicker(etStart));
        etLate.setOnClickListener(v -> showTimePicker(etLate));
        etEnd.setOnClickListener(v -> showTimePicker(etEnd));

        CancelBtn.setOnClickListener(v -> showMainLayout());

        crtBtn.setOnClickListener(v -> {
            String date = etDate.getText().toString();
            String start = etStart.getText().toString();
            endTimeStr = etEnd.getText().toString();

            if (date.isEmpty() || start.isEmpty() || endTimeStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate the QR based on the input data
            generatedQrBitmap = generateQRCode(date + " | Start: " + start);
            showQrCodeShowLayout();
        });
    }

    private void showQrCodeShowLayout() {
        setContentView(R.layout.qrcodeshow);

        ImageView ivQr = findViewById(R.id.iv_qr);
        Button btnBack = findViewById(R.id.backBtn);

        ivQr.setImageBitmap(generatedQrBitmap);

        btnBack.setOnClickListener(v -> {
            // Go back to ClassMo, but QR is saved in 'generatedQrBitmap'
            setContentView(R.layout.activity_employee_class_dashboard);
            rebindMainButtons();
        });
    }

    private void showDatePicker(EditText field) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            field.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Helper to show Time Picker
    private void showTimePicker(EditText field) {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            // "hh:mm a" is required for the validation logic to work
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            field.setText(sdf.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private Bitmap generateQRCode(String text) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            com.google.zxing.common.BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isTimeExpired(String endTime) {
        if (endTime.isEmpty()) return true;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTime = sdf.format(Calendar.getInstance().getTime());
            return sdf.parse(currentTime).after(sdf.parse(endTime));
        } catch (Exception e) {
            return true;
        }
    }
}