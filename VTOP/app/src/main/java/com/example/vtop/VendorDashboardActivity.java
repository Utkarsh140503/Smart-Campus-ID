package com.example.vtop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class VendorDashboardActivity extends AppCompatActivity {

    private TextView textRFID;
    private EditText editTotalAmount;
    private Button buttonSubmit;
    private ListView listPaymentHistory;

    private DatabaseReference databaseReference;
    private String vendorID, shopName;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> paymentHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_dashboard);

        // Initialize views
        textRFID = findViewById(R.id.textRFID);
        editTotalAmount = findViewById(R.id.editTotalAmount);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        listPaymentHistory = findViewById(R.id.listPaymentHistory);

        // Retrieve vendor details from intent
        vendorID = getIntent().getStringExtra("vendorID");
        shopName = getIntent().getStringExtra("shopName");

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize ListView adapter
        paymentHistoryList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, paymentHistoryList);
        listPaymentHistory.setAdapter(adapter);

        // Fetch current RFID value
        fetchCurrentRFID();

        // Fetch payment history
        fetchPaymentHistory();

        // Handle submit button click
        buttonSubmit.setOnClickListener(v -> {
            String totalAmount = editTotalAmount.getText().toString().trim();

            if (totalAmount.isEmpty()) {
                Toast.makeText(VendorDashboardActivity.this, "Please enter a total amount", Toast.LENGTH_SHORT).show();
            } else {
                checkSufficientCreditsAndProceed(totalAmount);
            }
        });
    }

    // Fetch the current RFID from Firebase
    private void fetchCurrentRFID() {
        databaseReference.child("PaymentRFID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String rfid = snapshot.getValue(String.class);
                    textRFID.setText("Current Payment RFID: " + rfid);
                } else {
                    textRFID.setText("Current Payment RFID: Not Available");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorDashboardActivity.this, "Failed to fetch RFID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch payment history for the current vendor
    private void fetchPaymentHistory() {
        databaseReference.child("PaymentHistory").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentHistoryList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> paymentData = (Map<String, Object>) data.getValue();

                    if (paymentData != null && vendorID.equals(paymentData.get("vendorID"))) {
                        String dateTime = data.getKey();
                        String rfid = (String) paymentData.get("rfidID");
                        String amount = (String) paymentData.get("totalAmount");

                        paymentHistoryList.add("Date: " + dateTime + "\nRFID: " + rfid + "\nAmount: " + amount);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorDashboardActivity.this, "Failed to fetch payment history", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check sufficient credits before proceeding
    private void checkSufficientCreditsAndProceed(String totalAmount) {
        String currentRFID = textRFID.getText().toString().replace("Current Payment RFID: ", "").trim();

        if (currentRFID.equals("Not Available")) {
            Toast.makeText(this, "No RFID available to process payment", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child("Student_Registrations").orderByChild("rfid").equalTo(currentRFID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                                int currentCredits = studentSnapshot.child("credits").getValue(Integer.class);
                                int requiredAmount = Integer.parseInt(totalAmount);

                                if (currentCredits >= requiredAmount) {
                                    showOTPDialog(currentRFID, totalAmount, studentSnapshot.getKey(), currentCredits, requiredAmount);
                                } else {
                                    Toast.makeText(VendorDashboardActivity.this, "Insufficient credits to process the payment", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(VendorDashboardActivity.this, "No student record found for the given RFID", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VendorDashboardActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Show OTP dialog
    private void showOTPDialog(String rfid, String totalAmount, String studentRegKey, int currentCredits, int requiredAmount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verify OTP");

        // Add an EditText for entering OTP
        final EditText input = new EditText(this);
        input.setHint("Enter OTP");
        builder.setView(input);

        builder.setPositiveButton("Verify", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button verifyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            verifyButton.setOnClickListener(view -> {
                String enteredOTP = input.getText().toString().trim();

                if (enteredOTP.isEmpty()) {
                    Toast.makeText(VendorDashboardActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                } else {
                    verifyOTP(rfid, enteredOTP, totalAmount, studentRegKey, currentCredits, requiredAmount, dialog);
                }
            });
        });

        dialog.show();
    }

    // Verify the entered OTP with Firebase
    private void verifyOTP(String rfid, String enteredOTP, String totalAmount, String studentRegKey, int currentCredits, int requiredAmount, AlertDialog dialog) {
        databaseReference.child("OTPs").child(rfid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String correctOTP = snapshot.getValue(String.class);

                    if (correctOTP != null && correctOTP.equals(enteredOTP)) {
                        dialog.dismiss();
                        updateStudentCreditsAndSubmitPayment(rfid, totalAmount, studentRegKey, currentCredits, requiredAmount);
                    } else {
                        Toast.makeText(VendorDashboardActivity.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VendorDashboardActivity.this, "No OTP found for the current RFID", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(VendorDashboardActivity.this, "Failed to verify OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Update student credits and submit payment
    private void updateStudentCreditsAndSubmitPayment(String rfid, String totalAmount, String studentRegKey, int currentCredits, int requiredAmount) {
        int updatedCredits = currentCredits - requiredAmount;

        databaseReference.child("Student_Registrations").child(studentRegKey).child("credits")
                .setValue(updatedCredits)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        submitPayment(rfid, totalAmount);
                    } else {
                        Toast.makeText(VendorDashboardActivity.this, "Failed to update credits", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Submit the payment details to Firebase
    private void submitPayment(String rfid, String totalAmount) {
        String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        // Create a new payment record
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("vendorID", vendorID);
        paymentData.put("shopName", shopName);
        paymentData.put("rfidID", rfid);
        paymentData.put("totalAmount", totalAmount);

        databaseReference.child("PaymentHistory").child(currentDateTime).setValue(paymentData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VendorDashboardActivity.this, "Payment successfully submitted", Toast.LENGTH_SHORT).show();
                        editTotalAmount.setText("");
                    } else {
                        Toast.makeText(VendorDashboardActivity.this, "Failed to submit payment", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
