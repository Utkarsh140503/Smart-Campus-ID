package com.example.vtop;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RFIDGatePassActivity extends AppCompatActivity {

    private TextView checkOutStatusTextView, checkedInStatusTextView;
    private TextView checkedOutDateTextView, checkedOutTimeTextView;
    private TextView checkedInDateTextView, checkedInTimeTextView;

    private FirebaseDatabase database;
    private DatabaseReference leaveManagementRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfidgate_pass);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();

        // Initialize UI components
        checkOutStatusTextView = findViewById(R.id.check_out_status_value);
        checkedInStatusTextView = findViewById(R.id.checked_in_status_value);
        checkedOutDateTextView = findViewById(R.id.checked_out_date_value);
        checkedOutTimeTextView = findViewById(R.id.checked_out_time_value);
        checkedInDateTextView = findViewById(R.id.checked_in_date_value);
        checkedInTimeTextView = findViewById(R.id.checked_in_time_value);

        // Get the UserID from the Intent (passed from the previous activity)
        String userId = getIntent().getStringExtra("UserID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the student’s RFID data in LeaveManagement
        DatabaseReference userRfidRef = database.getReference("Student_Registrations")
                .child(userId).child("rfid");

        userRfidRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rfid = dataSnapshot.getValue(String.class);
                if (rfid != null) {
                    fetchLeaveManagementData(rfid);
                } else {
                    Toast.makeText(RFIDGatePassActivity.this, "RFID not found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RFIDGatePassActivity.this, "Failed to fetch RFID: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchLeaveManagementData(String rfid) {
        // Reference to the LeaveManagement data for the student’s RFID
        leaveManagementRef = database.getReference("LeaveManagement").child(rfid);

        leaveManagementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve data and set to TextViews, if data is missing set 'false'
                String checkOutStatus = dataSnapshot.child("checkOut").getValue(Boolean.class)+"";
                String checkedInStatus = dataSnapshot.child("checkedIn").getValue(Boolean.class)+"";
                String checkedOutDate = dataSnapshot.child("checkedOutDate").getValue(String.class);
                String checkedOutTime = dataSnapshot.child("checkedOutTime").getValue(String.class);
                String checkedInDate = dataSnapshot.child("checkedInDate").getValue(String.class);
                String checkedInTime = dataSnapshot.child("checkedInTime").getValue(String.class);

                checkOutStatusTextView.setText(!checkOutStatus.equals("null") ? checkOutStatus : "false");
                checkedInStatusTextView.setText(!checkedInStatus.equals("null") ? checkedInStatus : "false");
                checkedOutDateTextView.setText(checkedOutDate != null ? checkedOutDate : "false");
                checkedOutTimeTextView.setText(checkedOutTime != null ? checkedOutTime : "false");
                checkedInDateTextView.setText(checkedInDate != null ? checkedInDate : "false");
                checkedInTimeTextView.setText(checkedInTime != null ? checkedInTime : "false");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RFIDGatePassActivity.this, "Failed to fetch data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
