package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentDashboard extends AppCompatActivity {

    private TextView welcomeTextView, studentInfoBanner, studentInfoTextView;
    private GridView optionsGridView;
    private DatabaseReference databaseReference;
    String proctorEID, proctorName, rfid,studentRegNumber;

    private final String[] options = {
            "Credit Requests",
            "Attendance RFID History",
            "Student Attendance",
            "Payment History"
    };

    private final int[] icons = {
            R.drawable.iocnone,
            R.drawable.rfidattendance,
            R.drawable.attendanceicon,
            R.drawable.transactionicon
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Bind views
        welcomeTextView = findViewById(R.id.welcomeTextView);
        studentInfoBanner = findViewById(R.id.studentInfoBanner);
        studentInfoTextView = findViewById(R.id.studentInfoTextView);
        optionsGridView = findViewById(R.id.optionsGridView);

        // Get the student registration number from the intent
        studentRegNumber = getIntent().getStringExtra("studentRegNumber");

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Student_Registrations");

        if (studentRegNumber != null) {
            // Fetch student data from Firebase
            fetchStudentData(studentRegNumber);
        } else {
            Toast.makeText(this, "No Student Registration Number provided", Toast.LENGTH_SHORT).show();
        }

        // Set up GridView
        OptionsGridAdapter adapter = new OptionsGridAdapter(this, options, icons);
        optionsGridView.setAdapter(adapter);

        // Handle GridView item clicks
        optionsGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleGridItemClick(position);
            }
        });
    }

    private void fetchStudentData(String studentRegNumber) {
        databaseReference.child(studentRegNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve data from snapshot
                    String parentName = snapshot.child("parentName").getValue(String.class);
                    String studentName = snapshot.child("name").getValue(String.class);
                    rfid = snapshot.child("rfid").getValue(String.class);
                    proctorName = snapshot.child("proctor").getValue(String.class);
                    proctorEID = snapshot.child("proctorEID").getValue(String.class);

                    // Update UI
                    welcomeTextView.setText("Welcome, " + parentName);
                    studentInfoBanner.setText("Your Student Information");
                    studentInfoTextView.setText(
                            "Student Name: " + studentName + "\n" +
                                    "Registration Number: " + studentRegNumber + "\n" +
                                    "RFID: " + rfid + "\n" +
                                    "Proctor Name: " + proctorName + "\n" +
                                    "Proctor EID: " + proctorEID
                    );
                } else {
                    Toast.makeText(ParentDashboard.this, "No data found for this student", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentDashboard.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGridItemClick(int position) {
        Intent intent;
        switch (position) {
            case 0:
                intent = new Intent(this, CreditRequestsActivity.class);
                intent.putExtra("proctorId", proctorEID);
                intent.putExtra("UserID", studentRegNumber);
                break;
            case 1:
                intent = new Intent(this, StudentAttendanceHistory.class);
                intent.putExtra("UserID", studentRegNumber);
                break;
            case 2:
                intent = new Intent(this, AttendanceDetailsActivity.class);
                intent.putExtra("empID", proctorEID);
                intent.putExtra("userID", studentRegNumber);
                break;
            case 3:
                intent = new Intent(this, StudentPaymentHistory.class);
                intent.putExtra("UserID", studentRegNumber);
                break;
            default:
                return;
        }
        startActivity(intent);
    }
}
