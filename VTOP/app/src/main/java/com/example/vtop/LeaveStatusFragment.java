package com.example.vtop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LeaveStatusFragment extends Fragment {

    private String userID; // User ID variable

    private ListView leaveRequestsListView;
    private TextView noRequestsTextView;
    private DatabaseReference leaveRequestsRef;
    private List<String> leaveRequestsList;
    private ArrayAdapter<String> leaveRequestsAdapter;

    public static LeaveStatusFragment newInstance(String userID) {
        LeaveStatusFragment fragment = new LeaveStatusFragment();
        Bundle args = new Bundle();
        args.putString("UserID", userID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userID = getArguments().getString("UserID");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leave_status, container, false);

        leaveRequestsListView = view.findViewById(R.id.leaveRequestsListView);
        noRequestsTextView = view.findViewById(R.id.noRequestsTextView);

        leaveRequestsList = new ArrayList<>();
        leaveRequestsAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item_leave_request, R.id.leaveRequestTextView, leaveRequestsList);
        leaveRequestsListView.setAdapter(leaveRequestsAdapter);

        leaveRequestsRef = FirebaseDatabase.getInstance().getReference()
                .child("LeaveRequestsByStudents")
                .child(userID); // Replace "userID" with the actual user ID

        retrieveLeaveRequests();

        // Set click listener for ListView items
        leaveRequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedLeaveId = leaveRequestsList.get(position);
                String leaveId = selectedLeaveId.replace("Leave ID: ", ""); // Remove "Leave ID: " prefix
                leaveId = leaveId.substring(0,8);
                showLeaveDetailsDialog(leaveId);
            }
        });

        return view;
    }

    private void retrieveLeaveRequests() {
        leaveRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leaveRequestsList.clear();

                for (DataSnapshot leaveSnapshot : dataSnapshot.getChildren()) {
                    String leaveId = leaveSnapshot.getKey();

                    // Retrieve leave application details from the leaveSnapshot
                    // Adjust the code below based on your database structure
                    String leaveType = leaveSnapshot.child("leaveType").getValue(String.class);
                    String leaveStatus = leaveSnapshot.child("leaveStatus").getValue(String.class);
                    String visitingPlace = leaveSnapshot.child("visitingPlace").getValue(String.class);

                    if (leaveStatus != null && leaveStatus.equals("Pending")) {
                        String leaveInfo = "Leave ID: " + leaveId
                                +"\nVisiting Place: " + visitingPlace
                                +"\nLeave Type: " + leaveType;
                        leaveRequestsList.add(leaveInfo);
                    }
                }

                leaveRequestsAdapter.notifyDataSetChanged();

                if (leaveRequestsList.isEmpty()) {
                    noRequestsTextView.setVisibility(View.VISIBLE);
                } else {
                    noRequestsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void showLeaveDetailsDialog(String leaveId) {
        DatabaseReference leaveRef = leaveRequestsRef.child(leaveId);

        leaveRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve leave application details from the dataSnapshot
                // Adjust the code below based on your database structure
                String fromDate = dataSnapshot.child("fromDate").getValue(String.class);
                String toDate = dataSnapshot.child("toDate").getValue(String.class);
                String leaveType = dataSnapshot.child("leaveType").getValue(String.class);
                String fromTime = dataSnapshot.child("fromTime").getValue(String.class);
                String toTime = dataSnapshot.child("toTime").getValue(String.class);
                String reason = dataSnapshot.child("reason").getValue(String.class);
                String proctorName = dataSnapshot.child("proctorName").getValue(String.class);
                String visitingPlace = dataSnapshot.child("visitingPlace").getValue(String.class);
                String leaveStatus = dataSnapshot.child("leaveStatus").getValue(String.class);

                // Create and show a dialog to display the leave details
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Leave Details");
                builder.setMessage("Leave ID: " + leaveId + "\n" +
                        "From Date: " + fromDate + "\n" +
                        "To Date: " + toDate + "\n" +
                        "Leave Type: " + leaveType + "\n" +
                        "From Time: " + fromTime + "\n" +
                        "To Time: " + toTime + "\n" +
                        "Reason: " + reason + "\n" +
                        "Proctor Name: " + proctorName + "\n" +
                        "Visiting Place: " + visitingPlace + "\n" +
                        "Leave Status: " + leaveStatus);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }
}
