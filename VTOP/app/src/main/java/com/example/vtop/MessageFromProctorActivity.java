package com.example.vtop;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MessageFromProctorActivity extends AppCompatActivity {

    private static final String TAG = "MessageFromProctor";

    private DatabaseReference mDatabase;

    private ListView messageListView;
    private ArrayAdapter<String> messageAdapter;
    private List<String> messages;

    private String userID;
    TextView textViewProctorID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_from_proctor);

        // Get userID from MainActivity as intent extra
        userID = getIntent().getStringExtra("UserID");

        messageListView = findViewById(R.id.messageListView);
        messages = new ArrayList<>();
        messageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        messageListView.setAdapter(messageAdapter);
        textViewProctorID = findViewById(R.id.textViewProctorID);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Retrieve proctorID from database
        DatabaseReference proctorIDRef = mDatabase.child("Student_Registrations").child(userID).child("proctorEID");
        proctorIDRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String proctorID = dataSnapshot.getValue(String.class);
                    retrieveMessages(proctorID);
                } else {
                    Log.e(TAG, "Proctor ID not found for userID: " + userID);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve proctorID.", databaseError.toException());
            }
        });
    }

    private void retrieveMessages(String proctorID) {
        DatabaseReference messagesRef = mDatabase.child("MessageFromProctors").child(proctorID).child(userID).child("messages");
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                textViewProctorID.setText("Proctor ID: " + proctorID);
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    String message = messageSnapshot.getValue(String.class);
                    messages.add(message);
                }

                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve messages.", databaseError.toException());
            }
        });
    }
}
