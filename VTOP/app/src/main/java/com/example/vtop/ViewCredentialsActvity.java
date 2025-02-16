package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewCredentialsActvity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private TextView usernameTextView;
    private TextView passwordTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_credentials_actvity);

        // Get the user ID from the intent
        String userID = getIntent().getStringExtra("UserID");

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference("Student_Registrations");

        // Retrieve the password from Firebase Realtime Database based on the user ID
        mDatabase.child(userID).child("password").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the password value
                String password = dataSnapshot.getValue(String.class);

                // Update the UI with the retrieved credentials
                TextView usernameTextView = findViewById(R.id.usernameTextView);
                TextView passwordTextView = findViewById(R.id.passwordTextView);

                // Set the username and password in the TextViews
                usernameTextView.setText(userID);
                passwordTextView.setText(password);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
            }
        });
    }
}