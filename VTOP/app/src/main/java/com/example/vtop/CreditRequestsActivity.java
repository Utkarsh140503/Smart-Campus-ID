package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreditRequestsActivity extends AppCompatActivity {

    private ListView pendingListView, approvedListView;
    private List<String> pendingRequests, approvedRequests;
    private ArrayAdapter<String> pendingAdapter, approvedAdapter;

    private DatabaseReference studentRef, creditHistoryRef;
    private String userId, proctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credit_requests);

        pendingListView = findViewById(R.id.pendingListView);
        approvedListView = findViewById(R.id.approvedListView);

        pendingRequests = new ArrayList<>();
        approvedRequests = new ArrayList<>();

        pendingAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendingRequests);
        approvedAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, approvedRequests);

        pendingListView.setAdapter(pendingAdapter);
        approvedListView.setAdapter(approvedAdapter);

        // Get the UserID from the previous activity
        userId = getIntent().getStringExtra("UserID");

        // Initialize the Firebase reference to Student_Registrations
        studentRef = FirebaseDatabase.getInstance().getReference().child("Student_Registrations").child(userId);

        // Fetch the proctorId
        fetchProctorId();
    }

    private void fetchProctorId() {
        studentRef.child("proctorEID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    proctorId = dataSnapshot.getValue(String.class);
                    if (proctorId != null) {
                        // Use the retrieved proctorId to fetch the credit history
                        fetchCreditHistory();
                    } else {
                        Toast.makeText(CreditRequestsActivity.this, "Proctor ID not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreditRequestsActivity.this, "No data found for user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreditRequestsActivity.this, "Error fetching proctor ID: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCreditHistory() {
        creditHistoryRef = FirebaseDatabase.getInstance().getReference()
                .child("CreditRequests")
                .child(proctorId)
                .child(userId);

        creditHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pendingRequests.clear();
                approvedRequests.clear();

                for (DataSnapshot creditSnapshot : dataSnapshot.getChildren()) {
                    String creditId = creditSnapshot.getKey();
                    String number = creditSnapshot.child("number").getValue(String.class);
                    Boolean status = creditSnapshot.child("status").getValue(Boolean.class);

                    String displayText = "ID: " + creditId + ", \nCredit: " + number;

                    if (status != null && status) {
                        approvedRequests.add(displayText);
                    } else {
                        pendingRequests.add(displayText);
                    }
                }

                pendingAdapter.notifyDataSetChanged();
                approvedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreditRequestsActivity.this, "Error fetching credit history: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
