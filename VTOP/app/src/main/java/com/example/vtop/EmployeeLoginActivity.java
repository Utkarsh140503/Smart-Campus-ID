package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmployeeLoginActivity extends AppCompatActivity {

    TextView regRedirect;
    private EditText editEmployeeNo, editPassword;
    private Button btnSubmit;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_login);

        LinearLayout developerLayout = findViewById(R.id.developerLayout);

        developerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portfolioUrl = "https://utkarsh140503.github.io/Portfolio/#";
                Uri uri = Uri.parse(portfolioUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        regRedirect = findViewById(R.id.textGoToRegistration);

        regRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmployeeLoginActivity.this, EmployeeRegistrationActivity.class);
                startActivity(intent);
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Employee_Registrations");

        editEmployeeNo = findViewById(R.id.editEmployeeID);
        editPassword = findViewById(R.id.editPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the entered registration number and password
                String empNo = editEmployeeNo.getText().toString().trim().toUpperCase();
                String password = editPassword.getText().toString().trim();

                // Validate if the fields are not empty
                if (TextUtils.isEmpty(empNo)) {
                    editEmployeeNo.setError("Please enter the registration number");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    editPassword.setError("Please enter the password");
                    return;
                }

                // Verify registration number and password from Firebase
                verifyUser(empNo, password);
            }
        });
    }

    private void verifyUser(final String empNo, final String password) {
        // Query the database to check if the registration number exists
        databaseReference.child(empNo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Registration number exists, now check the password
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    String nameFromDB = snapshot.child("name").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        // Password is correct, login successful
                        Toast.makeText(EmployeeLoginActivity.this, "Welcome "+nameFromDB, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EmployeeLoginActivity.this, EmployeeDashboardActivity.class);
                        intent.putExtra("ID", empNo);
                        intent.putExtra("Name", nameFromDB);
                        startActivity(intent);
                        finish();
                    } else {
                        // Password is incorrect
                        Toast.makeText(EmployeeLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Registration number does not exist
                    Toast.makeText(EmployeeLoginActivity.this, "Invalid registration number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EmployeeLoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}