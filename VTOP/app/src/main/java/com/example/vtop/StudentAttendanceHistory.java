package com.example.vtop;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StudentAttendanceHistory extends AppCompatActivity {

    private ListView attendanceListView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> attendanceHistory;

    private FirebaseDatabase database;
    private DatabaseReference attendanceHistoryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance_history);

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        // Initialize UI components
        attendanceListView = findViewById(R.id.attendance_list_view);
        attendanceHistory = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendanceHistory);
        attendanceListView.setAdapter(adapter);

        // Get RFID from intent
        String userId = getIntent().getStringExtra("UserID");
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Invalid User ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to Student Registration node
        DatabaseReference userRef = database.getReference("Student_Registrations").child(userId).child("rfid");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String rfid = dataSnapshot.getValue(String.class);
                if (rfid != null) {
                    fetchAttendanceHistory(rfid);
                } else {
                    Toast.makeText(StudentAttendanceHistory.this, "RFID not found for user!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentAttendanceHistory.this, "Failed to fetch RFID: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceHistory(String rfid) {
        // Reference to Attendance History node
        attendanceHistoryRef = database.getReference("AttendanceRFIDHistory").child(rfid);
        attendanceHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                attendanceHistory.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String dateTimeKey = snapshot.getKey();
                    String dateTimeValue = snapshot.getValue(String.class);

                    if (dateTimeKey != null && dateTimeValue != null) {
                        attendanceHistory.add("Date and Time of Attendance: \n"+dateTimeValue);
                    }
                }
                if (attendanceHistory.isEmpty()) {
                    Toast.makeText(StudentAttendanceHistory.this, "No attendance history found!", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StudentAttendanceHistory.this, "Failed to fetch history: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
