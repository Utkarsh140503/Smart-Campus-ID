package com.example.vtop;

import android.os.Bundle;
import android.widget.ArrayAdapter;
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

import java.util.ArrayList;
import java.util.Map;

public class StudentPaymentHistory extends AppCompatActivity {

    private TextView textStudentRFID;
    private ListView listPaymentHistory;

    private DatabaseReference databaseReference;
    private String studentRegNumber;
    private String studentRFID;

    private ArrayAdapter<String> adapter;
    private ArrayList<String> paymentHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_payment_history);

        // Retrieve registration number from Intent
        studentRegNumber = getIntent().getStringExtra("UserID");

        // Initialize views
        textStudentRFID = findViewById(R.id.textStudentRFID);
        listPaymentHistory = findViewById(R.id.listPaymentHistory);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize ListView adapter
        paymentHistoryList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, paymentHistoryList);
        listPaymentHistory.setAdapter(adapter);

        // Fetch student RFID and payment history
        fetchStudentRFID();
    }

    // Fetch student RFID from Firebase
    private void fetchStudentRFID() {
        databaseReference.child("Student_Registrations").child(studentRegNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Map<String, Object> studentData = (Map<String, Object>) snapshot.getValue();

                            if (studentData != null && studentData.containsKey("rfid")) {
                                studentRFID = studentData.get("rfid").toString();
                                textStudentRFID.setText("RFID: " + studentRFID);

                                // Fetch payment history using the RFID
                                fetchPaymentHistory(studentRFID);
                            } else {
                                Toast.makeText(StudentPaymentHistory.this, "RFID not found for the student", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StudentPaymentHistory.this, "Student not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentPaymentHistory.this, "Error fetching RFID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Fetch payment history for the student
    private void fetchPaymentHistory(String rfid) {
        databaseReference.child("PaymentHistory").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentHistoryList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Map<String, Object> paymentData = (Map<String, Object>) data.getValue();

                    if (paymentData != null && rfid.equals(paymentData.get("rfidID"))) {
                        String dateTime = data.getKey();
                        String vendorID = (String) paymentData.get("vendorID");
                        String storeName = (String) paymentData.get("shopName");
                        String amount = (String) paymentData.get("totalAmount");

                        paymentHistoryList.add("Date: " + dateTime +
                                "\nVendor: " + vendorID +
                                "\nStore: " + storeName +
                                "\nAmount: " + amount);
                    }
                }

                if (paymentHistoryList.isEmpty()) {
                    paymentHistoryList.add("No payment history found for this RFID.");
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentPaymentHistory.this, "Error fetching payment history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
