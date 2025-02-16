package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class VendorSignupActivity extends AppCompatActivity {

    private EditText editTextVendorID, editTextShopName, editTextPass;
    private Button btnSubmit;
    private TextView textViewAlreadyRegistered, textViewGoToHomePage;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_signup);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Vendors");

        // Initialize Views
        editTextVendorID = findViewById(R.id.editTextVendorID);
        editTextShopName = findViewById(R.id.editTextShopName);
        editTextPass = findViewById(R.id.editTextPass);
        btnSubmit = findViewById(R.id.btnSubmit);
        textViewAlreadyRegistered = findViewById(R.id.textViewAlreadyRegistered);
        textViewGoToHomePage = findViewById(R.id.textViewGoToHomePage);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveVendorDetails();
            }
        });

        // Handle Already Registered Click
        textViewAlreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(VendorSignupActivity.this, VendorLoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

        // Handle Go to Home Page Click
        textViewGoToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent homeIntent = new Intent(VendorSignupActivity.this, StartupActivity.class);
                startActivity(homeIntent);
                finish();
            }
        });
    }

    private void saveVendorDetails() {
        String vendorID = editTextVendorID.getText().toString().trim();
        String shopName = editTextShopName.getText().toString().trim();
        String password = editTextPass.getText().toString().trim();

        if (TextUtils.isEmpty(vendorID) || TextUtils.isEmpty(shopName) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Vendor Object
        Vendor vendor = new Vendor(vendorID, shopName, password);

        // Save to Firebase
            databaseReference.child(vendorID).setValue(vendor).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(VendorSignupActivity.this, "Vendor Registered Successfully", Toast.LENGTH_SHORT).show();
                    // Clear Fields
                    editTextVendorID.setText("");
                    editTextShopName.setText("");
                    editTextPass.setText("");
                } else {
                    Toast.makeText(VendorSignupActivity.this, "Failed to Register Vendor", Toast.LENGTH_SHORT).show();
                }
            });
    }
}

// Vendor Class
class Vendor {
    public String vendorID;
    public String shopName;
    public String password;

    public Vendor() {
        // Default constructor required for Firebase
    }

    public Vendor(String vendorID, String shopName, String password) {
        this.vendorID = vendorID;
        this.shopName = shopName;
        this.password = password;
    }
}
