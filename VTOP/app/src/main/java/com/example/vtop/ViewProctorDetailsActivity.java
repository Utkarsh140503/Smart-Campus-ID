package com.example.vtop;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewProctorDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ViewProctorDetails";

    private DatabaseReference mDatabase;

    private TextView textViewEmpNumber;
    private TextView textViewEmpName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_proctor_details);

        textViewEmpNumber = findViewById(R.id.textViewEmpNumber);
        textViewEmpName = findViewById(R.id.textViewEmpName);

        // Get the userID from MainActivity
        String userID = getIntent().getStringExtra("UserID");

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get the proctor ID from Student_Registrations using the userID
        DatabaseReference proctorIdRef = mDatabase.child("Student_Registrations").child(userID).child("proctorEID");
        proctorIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String proctorID = dataSnapshot.getValue(String.class);

                // Get the proctor details from Employee_Registrations using the proctorID
                DatabaseReference proctorRef = mDatabase.child("Employee_Registrations").child(proctorID);
                proctorRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String empNumber = dataSnapshot.child("empNumber").getValue(String.class);
                        String empName = dataSnapshot.child("name").getValue(String.class);

                        // Display the proctor details in the text views
                        textViewEmpNumber.setText("Employee Number: " + empNumber);
                        textViewEmpName.setText("Employee Name: " + empName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ViewProctorDetailsActivity.this, "Failed to read proctor details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ViewProctorDetailsActivity.this, "Failed to read proctor ID.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
