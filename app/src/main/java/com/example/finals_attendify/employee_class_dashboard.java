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
import java.util.Locale;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import android.app.Dialog;

public class employee_class_dashboard extends AppCompatActivity {

    TextView day, date, tvGreet, classLbl, sectionLbl;
    ImageView ivQr;
    Button createBtn, cancelBtn, backBtn, qrBtn;
    private Bitmap generatedQrBitmap = null; // Stores the generated QR
    private String endTimeValue = "";
    private String endTimeStr = "";
    private final Calendar calendar = Calendar.getInstance();


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

        sectionLbl = findViewById(R.id.sectionLbl);
        classLbl = findViewById(R.id.classLbl);
        date = findViewById(R.id.date);
        tvGreet = findViewById(R.id.tvGreet);
        day = findViewById(R.id.day);
        ivQr = findViewById(R.id.iv_qr);
        createBtn = findViewById(R.id.createBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        backBtn = findViewById(R.id.backBtn);
        qrBtn = findViewById(R.id.qrBtn);





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

        //display data
        if (subject != null && section != null) {
            classLbl.setText(subject);
            sectionLbl.setText(section);
        }

        //Display day and date
        day.setText(dayString);
        date.setText(dateString);

        qrBtn.setOnClickListener(v -> handleqr());
    }

    private void handleqr() {
        // Check if QR exists and if the current time is before etEnd

        if (generatedQrBitmap != null && !isTimeExpired(endTimeStr)) {
            showQrCodeLayout(); // Show the QR display screen
        } else {
            showInputPopup(); // Show the popup to create a new one
        }
    }

    private void showInputPopup() {
        // Create the dialog
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.qrlayout);

        // Make the dialog background transparent so the CardView corners look rounded
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views inside the popup
        EditText etDate = dialog.findViewById(R.id.etDate);
        EditText etStart = dialog.findViewById(R.id.etStart);
        EditText etLate = dialog.findViewById(R.id.etLate);
        EditText etEnd = dialog.findViewById(R.id.etEnd);
        Button createBtn = dialog.findViewById(R.id.createBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

        // Set up Pickers
        etDate.setOnClickListener(v -> showDatePicker(etDate));
        etStart.setOnClickListener(v -> showTimePicker(etStart));
        etLate.setOnClickListener(v -> showTimePicker(etLate));
        etEnd.setOnClickListener(v -> showTimePicker(etEnd));

        // Cancel Button dismisses the popup
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Create Button generates QR and moves to the next screen
        createBtn.setOnClickListener(v -> {
            String date = etDate.getText().toString();
            String start = etStart.getText().toString();
            endTimeStr = etEnd.getText().toString();

            if (date.isEmpty() || start.isEmpty() || endTimeStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Generate the QR
            generatedQrBitmap = generateQRCode("Date: " + date + " | Start: " + start);

            dialog.dismiss(); // Close the popup
            showQrCodeLayout(); // Show the layout_show_qr screen
        });

        dialog.show();
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

            // "hh:mm a" produces "01:30 PM"
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            field.setText(sdf12.format(calendar.getTime()));
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show(); // false = 12hr format
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
            // Change "HH:mm" to "hh:mm a" to match your picker
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String currentTime = sdf.format(Calendar.getInstance().getTime());
            return sdf.parse(currentTime).after(sdf.parse(endTime));
        } catch (Exception e) {
            return true;
        }
    }
}