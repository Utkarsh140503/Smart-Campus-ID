package com.example.vtop;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OnlineBookingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_booking);

        TextView notificationText = findViewById(R.id.notification_text);
        notificationText.setText("VTOP Notification:\n\nSorry, Unable to process your request.\n\nKindly contact HELP DESK (SDC)\nchennai.sdc@vit.ac.in");
    }
}
