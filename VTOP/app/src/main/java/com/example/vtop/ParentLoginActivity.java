package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentLoginActivity extends AppCompatActivity {

    private EditText editRegNo, editPassword;
    private Button btnSubmit;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("ParentRegistration");

        // Bind views
        editRegNo = findViewById(R.id.editRegNo);
        editPassword = findViewById(R.id.editPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editRegNo.getText().toString().trim();
                String password = editPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(ParentLoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if the email ends with @gmail.com
                if (!email.endsWith("@gmail.com")) {
                    Toast.makeText(ParentLoginActivity.this, "Email must end with @gmail.com", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Extract the part before @gmail.com
                String username = email.substring(0, email.indexOf("@gmail.com"));

                // Authenticate the user
                authenticateUser(username, password);
            }
        });
    }

    private void authenticateUser(String username, String password) {
        databaseReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Check if password matches
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        String parentName = snapshot.child("parentName").getValue(String.class);
                        String studentRegNumber = snapshot.child("studentRegNumber").getValue(String.class);

                        Toast.makeText(ParentLoginActivity.this, "Welcome, " + parentName, Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(ParentLoginActivity.this, ParentDashboard.class);
                        i.putExtra("username", username);
                        i.putExtra("studentRegNumber", studentRegNumber);
                        i.putExtra("parentName", parentName);
                        startActivity(i);
                        // Proceed to next activity (if required)
                    } else {
                        Toast.makeText(ParentLoginActivity.this, "Invalid Password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ParentLoginActivity.this, "No account found for this email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ParentLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
