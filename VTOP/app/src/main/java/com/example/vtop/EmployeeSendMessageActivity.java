package com.example.vtop;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmployeeSendMessageActivity extends AppCompatActivity {

    private static final String TAG = "EmployeeSendMessage";

    private DatabaseReference mDatabase;

    private EditText editTextStudentID;
    private EditText editTextMessage;
    private Button searchButton;
    private Button sendButton;

    private String empID;
    private int messageCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_send_message);

        // Get empID from EmployeeDashboardActivity as intent extra
        empID = getIntent().getStringExtra("empID");

        editTextStudentID = findViewById(R.id.editTextStudentID);
        editTextMessage = findViewById(R.id.editTextMessage);
        searchButton = findViewById(R.id.searchButton);
        sendButton = findViewById(R.id.sendButton);

        // Initialize Firebase database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentID = editTextStudentID.getText().toString().trim();

                if(studentID.isEmpty()){
                    editTextStudentID.setError("Student ID cannot be empty!");
                }else{
                    // Check if the student ID exists in the database
                    DatabaseReference studentRef = mDatabase.child("Student_Registrations").child(studentID);
                    studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Student exists, enable the send button
                                sendButton.setEnabled(true);
                            } else {
                                // Student does not exist, disable the send button
                                sendButton.setEnabled(false);
                                Toast.makeText(EmployeeSendMessageActivity.this, "Student with ID "+studentID+" does not exist.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(EmployeeSendMessageActivity.this, "Failed to search for student ID.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentID = editTextStudentID.getText().toString().trim();
                String message = editTextMessage.getText().toString().trim();

                // Get the next message count
                DatabaseReference messageCountRef = mDatabase.child("MessageFromProctors").child(empID).child(studentID).child("messageCount");
                messageCountRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            messageCount = dataSnapshot.getValue(Integer.class);
                        }

                        // Store the message in the database
                        DatabaseReference messageRef = mDatabase.child("MessageFromProctors").child(empID).child(studentID).child("messages").child("message" + messageCount);
                        messageRef.setValue(message, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@NonNull DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    // Increment the message count
                                    messageCount++;

                                    // Update the message count in the database
                                    mDatabase.child("MessageFromProctors").child(empID).child(studentID).child("messageCount").setValue(messageCount);

                                    Toast.makeText(EmployeeSendMessageActivity.this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EmployeeSendMessageActivity.this, "Failed to send message!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e(TAG, "Failed to retrieve message count.", databaseError.toException());
                        Toast.makeText(EmployeeSendMessageActivity.this, "Failed to send message!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
