package com.example.vtop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EmployeeRegistrationActivity extends AppCompatActivity {

    private static final int IMAGE_REQUEST_CODE = 1;

    private EditText editTextEmployeeNumber, editTextName, editTextPassword;
    private ImageView imageViewTeacher;
    private Uri imageUri;
    private Button btnSubmit;
    private TextView loginRedirect, textViewGoToHomePage;

    private DatabaseReference databaseReference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_registration);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Employee_Registrations");

        editTextEmployeeNumber = findViewById(R.id.editTextEmployeeNumber);
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        loginRedirect = findViewById(R.id.textViewAlreadyRegistered);
        textViewGoToHomePage = findViewById(R.id.textViewGoToHomePage);
        imageViewTeacher = findViewById(R.id.imageViewTeacher);

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

        // Set click listener for the teacher image view to pick an image
        imageViewTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewTeacher.setImageURI(imageUri);
        }
    }

    private void uploadTeacherImage(String teacherID, String teacherName, String teacherSubject, String teacherPhone, String teacherEmail, String teacherPassword) {
        // Get the reference to Firebase Storage
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child("teacher_images").child(teacherID + ".jpg");

        // Upload the image
        UploadTask uploadTask = storageReference.putFile(imageUri);

        // Get the download URL for the uploaded image
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null) {
                        String imageUrl = downloadUri.toString();
                        registerUser();
                    } else {
                        Toast.makeText(EmployeeRegistrationActivity.this, "Failed to upload teacher image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EmployeeRegistrationActivity.this, "Failed to upload teacher image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser() {
        String empNumber = editTextEmployeeNumber.getText().toString().trim().toUpperCase();
        String name = editTextName.getText().toString().trim().toUpperCase();
        String password = editTextPassword.getText().toString().trim();

        if (!empNumber.isEmpty() && !name.isEmpty() && !password.isEmpty() && imageUri != null) {
//            String id = databaseReference.push().getKey();
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReference().child(empNumber + "_profile_image.jpg");
            UploadTask uploadTask = storageReference.putFile(imageUri);

            EmployeeRegistrationActivity.Registration registration = new Registration(empNumber, name, password);

//            if (id != null) {
//                databaseReference.child(id).setValue(registration);
//                redirectToLogin();
//            }
            databaseReference.child(empNumber).setValue(registration);
            redirectToLogin();
        }else{
            Toast.makeText(this, "Please fill all the details including the image", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToLogin() {
        Intent intent = new Intent(EmployeeRegistrationActivity.this, EmployeeLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToHomePage() {
        Intent intent = new Intent(EmployeeRegistrationActivity.this, StartupActivity.class);
        startActivity(intent);
        finishAffinity();
    }

    // Registration data model
    private static class Registration {
        private String empNumber;
        private String name;
        private String password;

        public Registration() {
            // Default constructor required for calls to DataSnapshot.getValue(Registration.class)
        }

        public Registration(String empNumber, String name, String password) {
            this.empNumber = empNumber;
            this.name = name;
            this.password = password;
        }

        public String getEmpNumber() {
            return empNumber;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }
    }
}