package com.example.vtop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class StudentRegistrationActivity extends AppCompatActivity {

    private EditText editTextRFID, editTextMobNum, editTextRegNumber, editTextName, editTextProctor, editTextProctorEID, editTextPassword, editTextParentName, editTextParentEmail;
    private Button btnSubmit;
    private TextView loginRedirect, textViewGoToHomePage;

    private DatabaseReference studentDatabaseReference;
    private DatabaseReference parentDatabaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_registration);

        // Initialize Firebase database references
        studentDatabaseReference = FirebaseDatabase.getInstance().getReference("Student_Registrations");
        parentDatabaseReference = FirebaseDatabase.getInstance().getReference("ParentRegistration");

        editTextRFID = findViewById(R.id.editTextRFID);
        editTextMobNum = findViewById(R.id.editTextMobNum);
        editTextRegNumber = findViewById(R.id.editTextRegNumber);
        editTextName = findViewById(R.id.editTextName);
        editTextProctor = findViewById(R.id.editTextProctor);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextProctorEID = findViewById(R.id.editTextProctorEID);
        editTextParentName = findViewById(R.id.editTextParentName);
        editTextParentEmail = findViewById(R.id.editTextParentEmail);
        btnSubmit = findViewById(R.id.btnSubmit);
        loginRedirect = findViewById(R.id.textViewAlreadyRegistered);
        textViewGoToHomePage = findViewById(R.id.textViewGoToHomePage);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        loginRedirect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                redirectToLogin();
            }
        });

        textViewGoToHomePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHomePage();
            }
        });
    }

    private void registerUser() {
        String rfid = editTextRFID.getText().toString().trim().toUpperCase();
        String regNumber = editTextRegNumber.getText().toString().trim().toUpperCase();
        String name = editTextName.getText().toString().trim().toUpperCase();
        String proctor = editTextProctor.getText().toString().trim().toUpperCase();
        String password = editTextPassword.getText().toString().trim();
        String proctorEID = editTextProctorEID.getText().toString();
        String parentName = editTextParentName.getText().toString().trim();
        String parentEmail = editTextParentEmail.getText().toString().trim();
        String mobileNum = editTextMobNum.getText().toString().trim();

        if (!mobileNum.isEmpty() && !rfid.isEmpty() && !regNumber.isEmpty() && !name.isEmpty() && !proctor.isEmpty() && !proctorEID.isEmpty() && !password.isEmpty() &&
                !parentName.isEmpty() && parentEmail.endsWith("@gmail.com")) {

            // Check if the proctor ID is valid
            DatabaseReference employeeReference = FirebaseDatabase.getInstance().getReference("Employee_Registrations").child(proctorEID);
            employeeReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Proctor ID is valid, proceed with registration
                        Registration registration = new Registration(mobileNum, rfid, regNumber, name, proctor, proctorEID, password, parentName, parentEmail, 0);
                        studentDatabaseReference.child(regNumber).setValue(registration);

                        // Register parent
                        String parentID = parentEmail.split("@")[0]; // Extract part before '@'
                        String randomPassword = generateRandomPassword();
                        ParentRegistration parentRegistration = new ParentRegistration(parentName, randomPassword, regNumber);
                        parentDatabaseReference.child(parentID).setValue(parentRegistration);

                        Toast.makeText(StudentRegistrationActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        redirectToLogin();
                    } else {
                        Toast.makeText(StudentRegistrationActivity.this, "Invalid Proctor ID", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(StudentRegistrationActivity.this, "Error checking Proctor ID", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Fill All Details Correctly!", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(StudentRegistrationActivity.this, StudentLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHomePage() {
        Intent intent = new Intent(StudentRegistrationActivity.this, StartupActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    private static class Registration {
        private String rfid;
        private String regNumber;
        private String name;
        private String proctor;
        private String proctorEID;
        private String password;
        private String parentName;
        private String parentEmail;
        private String mobileNum;
        private int credits; // New field

        // Default constructor required for Firebase serialization
        public Registration() {
        }

        // Parameterized constructor
        public Registration(String mobileNum, String rfid, String regNumber, String name, String proctor, String proctorEID, String password, String parentName, String parentEmail, int credits) {
            this.mobileNum = mobileNum;
            this.rfid = rfid;
            this.regNumber = regNumber;
            this.name = name;
            this.proctor = proctor;
            this.proctorEID = proctorEID;
            this.password = password;
            this.parentName = parentName;
            this.parentEmail = parentEmail;
            this.credits = credits; // Initialize credits
        }

        // Getters and Setters
        public String getMobileNum() {
            return mobileNum;
        }

        public void setMobileNum(String mobileNum) {
            this.mobileNum = mobileNum;
        }

        public String getRfid() {
            return rfid;
        }

        public void setRfid(String rfid) {
            this.rfid = rfid;
        }

        public String getRegNumber() {
            return regNumber;
        }

        public void setRegNumber(String regNumber) {
            this.regNumber = regNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getProctor() {
            return proctor;
        }

        public void setProctor(String proctor) {
            this.proctor = proctor;
        }

        public String getProctorEID() {
            return proctorEID;
        }

        public void setProctorEID(String proctorEID) {
            this.proctorEID = proctorEID;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getParentName() {
            return parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }

        public String getParentEmail() {
            return parentEmail;
        }

        public void setParentEmail(String parentEmail) {
            this.parentEmail = parentEmail;
        }

        public int getCredits() {
            return credits;
        }

        public void setCredits(int credits) {
            this.credits = credits;
        }
    }

    private static class ParentRegistration {
        private String parentName;
        private String password;
        private String studentRegNumber;

        // Default constructor required for Firebase serialization
        public ParentRegistration() {
        }

        // Parameterized constructor
        public ParentRegistration(String parentName, String password, String studentRegNumber) {
            this.parentName = parentName;
            this.password = password;
            this.studentRegNumber = studentRegNumber;
        }

        // Getters and Setters
        public String getParentName() {
            return parentName;
        }

        public void setParentName(String parentName) {
            this.parentName = parentName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getStudentRegNumber() {
            return studentRegNumber;
        }

        public void setStudentRegNumber(String studentRegNumber) {
            this.studentRegNumber = studentRegNumber;
        }
    }
}
