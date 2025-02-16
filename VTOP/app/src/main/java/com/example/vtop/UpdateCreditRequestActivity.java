package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateCreditRequestActivity extends AppCompatActivity {

    private String empID, selectedKey;
    private TextView selectedRequestTextView;
    private ListView creditRequestListView;
    private DatabaseReference creditRequestsRef, studentRegistrationsRef;
    private List<String> creditRequestIds;
    private ArrayAdapter<String> creditRequestAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_credit_request);

        selectedRequestTextView = findViewById(R.id.selectedRequestTextView);
        creditRequestListView = findViewById(R.id.creditRequestListView);

        creditRequestIds = new ArrayList<>();
        creditRequestAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, creditRequestIds);
        creditRequestListView.setAdapter(creditRequestAdapter);

        empID = getIntent().getStringExtra("empID");
        selectedKey = getIntent().getStringExtra("selectedKey");

        creditRequestsRef = FirebaseDatabase.getInstance().getReference()
                .child("CreditRequests").child(empID).child(selectedKey);

        // Display the selected request
        selectedRequestTextView.setText("Selected Request: " + selectedKey);
        studentRegistrationsRef = FirebaseDatabase.getInstance().getReference().child("Student_Registrations");

        // Retrieve and display credit requests with status false
        loadCreditRequests();

        // Handle list item clicks
        creditRequestListView.setOnItemClickListener((parent, view, position, id) -> {
            String creditRequestId = creditRequestIds.get(position);
            showCreditRequestDialog(creditRequestId.substring(creditRequestId.indexOf('-')));
        });
    }

    private void loadCreditRequests() {
        creditRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                creditRequestIds.clear();
                for (DataSnapshot creditRequestSnapshot : snapshot.getChildren()) {
                    String creditRequestId = creditRequestSnapshot.getKey();
                    Boolean status = creditRequestSnapshot.child("status").getValue(Boolean.class);
                    if (status != null && !status) {
                        creditRequestIds.add("Request ID: "+creditRequestId);
                    }
                }
                creditRequestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void showCreditRequestDialog(String creditRequestId) {
        DatabaseReference creditRequestRef = creditRequestsRef.child(creditRequestId);

        creditRequestRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String number = snapshot.child("number").getValue(String.class);
                Boolean status = snapshot.child("status").getValue(Boolean.class);

                if (number == null || status == null) return;

                // Show dialog box
                AlertDialog.Builder dialog = new AlertDialog.Builder(UpdateCreditRequestActivity.this);
                dialog.setTitle("Credit Request Details");
                dialog.setMessage("Number: " + number + "\nStatus: " + status);

                // Approve Button
                dialog.setPositiveButton("Approve", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Update status to true
                        Map<String, Object> updateMap = new HashMap<>();
                        updateMap.put("status", true);

                        creditRequestRef.updateChildren(updateMap).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateStudentCredits(selectedKey, Integer.parseInt(number));
                                loadCreditRequests(); // Refresh the list
                            }
                        });
                    }
                });

                // Decline Button
                dialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void updateStudentCredits(String regNumber, int creditsToAdd) {
        DatabaseReference studentRef = studentRegistrationsRef.child(regNumber);

        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentCredits = snapshot.child("credits").getValue(Integer.class);
                if (currentCredits == null) return;

                int updatedCredits = currentCredits + creditsToAdd;

                // Update the credits in the database
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put("credits", updatedCredits);

                studentRef.updateChildren(updateMap).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Notify success
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}
