package com.example.vtop;// LeaveHistoryFragment.java

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

public class LeaveHistoryFragment extends Fragment {

    private String userID;
    private ListView leaveHistoryListView;
    private TextView tvLeaveHistory;
    private DatabaseReference leaveRequestsRef;
    private List<LeaveRequest> leaveRequestsList;
    private ArrayAdapter<LeaveRequest> leaveRequestsAdapter;

    public static LeaveHistoryFragment newInstance(String userID) {
        LeaveHistoryFragment fragment = new LeaveHistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_leave_history, container, false);

        leaveHistoryListView = view.findViewById(R.id.listLeaveHistory);
        tvLeaveHistory = view.findViewById(R.id.tvLeaveHistory);

        leaveRequestsList = new ArrayList<>();
        leaveRequestsAdapter = new ArrayAdapter<LeaveRequest>(requireContext(), R.layout.list_item_leave_history, leaveRequestsList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_leave_history, parent, false);
                }

                TextView leaveInfoTextView = convertView.findViewById(R.id.leaveInfoTextView);
                LeaveRequest leaveRequest = getItem(position);
                if (leaveRequest != null) {
                    leaveInfoTextView.setText(leaveRequest.toString());
                }

                return convertView;
            }
        };

        leaveHistoryListView.setAdapter(leaveRequestsAdapter);

        leaveRequestsRef = FirebaseDatabase.getInstance().getReference().child("LeaveRequestsByStudents").child(userID);
        leaveRequestsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                leaveRequestsList.clear();

                for (DataSnapshot leaveSnapshot : dataSnapshot.getChildren()) {
                    String leaveId = leaveSnapshot.getKey();
                    String leaveType = leaveSnapshot.child("leaveType").getValue(String.class);
                    String visitingPlace = leaveSnapshot.child("visitingPlace").getValue(String.class);
                    LeaveRequest leaveRequest = new LeaveRequest(leaveId, leaveType, visitingPlace);
                    leaveRequestsList.add(leaveRequest);
                }

                leaveRequestsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });

        leaveHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LeaveRequest leaveRequest = leaveRequestsList.get(position);
                String leaveId = leaveRequest.getLeaveId();
                showLeaveDetailsDialog(leaveId);
            }
        });

        return view;
    }

    private void showLeaveDetailsDialog(String leaveId) {
        DatabaseReference leaveDetailsRef = FirebaseDatabase.getInstance().getReference().child("LeaveRequestsByStudents").child(userID).child(leaveId);
        leaveDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String leaveType = dataSnapshot.child("leaveType").getValue(String.class);
                    String fromDate = dataSnapshot.child("fromDate").getValue(String.class);
                    String toDate = dataSnapshot.child("toDate").getValue(String.class);
                    String visitingPlace = dataSnapshot.child("visitingPlace").getValue(String.class);
                    String proctor = dataSnapshot.child("proctorName").getValue(String.class);
                    String reason = dataSnapshot.child("reason").getValue(String.class);
                    String leaveStatus = dataSnapshot.child("leaveStatus").getValue(String.class);

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setTitle("Leave Details");
                    builder.setMessage("Leave ID: " + leaveId +
                            "\nLeave Type: " + leaveType +
                            "\nFrom: " + fromDate +
                            "\nTo: " + toDate +
                            "\nVisiting Place: " + visitingPlace +
                            "\nProctor: " + proctor +
                            "\nReason: " + reason +
                            "\nLeave Status: " + leaveStatus);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private static class LeaveRequest {
        private String leaveId;
        private String leaveType;
        private String visitingPlace;

        public LeaveRequest(String leaveId, String leaveType, String visitingPlace) {
            this.leaveId = leaveId;
            this.leaveType = leaveType;
            this.visitingPlace = visitingPlace;
        }

        public String getLeaveId() {
            return leaveId;
        }

        public String getLeaveType() {
            return leaveType;
        }

        public String getVisitingPlace() {
            return visitingPlace;
        }

        @NonNull
        @Override
        public String toString() {
            return "Leave ID: " + leaveId + "\nLeave Type: " + leaveType + "\nVisiting Place: " + visitingPlace;
        }
    }
}
