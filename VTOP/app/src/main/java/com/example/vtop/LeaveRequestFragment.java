package com.example.vtop;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LeaveRequestFragment extends Fragment {

    private TextView tvSelectedFromDate, tvSelectedTimeFrom, tvSelectedToDate, tvSelectedTimeTo, tvProctorName;
    private Spinner spinnerLeaveType;
    private TextView etVisitingPlace, etReason;
    private Button btnFromDate, btnTimeFrom, btnToDate, btnTimeTo, btnSubmitLeave;
    private Calendar calendar;

    private String userID; // User ID variable
    private String proctorEID;

    private DatabaseReference databaseReference;

    public static LeaveRequestFragment newInstance(String userID) {
        LeaveRequestFragment fragment = new LeaveRequestFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_request, container, false);

        tvSelectedFromDate = view.findViewById(R.id.tvSelectedFromDate);
        tvSelectedTimeFrom = view.findViewById(R.id.tvSelectedTimeFrom);
        tvSelectedToDate = view.findViewById(R.id.tvSelectedToDate);
        tvSelectedTimeTo = view.findViewById(R.id.tvSelectedTimeTo);
        tvProctorName = view.findViewById(R.id.tvProctorName);
        spinnerLeaveType = view.findViewById(R.id.spinnerLeaveType);
        etVisitingPlace = view.findViewById(R.id.etVisitingPlace);
        etReason = view.findViewById(R.id.etReason);
        btnFromDate = view.findViewById(R.id.btnFromDate);
        btnTimeFrom = view.findViewById(R.id.btnTimeFrom);
        btnToDate = view.findViewById(R.id.btnToDate);
        btnTimeTo = view.findViewById(R.id.btnTimeTo);
        btnSubmitLeave = view.findViewById(R.id.btnSubmitLeave);

        calendar = Calendar.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        retrieveProctorName();
        retrieveProctorEID();

        ArrayAdapter<CharSequence> leaveTypeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.leave_types, android.R.layout.simple_spinner_item);
        leaveTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeaveType.setAdapter(leaveTypeAdapter);

        btnFromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(tvSelectedFromDate);
            }
        });

        btnTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(tvSelectedTimeFrom);
            }
        });

        btnToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(tvSelectedToDate);
            }
        });

        btnTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(tvSelectedTimeTo);
            }
        });

        btnSubmitLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLeaveRequest();
            }
        });

        return view;
    }

    private void retrieveProctorName() {
        databaseReference.child("Student_Registrations").child(userID).child("proctor").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String proctorName = dataSnapshot.getValue(String.class);
                    tvProctorName.setText(proctorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LeaveRequestFragment", "Failed to retrieve proctor name from database: " + databaseError.getMessage());
            }
        });
    }

    private void retrieveProctorEID() {
        databaseReference.child("Student_Registrations").child(userID).child("proctorEID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    proctorEID = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LeaveRequestFragment", "Failed to retrieve proctor EID from database: " + databaseError.getMessage());
            }
        });
    }

    private void showDatePicker(final TextView textView) {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String formattedDate = sdf.format(calendar.getTime());
                textView.setText(formattedDate);
            }
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(final TextView textView) {
        TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);

                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String formattedTime = sdf.format(calendar.getTime());
                textView.setText(formattedTime);
            }
        };

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void submitLeaveRequest() {
        String leaveType = spinnerLeaveType.getSelectedItem().toString();
        String fromDate = tvSelectedFromDate.getText().toString();
        String fromTime = tvSelectedTimeFrom.getText().toString();
        String toDate = tvSelectedToDate.getText().toString();
        String toTime = tvSelectedTimeTo.getText().toString();
        String proctorName = tvProctorName.getText().toString();
        String visitingPlace = etVisitingPlace.getText().toString();
        String reason = etReason.getText().toString();

        // Check if all fields are filled
        if (leaveType.isEmpty() || fromDate.isEmpty() || fromTime.isEmpty() || toDate.isEmpty()
                || toTime.isEmpty() || proctorName.isEmpty() || visitingPlace.isEmpty() || reason.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the "from" date is before the "to" date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date fromDateObj = sdf.parse(fromDate);
            Date toDateObj = sdf.parse(toDate);
            if (fromDateObj != null && toDateObj != null) {
                if (fromDateObj.after(toDateObj)) {
                    Toast.makeText(requireContext(), "Please select a valid date range", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error parsing dates", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a random 7-digit leave ID
        String leaveId = "L" + String.format("%07d", (int) (Math.random() * 10000000));

        // Store the leave request in the database
        DatabaseReference leaveRequestsRef = databaseReference.child("LeaveRequestsByStudents").child(userID);
        DatabaseReference xyz = databaseReference.child("AllLeaveRequestsByStudents").child(proctorEID);

        leaveRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                long leaveCount = dataSnapshot.getChildrenCount(); // Get the current count of leave requests

                LeaveRequest leaveRequest = new LeaveRequest(leaveType, fromDate, fromTime, toDate, toTime, proctorName, visitingPlace, reason, leaveId, "Pending");
                leaveRequestsRef.child(leaveId).setValue(leaveRequest);

                xyz.child(userID).child(leaveId).setValue(leaveRequest);

                Toast.makeText(requireContext(), "Leave request submitted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("LeaveRequestFragment", "Failed to submit leave request: " + databaseError.getMessage());
                Toast.makeText(requireContext(), "Failed to submit leave request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class LeaveRequest {
        private String leaveType;
        private String fromDate;
        private String fromTime;
        private String toDate;
        private String toTime;
        private String proctorName;
        private String visitingPlace;
        private String reason;
        private String leaveId;
        private String leaveStatus;

        public LeaveRequest() {
            // Default constructor required for Firebase
        }

        public LeaveRequest(String leaveType, String fromDate, String fromTime, String toDate,
                            String toTime, String proctorName, String visitingPlace, String reason, String leaveId, String leaveStatus) {
            this.leaveType = leaveType;
            this.fromDate = fromDate;
            this.fromTime = fromTime;
            this.toDate = toDate;
            this.toTime = toTime;
            this.proctorName = proctorName;
            this.visitingPlace = visitingPlace;
            this.reason = reason;
            this.leaveId = leaveId;
            this.leaveStatus = leaveStatus;
        }

        public String getLeaveStatus() {
            return leaveStatus;
        }

        public void setLeaveStatus(String leaveStatus) {
            this.leaveStatus = leaveStatus;
        }

        public String getLeaveId() {
            return leaveId;
        }

        public void setLeaveId(String leaveId) {
            this.leaveId = leaveId;
        }

        public String getLeaveType() {
            return leaveType;
        }

        public void setLeaveType(String leaveType) {
            this.leaveType = leaveType;
        }

        public String getFromDate() {
            return fromDate;
        }

        public void setFromDate(String fromDate) {
            this.fromDate = fromDate;
        }

        public String getFromTime() {
            return fromTime;
        }

        public void setFromTime(String fromTime) {
            this.fromTime = fromTime;
        }

        public String getToDate() {
            return toDate;
        }

        public void setToDate(String toDate) {
            this.toDate = toDate;
        }

        public String getToTime() {
            return toTime;
        }

        public void setToTime(String toTime) {
            this.toTime = toTime;
        }

        public String getProctorName() {
            return proctorName;
        }

        public void setProctorName(String proctorName) {
            this.proctorName = proctorName;
        }

        public String getVisitingPlace() {
            return visitingPlace;
        }

        public void setVisitingPlace(String visitingPlace) {
            this.visitingPlace = visitingPlace;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
