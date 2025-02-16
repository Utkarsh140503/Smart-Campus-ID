package com.example.vtop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VendorLoginActivity extends AppCompatActivity {

    private EditText editVendorId, editPassword;
    private Button btnSubmit;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_login);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Vendors");

        // Initialize Views
        editVendorId = findViewById(R.id.editVendorId);
        editPassword = findViewById(R.id.editPassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        TextView textGoToRegistration = findViewById(R.id.textGoToRegistration);
        TextView textGoToHomePage = findViewById(R.id.textGoToHomePage);

        // Handle "Submit" button click for login
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginVendor();
            }
        });

        // Redirect to VendorSignupActivity
        textGoToRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendorLoginActivity.this, VendorSignupActivity.class);
                startActivity(intent);
            }
        });

        // Redirect to StartUpActivity
        textGoToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VendorLoginActivity.this, StartupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loginVendor() {
        String vendorID = editVendorId.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Check if fields are empty
        if (TextUtils.isEmpty(vendorID) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate credentials against Firebase database
        databaseReference.child(vendorID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                DataSnapshot snapshot = task.getResult();
                String dbPassword = snapshot.child("password").getValue(String.class);

                if (dbPassword != null && dbPassword.equals(password)) {
                    String shopName = snapshot.child("shopName").getValue(String.class);

                    // Redirect to VendorDashboardActivity
                    Intent intent = new Intent(VendorLoginActivity.this, VendorDashboardActivity.class);
                    intent.putExtra("vendorID", vendorID);
                    intent.putExtra("shopName", shopName);
                    startActivity(intent);
                    finish(); // Finish the login activity
                } else {
                    Toast.makeText(VendorLoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(VendorLoginActivity.this, "Vendor ID not found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
