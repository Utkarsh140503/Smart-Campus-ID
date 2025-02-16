package com.example.vtop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class LeaveRequestActivity extends AppCompatActivity {

    private Button btnLeaveRequest;
    private Button btnLeaveStatus;
    private Button btnLeaveHistory;

    private Fragment leaveRequestFragment;
    private Fragment leaveStatusFragment;
    private Fragment leaveHistoryFragment;

    private String userID; // User ID variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leave_request);

        btnLeaveRequest = findViewById(R.id.btn_leave_request);
        btnLeaveStatus = findViewById(R.id.btn_leave_status);
        btnLeaveHistory = findViewById(R.id.btn_leave_history);

        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID"); // Retrieve the UserID from Intent

        leaveRequestFragment = LeaveRequestFragment.newInstance(userID); // Pass the UserID to the fragment
        leaveStatusFragment = LeaveStatusFragment.newInstance(userID);
        leaveHistoryFragment = LeaveHistoryFragment.newInstance(userID);

        // Initially hide all fragments
        hideAllFragments();

        btnLeaveRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(leaveRequestFragment);
            }
        });

        btnLeaveStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(leaveStatusFragment);
            }
        });

        btnLeaveHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFragment(leaveHistoryFragment);
            }
        });
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    private void hideAllFragments() {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(leaveRequestFragment)
                .remove(leaveStatusFragment)
                .remove(leaveHistoryFragment)
                .commit();
    }
}
