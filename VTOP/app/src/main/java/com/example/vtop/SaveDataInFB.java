package com.example.vtop;

import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class SaveDataInFB extends AppCompatActivity {

    private Spinner paymentSpinner, attendanceSpinner, leaveSpinner;
    private Button paymentOkButton, attendanceOkButton, leaveOkButton;

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_data_in_fb);

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        // Initialize UI components
        paymentSpinner = findViewById(R.id.payment_spinner);
        attendanceSpinner = findViewById(R.id.attendance_spinner);
        leaveSpinner = findViewById(R.id.leave_spinner);

        paymentOkButton = findViewById(R.id.payment_ok_button);
        attendanceOkButton = findViewById(R.id.attendance_ok_button);
        leaveOkButton = findViewById(R.id.leave_ok_button);

        // Spinner values
        String[] rfidValues = {
                "0003880758",
                "0010017441",
                "0010032123",
                "4288446101"
        };

        // Set up spinner adapters
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                rfidValues
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        paymentSpinner.setAdapter(adapter);
        attendanceSpinner.setAdapter(adapter);
        leaveSpinner.setAdapter(adapter);

        // Attendance OK button logic
        attendanceOkButton.setOnClickListener(v -> {
            String selectedValue = attendanceSpinner.getSelectedItem().toString();
            String currentDateTime = getCurrentDateTime();

            DatabaseReference attendanceRef = database.getReference("AttendanceRFID");
            DatabaseReference historyRef = database.getReference("AttendanceRFIDHistory").child(selectedValue);

            attendanceRef.setValue(selectedValue).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SaveDataInFB.this, "Attendance RFID saved successfully!", Toast.LENGTH_SHORT).show();
                    historyRef.child(currentDateTime).setValue(currentDateTime);
                } else {
                    Toast.makeText(SaveDataInFB.this, "Failed to save Attendance RFID!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Leave OK button logic
        leaveOkButton.setOnClickListener(v -> {
            String selectedValue = leaveSpinner.getSelectedItem().toString();
            String currentDateTime = getCurrentDateTime();
            String currentDate = getCurrentDate();

            DatabaseReference leaveRef = database.getReference("LeaveRFID");
            DatabaseReference leaveManagementRef = database.getReference("LeaveManagement").child(selectedValue);

            leaveRef.setValue(selectedValue).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    leaveManagementRef.get().addOnCompleteListener(snapshotTask -> {
                        if (snapshotTask.isSuccessful() && snapshotTask.getResult() != null) {
                            Long count = snapshotTask.getResult().child("count").getValue(Long.class);
                            if (count == null) count = 1L;

                            boolean isCheckedIn = count % 2 == 0;
                            HashMap<String, Object> updateMap = new HashMap<>();

                            updateMap.put("checkedIn", isCheckedIn);
                            updateMap.put("checkedOut", !isCheckedIn);
                            updateMap.put("count", count + 1);

                            if (isCheckedIn) {
                                updateMap.put("checkedInTime", currentDateTime);
                                updateMap.put("checkedInDate", currentDate);
                            } else {
                                updateMap.put("checkedOutTime", currentDateTime);
                                updateMap.put("checkedOutDate", currentDate);
                            }

                            updateMap.put("Notification", false);

                            leaveManagementRef.updateChildren(updateMap).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(SaveDataInFB.this, "Leave RFID updated successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SaveDataInFB.this, "Failed to update Leave RFID!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(SaveDataInFB.this, "Failed to save Leave RFID!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Payment OK button logic
        paymentOkButton.setOnClickListener(v -> {
            String selectedRFID = paymentSpinner.getSelectedItem().toString();
            DatabaseReference studentRef = database.getReference("Student_Registrations");
            DatabaseReference paymentRef = database.getReference("PaymentRFID");
            paymentRef.setValue(selectedRFID).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(SaveDataInFB.this, "Payment RFID saved successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SaveDataInFB.this, "Failed to save Payment RFID!", Toast.LENGTH_SHORT).show();
                }
            });
            studentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DataSnapshot snapshot = task.getResult();
                    boolean found = false;

                    for (DataSnapshot student : snapshot.getChildren()) {
                        String rfid = student.child("rfid").getValue(String.class);
                        if (selectedRFID.equals(rfid)) {
                            String regNumber = student.child("regNumber").getValue(String.class);
                            String mobileNum = student.child("mobileNum").getValue(String.class);

                            if (regNumber != null && mobileNum != null) {
                                found = true;
                                String otp = generateOTP();
                                sendSMS(mobileNum, otp);

                                DatabaseReference otpRef = database.getReference("OTPs").child(rfid);
                                otpRef.setValue(otp).addOnCompleteListener(otpTask -> {
                                    if (otpTask.isSuccessful()) {
                                        Toast.makeText(SaveDataInFB.this, "OTP sent and saved successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SaveDataInFB.this, "Failed to save OTP!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            }
                        }
                    }

                    if (!found) {
                        Toast.makeText(SaveDataInFB.this, "RFID not found in database!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(SaveDataInFB.this, "Error retrieving data from Firebase!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendSMS(String mobileNum, String otp) {
        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();

        // Set the phone number and message to send
        String message = "Your OTP is: " + otp;

        // Send the message
        try {
            smsManager.sendTextMessage(mobileNum, null, message, null, null);
            Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
