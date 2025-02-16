package com.example.vtop;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnToggleSidebar, profileImg, selectImg;
    private TextView nameTV, regNoTV;


    private DatabaseReference attendanceRef;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedImageBitmap = null;

    private String regNo, proctorId;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnToggleSidebar = findViewById(R.id.imgToggleSidebar);
        nameTV = findViewById(R.id.textName);
        regNoTV = findViewById(R.id.textRegistration);
        profileImg = findViewById(R.id.imgProfile);
        selectImg = findViewById(R.id.selectImg);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions every time the activity starts
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);


        Intent intent = getIntent();
        String name = intent.getStringExtra("Name");
        regNo = intent.getStringExtra("ID");

        // Initialize Firebase database reference
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        // Get the proctor ID from Student_Registrations using the userID
        DatabaseReference proctorIdRef = mDatabase.child("Student_Registrations").child(regNo).child("proctorEID");
        proctorIdRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                proctorId = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to read proctor ID.", Toast.LENGTH_SHORT).show();
            }
        });

        nameTV.setText(name);
        regNoTV.setText(regNo);

        attendanceRef = FirebaseDatabase.getInstance().getReference().child("Student_Attendance");

        checkProfileImage();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // Handle navigation item clicks here
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_mark_attendance:
                        showAttendanceConfirmationDialog();
                        // showToast("Mark Attendance clicked");
                        break;
                    case R.id.nav_proctor:
                        showProctorDialog();
                        showToast("Proctor clicked");
                        break;
                    case R.id.nav_hostels:
                        showHostelOptionsDialog();
                        break;
                    case R.id.nav_my_account:
                        showMyAccountDialog();
                        break;
                    case R.id.payment_history:
                        showPaymentHistory();
                        break;
                    case R.id.attendance_tracking:
                        showAttendanceTracking();
                        break;
                    case R.id.RFIDGatePass:
                        showRFIDGatePass();
                        break;
                    case R.id.credit:
                        showCreditDialog();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        btnToggleSidebar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now proceed to mark attendance or any other location-related operations
            } else {
                // Permission denied, request the permission again
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            }
        }
    }

    private void showRFIDGatePass(){
        Intent i = new Intent(MainActivity.this, RFIDGatePassActivity.class);
        i.putExtra("UserID", regNo);
        startActivity(i);
    }

    private void showAttendanceTracking(){
        Intent i = new Intent(MainActivity.this, StudentAttendanceHistory.class);
        i.putExtra("UserID", regNo);
        startActivity(i);
    }

    private void showPaymentHistory(){
        Intent i = new Intent(MainActivity.this, StudentPaymentHistory.class);
        i.putExtra("UserID", regNo);
        startActivity(i);
    }

    private void showCreditDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Credit Options");
        String[] options = {"Add Credits", "Credit History"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which];
                showToast("Selected Option: " + selectedOption);

                // Redirect to the respective activity based on the selected option
                switch (selectedOption) {
                    case "Add Credits":
                        showAddCreditsDialog();
                        break;
                    case "Credit History":
                        Intent intent = new Intent(MainActivity.this, CreditHistoryActivity.class);
                        intent.putExtra("UserID", regNo);
                        startActivity(intent);
                        break;
                }
            }
        });
        builder.show();
    }

    private void showAddCreditsDialog() {
        // Create a dialog to prompt user for the credit number
        final EditText creditInput = new EditText(MainActivity.this);
        creditInput.setHint("Enter Credit Number");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Credits");
        builder.setView(creditInput);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String creditNumber = creditInput.getText().toString();

                if (creditNumber.isEmpty()) {
                    showToast("Please enter a valid number.");
                    return;
                }

                // Get the current user ID (assumed to be stored as regNo)
                String userId = regNo;  // Your logic to get the user ID

                // Fetch proctorEID from Firebase
                DatabaseReference proctorRef = FirebaseDatabase.getInstance().getReference()
                        .child("Student_Registrations")
                        .child(userId)
                        .child("proctorEID");

                proctorRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        // Retrieve proctorEID from Firebase
                        String proctorEID = dataSnapshot.getValue(String.class);

                        if (proctorEID != null) {
                            // Generate a random credit ID
                            String randomCreditId = FirebaseDatabase.getInstance().getReference()
                                    .push().getKey();

                            if (randomCreditId == null) {
                                showToast("Failed to generate a random credit ID.");
                                return;
                            }

                            // Create a map with the credit number and the initial status (false)
                            Map<String, Object> creditData = new HashMap<>();
                            creditData.put("number", creditNumber);
                            creditData.put("status", false);

                            // Reference to Firebase Realtime Database
                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
                            databaseRef.child("CreditRequests")
                                    .child(proctorEID)  // Proctor's ID
                                    .child(userId)      // User ID
                                    .child(randomCreditId) // Random Credit ID
                                    .setValue(creditData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                showToast("Credits request added successfully!");
                                            } else {
                                                showToast("Failed to add credits.");
                                            }
                                        }
                                    });
                        } else {
                            showToast("Proctor ID not found.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        showToast("Failed to fetch Proctor ID: " + databaseError.getMessage());
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }

    private void showAttendanceConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Attendance");
        builder.setMessage("Are you sure you want to mark your attendance?");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markAttendance();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @SuppressLint("MissingPermission")
    private void markAttendance() {
        // Get current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Get student ID (regNo)
        String studentId = regNo;

        // Check if location services are enabled
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Location services are disabled, prompt the user to enable them
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enable Location")
                    .setMessage("Location services are disabled. Please enable them to mark your attendance.")
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        // Request location updates
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses;
                    String cityName = "";

                    try {
                        addresses = geocoder.getFromLocation(latitude, longitude, 1);
                        if (!addresses.isEmpty()) {
                            cityName = addresses.get(0).getLocality();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Create a new attendance entry in the database
                    DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference()
                            .child("Student_Attendance").child(proctorId).child(studentId).child(currentDateAndTime);

                    // Prepare attendance details including location and city name
                    Map<String, Object> attendanceDetails = new HashMap<>();
                    attendanceDetails.put("dateAndTime", currentDateAndTime);
                    attendanceDetails.put("studentId", studentId);
                    attendanceDetails.put("proctorId", proctorId);
                    attendanceDetails.put("latitude", latitude);
                    attendanceDetails.put("longitude", longitude);
                    attendanceDetails.put("cityName", cityName);

                    // Push the attendance details to the database
                    attendanceRef.updateChildren(attendanceDetails)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MainActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(MainActivity.this, "Getting Location. Attendance will be marked soon!", Toast.LENGTH_SHORT).show();
                    requestLocationUpdates();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // Update interval in milliseconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        fusedLocationProviderClient.removeLocationUpdates(this); // Stop requesting updates
                        // Call markAttendance with the valid location
                        markAttendanceWithLocation(location);
                        break; // Break loop after obtaining a valid location
                    }
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void markAttendanceWithLocation(Location location) {
        // Get current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Get student ID (regNo)
        String studentId = regNo;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses;
        String cityName = "";

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (!addresses.isEmpty()) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a new attendance entry in the database
        DatabaseReference attendanceRef = FirebaseDatabase.getInstance().getReference()
                .child("Student_Attendance").child(proctorId).child(studentId).child(currentDateAndTime);

        // Prepare attendance details including location and city name
        Map<String, Object> attendanceDetails = new HashMap<>();
        attendanceDetails.put("dateAndTime", currentDateAndTime);
        attendanceDetails.put("studentId", studentId);
        attendanceDetails.put("proctorId", proctorId);
        attendanceDetails.put("latitude", latitude);
        attendanceDetails.put("longitude", longitude);
        attendanceDetails.put("cityName", cityName);

        // Push the attendance details to the database
        attendanceRef.updateChildren(attendanceDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Attendance marked successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to mark attendance.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showHostelOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hostel Options");
        String[] options = {"Online Booking", "Leave Request", "Mess Selection 2023-2024", "Caterer Change", "Attendance View"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which];
                showToast("Selected Option: " + selectedOption);

                // Redirect to the respective activity based on the selected option
                switch (selectedOption) {
                    case "Online Booking":
                        startActivity(new Intent(MainActivity.this, OnlineBookingActivity.class));
                        break;
                    case "Leave Request":
                        Intent intent = new Intent(MainActivity.this, LeaveRequestActivity.class);
                        intent.putExtra("UserID", regNo);
                        startActivity(intent);
                        break;
                    case "Mess Selection 2023-2024":
                        Intent intent1 = new Intent(MainActivity.this, MessSelectionActivity.class);
                        intent1.putExtra("UserID", regNo);
                        startActivity(intent1);
                        break;
                    case "Caterer Change":
                        // startActivity(new Intent(MainActivity.this, CatererChangeActivity.class));
                        break;
                    case "Attendance View":
                        // startActivity(new Intent(MainActivity.this, AttendanceViewActivity.class));
                        break;
                }
            }
        });
        builder.show();
    }

    private void showMyAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("My Account Options");
        String[] options = {"Change Password", "View Credentials"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which];
                showToast("Selected Option: " + selectedOption);

                // Redirect to the respective activity based on the selected option
                switch (selectedOption) {
                    case "Change Password":
                        Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                        intent.putExtra("UserID", regNo);
                        startActivity(intent);
                        break;
                    case "View Credentials":
                        Intent intent1 = new Intent(MainActivity.this, ViewCredentialsActvity.class);
                        intent1.putExtra("UserID", regNo);
                        startActivity(intent1);
                        break;
                }
            }
        });
        builder.show();
    }

    private void showProctorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Proctor ");
        String[] options = {"View Proctor Details", "View VTOP Message from Proctor"};
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedOption = options[which];
                showToast("Selected Option: " + selectedOption);

                // Redirect to the respective activity based on the selected option
                switch (selectedOption) {
                    case "View Proctor Details":
                        Intent intent = new Intent(MainActivity.this, ViewProctorDetailsActivity.class);
                        intent.putExtra("UserID", regNo);
                        startActivity(intent);
                        break;
                    case "View VTOP Message from Proctor":
                        Intent intent1 = new Intent(MainActivity.this, MessageFromProctorActivity.class);
                        intent1.putExtra("UserID", regNo);
                        startActivity(intent1);
                        break;
                }
            }
        });
        builder.show();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

                // check if image size exceeds 500KB and compress it accordingly
                int imageSize = getBitmapSize(selectedImageBitmap);
                if (imageSize > 500000) {
                    selectedImageBitmap = compressBitmap(selectedImageBitmap, imageSize);
                }

                profileImg.setImageBitmap(selectedImageBitmap);
                uploadImageToStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int getBitmapSize(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray.length;
    }

    private Bitmap compressBitmap(Bitmap imageBitmap, int imageSize) {
        float compressionRatio = 500000f / imageSize;
        return Bitmap.createScaledBitmap(imageBitmap, (int)(imageBitmap.getWidth() * compressionRatio), (int)(imageBitmap.getHeight() * compressionRatio), true);
    }

    private void uploadImageToStorage() {
        if (selectedImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            imageName = regNo + "_profile_img.jpg";

            StorageReference imageRef = storageReference.child(imageName);
            UploadTask uploadTask = imageRef.putBytes(imageData);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Log.d("Upload", "Image uploaded successfully");
                    Toast.makeText(MainActivity.this, "Image Updated Successfully!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
//                    Log.e("Upload", "Image upload failed: " + e.getMessage());
                    Toast.makeText(MainActivity.this, "Image Updation Failed!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkProfileImage() {
        imageName = regNo + "_profile_img.jpg";

        StorageReference imageRef = storageReference.child(imageName);
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(MainActivity.this)
                        .load(uri)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(profileImg);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                profileImg.setImageResource(R.mipmap.ic_launcher);
            }
        });
    }
}
