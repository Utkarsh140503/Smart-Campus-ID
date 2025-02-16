package com.example.vtop;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AttendanceDetailsActivity extends AppCompatActivity {

    private String userID, empID;
    private ListView attendanceDetailsListView;
    private DatabaseReference attendanceRef;
    private List<String> attendanceList;
    private ArrayAdapter<String> attendanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            empID = extras.getString("empID");
            userID = extras.getString("userID");
        }

        attendanceDetailsListView = findViewById(R.id.attendanceDetailsListView);

        attendanceList = new ArrayList<>();
        attendanceAdapter = new ArrayAdapter<>(this, R.layout.list_item_attendance, R.id.attendanceTextView, attendanceList);
        attendanceDetailsListView.setAdapter(attendanceAdapter);

        attendanceRef = FirebaseDatabase.getInstance().getReference()
                .child("Student_Attendance")
                .child(empID)
                .child(userID);

        retrieveAttendance();

//        leaveRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                showLeaveDetailsPopup(position);
//            }
//        });
    }

    private void retrieveAttendance() {
        attendanceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                attendanceList.clear();

                for (DataSnapshot attendanceSnapshot : dataSnapshot.getChildren()) {
                    String attendanceId = attendanceSnapshot.getKey();

                    String cityName = attendanceSnapshot.child("cityName").getValue(String.class);
                    String dateAndTime = attendanceSnapshot.child("dateAndTime").getValue(String.class);
                    String latitude = attendanceSnapshot.child("latitude").getValue(Double.class)+"";
                    String longitude = attendanceSnapshot.child("longitude").getValue(Double.class)+"";
                    String proctorId = attendanceSnapshot.child("proctorId").getValue(String.class);
                    String studentId = attendanceSnapshot.child("studentId").getValue(String.class);

                    String attendanceInfo = "Attendance ID: " + attendanceId +
                            "\nCity Name: " + cityName +
                            "\nDate and Time: " + dateAndTime +
                            "\nLatitude: " + latitude +
                            "\nLongitude: " + longitude +
                            "\nProctor ID: " + proctorId +
                            "\nStudent ID: " + studentId;

                    attendanceList.add(attendanceInfo);
                }

                attendanceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }
}
