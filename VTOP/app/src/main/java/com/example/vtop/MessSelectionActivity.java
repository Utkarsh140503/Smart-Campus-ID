package com.example.vtop;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MessSelectionActivity extends AppCompatActivity {

    private String userID;
    private Spinner spinnerMess;
    private Spinner spinnerFoodPreference;
    private Button btnSubmit;
    private TextView txtSelectedData;

    private DatabaseReference databaseReference, databaseReference2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_selection);

        Intent intent = getIntent();
        userID = intent.getStringExtra("UserID");

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("MessSelection").child(userID);
        databaseReference2 = database.getReference("Student_Registrations").child(userID);

        TextView tvHeading = findViewById(R.id.tvHeading);
        spinnerMess = findViewById(R.id.spinnerMess);
        spinnerFoodPreference = findViewById(R.id.spinnerFoodPreference);
        btnSubmit = findViewById(R.id.btnSubmit);
        txtSelectedData = findViewById(R.id.txtSelectedData);

        // Set heading text
        tvHeading.setText("Mess Selection 2023 - 24");

        // Set adapter for messOptions array
        ArrayAdapter<CharSequence> messAdapter = ArrayAdapter.createFromResource(this, R.array.mess_options, android.R.layout.simple_spinner_item);
        messAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMess.setAdapter(messAdapter);

        // Set adapter for foodOptions array
        ArrayAdapter<CharSequence> foodAdapter = ArrayAdapter.createFromResource(this, R.array.food_options, android.R.layout.simple_spinner_item);
        foodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFoodPreference.setAdapter(foodAdapter);

        // Set click listener for btnSubmit
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get selected options
                String selectedMessOption = spinnerMess.getSelectedItem().toString();
                String selectedFoodOption = spinnerFoodPreference.getSelectedItem().toString();

                // Store selected data in Firebase Realtime Database
                databaseReference.child("Caterer").setValue(selectedMessOption);
                databaseReference.child("FoodPreference").setValue(selectedFoodOption);

                databaseReference2.child("Mess_Details").child("Caterer").setValue(selectedMessOption);
                databaseReference2.child("Mess_Details").child("FoodPreference").setValue(selectedFoodOption);

                Toast.makeText(MessSelectionActivity.this, "Mess Preference Stored Successfully!", Toast.LENGTH_SHORT).show();

                // Display selected data in TextView
                String selectedData = "Selected Mess: " + selectedMessOption + "\nSelected Food Preference: " + selectedFoodOption;
                txtSelectedData.setText(selectedData);
                txtSelectedData.setVisibility(View.VISIBLE);
            }
        });
    }
}
