package com.example.finals_attendify;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class employee_class_dashboard extends AppCompatActivity {


    private TextView dayTxt, dateTxt, greetTxt, classLbl, sectionLbl, statusTxt;
    private ImageView ivQr, studentBtn;
    private Button qrBtn;


    private Bitmap generatedQrBitmap;
    private String sessionId = "";
    private long expiryTimestamp = 0;


    private final Calendar calendar = Calendar.getInstance();
    private final android.os.Handler statusHandler = new android.os.Handler();
    private Runnable statusRunnable;


    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_class_dashboard);


        // Handle system bars padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom);
            return insets;
        });


        initViews();
        setupInitialData();
        setupButtons();


        startStatusUpdateLoop();
    }


    private void initViews() {
        dayTxt = findViewById(R.id.day);
        dateTxt = findViewById(R.id.date);
        greetTxt = findViewById(R.id.tvGreet);
        classLbl = findViewById(R.id.classLbl);
        sectionLbl = findViewById(R.id.sectionLbl);
        statusTxt = findViewById(R.id.status);
        ivQr = findViewById(R.id.iv_qr);
        qrBtn = findViewById(R.id.qrBtn);
        studentBtn = findViewById(R.id.studentbutton);


        db = FirebaseFirestore.getInstance();
    }


    private void setupInitialData() {
        // Display day and date
        Date now = new Date();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        dayTxt.setText(dayFormat.format(now));
        dateTxt.setText(dateFormat.format(now));


        // Get data from Intent
        String subject = getIntent().getStringExtra("subjectName");
        String section = getIntent().getStringExtra("section");
        String greetText = getIntent().getStringExtra("tvGreet");
        expiryTimestamp = getIntent().getLongExtra("expiryTimestamp", 0);
        sessionId = getIntent().getStringExtra("qrData");


        if (subject != null) classLbl.setText(subject);
        if (section != null) sectionLbl.setText(section);
        if (greetText != null) greetTxt.setText(greetText);


        updateStatusText();
    }


    private void setupButtons() {
        studentBtn.setOnClickListener(v -> {
            if (sessionId.isEmpty()) {
                Toast.makeText(this, "No active session yet", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, ClassMasterlistActivity.class);
            i.putExtra("subject", classLbl.getText().toString());
            i.putExtra("sessionId", sessionId);
            startActivity(i);
        });


        qrBtn.setOnClickListener(v -> handleQrButton());
    }


    private void handleQrButton() {
        if (generatedQrBitmap != null && System.currentTimeMillis() < expiryTimestamp) {
            showQrDialog();
        } else {
            showQrInputDialog();
        }
    }


    private void showQrDialog() {
        Dialog qrDialog = new Dialog(this);
        qrDialog.setContentView(R.layout.qrcodeshow);
        if (qrDialog.getWindow() != null) {
            qrDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }


        ImageView qrImage = qrDialog.findViewById(R.id.iv_qr);
        Button backBtn = qrDialog.findViewById(R.id.backBtn);


        if (generatedQrBitmap != null) qrImage.setImageBitmap(generatedQrBitmap);


        backBtn.setOnClickListener(v -> qrDialog.dismiss());


        qrDialog.show();
    }


    private void showQrInputDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.qrlayout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }


        EditText etDate = dialog.findViewById(R.id.etDate);
        EditText etStart = dialog.findViewById(R.id.etStart);
        EditText etLate = dialog.findViewById(R.id.etLate);
        EditText etEnd = dialog.findViewById(R.id.etEnd);
        Button createBtn = dialog.findViewById(R.id.createBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);


        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etStart.setOnClickListener(v -> showTimePicker(etStart));
        etLate.setOnClickListener(v -> showTimePicker(etLate));
        etEnd.setOnClickListener(v -> showTimePicker(etEnd));


        cancelBtn.setOnClickListener(v -> dialog.dismiss());


        createBtn.setOnClickListener(v -> {
            String dateInput = etDate.getText().toString().trim();
            String startInput = etStart.getText().toString().trim();
            String lateInput = etLate.getText().toString().trim();
            String endInput = etEnd.getText().toString().trim();


            if (dateInput.isEmpty() || startInput.isEmpty() || lateInput.isEmpty() || endInput.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }


            if (startInput.equals(lateInput) || startInput.equals(endInput) || lateInput.equals(endInput)) {
                Toast.makeText(this, "Time fields cannot be the same", Toast.LENGTH_SHORT).show();
                return;
            }


            try {
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat sdfTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date selectedDate = sdfDate.parse(dateInput);
                Date endTime = sdfTime.parse(endInput);


                // Validate date is today or future
                Calendar calToday = Calendar.getInstance();
                calToday.set(Calendar.HOUR_OF_DAY, 0);
                calToday.set(Calendar.MINUTE, 0);
                calToday.set(Calendar.SECOND, 0);
                calToday.set(Calendar.MILLISECOND, 0);
                if (selectedDate.before(calToday.getTime())) {
                    Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show();
                    return;
                }


                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(selectedDate);
                Calendar timeParts = Calendar.getInstance();
                timeParts.setTime(endTime);
                calEnd.set(Calendar.HOUR_OF_DAY, timeParts.get(Calendar.HOUR_OF_DAY));
                calEnd.set(Calendar.MINUTE, timeParts.get(Calendar.MINUTE));
                expiryTimestamp = calEnd.getTimeInMillis();


                // Generate sessionId
                String subjectCode = classLbl.getText().toString();
                String idDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                String idTime = new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date());
                sessionId = subjectCode + "_" + idDate + "_" + idTime;


                // Save to Firestore
                Map<String, Object> session = new HashMap<>();
                session.put("subjectId", classLbl.getText().toString());
                session.put("startTime", startInput);
                session.put("lateTime", lateInput);
                session.put("endTime", endInput);
                session.put("status", "ACTIVE");
                session.put("createdBy", "EMPLOYEE");


                db.collection("attendance_sessions").document(sessionId)
                        .set(session)
                        .addOnSuccessListener(unused -> {
                            generatedQrBitmap = generateQRCode(sessionId);
                            updateStatusText();
                            dialog.dismiss();
                            showQrDialog();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to create session", Toast.LENGTH_SHORT).show());


            } catch (ParseException e) {
                Toast.makeText(this, "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();
    }


    private void showDatePicker(EditText field) {
        new DatePickerDialog(this, (view, year, month, day) -> {
            calendar.set(year, month, day);
            field.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }


    private void showTimePicker(EditText field) {
        new TimePickerDialog(this, (view, hour, minute) -> {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            field.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }


    private Bitmap generateQRCode(String text) {
        MultiFormatWriter writer = new MultiFormatWriter();
        try {
            com.google.zxing.common.BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 500, 500);
            BarcodeEncoder encoder = new BarcodeEncoder();
            return encoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void startStatusUpdateLoop() {
        statusRunnable = new Runnable() {
            @Override
            public void run() {
                updateStatusText();
                statusHandler.postDelayed(this, 1000);
            }
        };
        statusHandler.post(statusRunnable);
    }


    private void updateStatusText() {
        if (expiryTimestamp != 0 && System.currentTimeMillis() < expiryTimestamp) {
            statusTxt.setText("Attendance is ACTIVE");
            statusTxt.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            statusTxt.setText("Attendance is INACTIVE");
            statusTxt.setTextColor(getColor(android.R.color.holo_red_dark));
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statusHandler != null && statusRunnable != null) {
            statusHandler.removeCallbacks(statusRunnable);
        }
    }


    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("updatedExpiry", expiryTimestamp);
        resultIntent.putExtra("updatedQr", sessionId);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}
