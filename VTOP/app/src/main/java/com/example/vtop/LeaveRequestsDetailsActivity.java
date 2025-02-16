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

public class LeaveRequestsDetailsActivity extends AppCompatActivity {

    private String userID, empID;
    private ListView leaveRequestsListView;
    private DatabaseReference leaveRequestsRef;
    private List<String> leaveRequestsList;
    private ArrayAdapter<String> leaveRequestsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_requests_details);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            empID = extras.getString("empID");
            userID = extras.getString("userID");
        }

        leaveRequestsListView = findViewById(R.id.leaveRequestsListView);

        leaveRequestsList = new ArrayList<>();
        leaveRequestsAdapter = new ArrayAdapter<>(this, R.layout.list_item_leave_request, R.id.leaveRequestTextView, leaveRequestsList);
        leaveRequestsListView.setAdapter(leaveRequestsAdapter);

        leaveRequestsRef = FirebaseDatabase.getInstance().getReference()
                .child("AllLeaveRequestsByStudents")
                .child(empID)
                .child(userID);

        retrieveLeaveRequests();

        leaveRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLeaveDetailsPopup(position);
            }
        });
    }

    private void retrieveLeaveRequests() {
        leaveRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                leaveRequestsList.clear();

                for (DataSnapshot leaveSnapshot : dataSnapshot.getChildren()) {
                    String leaveId = leaveSnapshot.getKey();

                    String fromDate = leaveSnapshot.child("fromDate").getValue(String.class);
                    String toDate = leaveSnapshot.child("toDate").getValue(String.class);
                    String fromTime = leaveSnapshot.child("fromTime").getValue(String.class);
                    String toTime = leaveSnapshot.child("toTime").getValue(String.class);
                    String leaveType = leaveSnapshot.child("leaveType").getValue(String.class);
                    String proctorName = leaveSnapshot.child("proctorName").getValue(String.class);
                    String reason = leaveSnapshot.child("reason").getValue(String.class);
                    String visitingPlace = leaveSnapshot.child("visitingPlace").getValue(String.class);
                    String leaveStatus = leaveSnapshot.child("leaveStatus").getValue(String.class);

                    String leaveInfo = "Leave ID: " + leaveId +
                            "\nFrom Date: " + fromDate +
                            "\nTo Date: " + toDate +
                            "\nFrom Time: " + fromTime +
                            "\nTo Time: " + toTime +
                            "\nLeave Type: " + leaveType +
                            "\nProctor Name: " + proctorName +
                            "\nReason: " + reason +
                            "\nVisiting Place: " + visitingPlace +
                            "\nLeave Status: " + leaveStatus;

                    leaveRequestsList.add(leaveInfo);
                }

                leaveRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void showLeaveDetailsPopup(final int position) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.leave_details_popup, null);
        dialogBuilder.setView(dialogView);

        final TextView leaveDetailsTextView = dialogView.findViewById(R.id.leaveDetailsTextView);
        final Spinner leaveStatusSpinner = dialogView.findViewById(R.id.leaveStatusSpinner);

        leaveDetailsTextView.setText(leaveRequestsList.get(position));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.leave_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        leaveStatusSpinner.setAdapter(adapter);

        dialogBuilder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedLeaveStatus = leaveStatusSpinner.getSelectedItem().toString();
                updateLeaveStatus(position, selectedLeaveStatus);
            }
        });

        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void updateLeaveStatus(int position, String selectedLeaveStatus) {
        String leaveId = leaveRequestsListView.getItemAtPosition(position).toString().split("\n")[0].replace("Leave ID: ", "");
        DatabaseReference leaveRef = FirebaseDatabase.getInstance().getReference()
                .child("AllLeaveRequestsByStudents")
                .child(empID)
                .child(userID)
                .child(leaveId)
                .child("leaveStatus");

        DatabaseReference studentLeaveRef = FirebaseDatabase.getInstance().getReference()
                .child("LeaveRequestsByStudents")
                        .child(userID)
                        .child(leaveId)
                        .child("leaveStatus");

        leaveRef.setValue(selectedLeaveStatus);
        studentLeaveRef.setValue(selectedLeaveStatus);
        Toast.makeText(this, "Leave Status updated successfully", Toast.LENGTH_SHORT).show();
    }
}
