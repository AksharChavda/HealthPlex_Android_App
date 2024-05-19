package com.example.healthplex;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Attendance extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private static final double GYM_LATITUDE = 22.38569766992681;
    private static final double GYM_LONGITUDE = 73.14499269455816;
    private static final float MAX_DISTANCE = 200; // in meters

    private FirebaseAuth auth;
    private DatabaseReference locationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        auth = FirebaseAuth.getInstance();
        locationRef = FirebaseDatabase.getInstance().getReference("userLocations");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnMarkAttendance.setOnClickListener(v -> markAttendance());
    }

    private void markAttendance() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        long timestamp = location.getTime();
                        float[] results = new float[1];
                        Location.distanceBetween(latitude, longitude, GYM_LATITUDE, GYM_LONGITUDE, results);
                        float distance = results[0];

                        if (distance <= MAX_DISTANCE) {
                            // Store the location
                            storeLocation(location);
                            Toast.makeText(Attendance.this, "Attendance marked", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Attendance.this, "You are not at the gym", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Attendance.this, "Unable to retrieve location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeLocation(Location location) {
        if (location == null || auth.getCurrentUser() == null || auth.getCurrentUser().getEmail() == null) {
            return;
        }

        // Get the user's email ID
        String userEmail = auth.getCurrentUser().getEmail();

        // Store location data in Firebase with the reference of the user's email ID
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());

        // Store location data in Firebase with the reference of the user's email ID
        DatabaseReference userLocationRef = locationRef.child(getEmailAsKey(userEmail)).push();

        userLocationRef.child("latitude").setValue(location.getLatitude());
        userLocationRef.child("longitude").setValue(location.getLongitude());
        userLocationRef.child("accuracy").setValue(location.getAccuracy());
        userLocationRef.child("timestamp").setValue(currentDateTime); // Store timestamp

        // Print the date and time for debugging

        Log.d(TAG, "Location stored at: " + currentDateTime);



    }
    // Helper method to convert email to a valid Firebase key
    private String getEmailAsKey(String email) {
        return email.replace('.', ','); // Replace '.' with ',' to create a valid Firebase key
    }
}
