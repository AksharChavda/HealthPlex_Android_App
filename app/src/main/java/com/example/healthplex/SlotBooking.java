package com.example.healthplex;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class SlotBooking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_booking);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            assert userEmail != null;
            DatabaseReference userSlotsRef = FirebaseDatabase.getInstance().getReference("userSlots").child(userEmail.replace(".", ","));
        }  // Handle the case where user is not authenticated


        displaySlots();
    }

    private void displaySlots() {
        LinearLayout timeSlotsLayout = findViewById(R.id.timeSlotsLayout);
        timeSlotsLayout.removeAllViews(); // Clear existing views

        String[] slots = {"6am - 7am", "7am - 8am", "8am - 9am", "9am - 10am"};

        for (String slot : slots) {
            // Inflate slot view from layout XML
            View slotView = getLayoutInflater().inflate(R.layout.slot_item, timeSlotsLayout, false);
            TextView slotTextView = slotView.findViewById(R.id.slotTextView);
            slotTextView.setText(slot);

            // Set click listener to book the slot
            slotView.setOnClickListener(view -> bookSlot(slot));

            // Add slot view to the layout
            timeSlotsLayout.addView(slotView);
        }
    }
    public static class SlotAvailability {
        private int bookedSeats;

        public SlotAvailability() {
            // Default constructor required for Firebase
        }

        public SlotAvailability(int bookedSeats) {
            this.bookedSeats = bookedSeats;
        }

        public int getBookedSeats() {
            return bookedSeats;
        }

        public void setBookedSeats(int bookedSeats) {
            this.bookedSeats = bookedSeats;
        }
    }

    private void initializeSlotsAvailability() {
        DatabaseReference slotsRef = FirebaseDatabase.getInstance().getReference("slots");

        Map<String, Object> initialSlotsAvailability = new HashMap<>();
        initialSlotsAvailability.put("6am - 7am", new SlotAvailability(0));
        initialSlotsAvailability.put("7am - 8am", new SlotAvailability(0));
        initialSlotsAvailability.put("8am - 9am", new SlotAvailability(0));
        initialSlotsAvailability.put("9am - 10am", new SlotAvailability(0));

        slotsRef.setValue(initialSlotsAvailability)
                .addOnSuccessListener(aVoid -> {
                    // Slots initialized successfully, update UI
                    displaySlots();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    Toast.makeText(SlotBooking.this, "Failed to initialize slots", Toast.LENGTH_SHORT).show();
                });
    }

    private void bookSlot(String slot) {
        // Get current date and time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());

        // Reference to the specific slot
        DatabaseReference slotRef = FirebaseDatabase.getInstance().getReference("slots").child(slot);

        // Check if the slot has already been booked
        slotRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Slot already booked
                    Toast.makeText(SlotBooking.this, "This slot is already booked.", Toast.LENGTH_SHORT).show();
                } else {
                    // Book the slot
                    Map<String, Object> slotBooking = new HashMap<>();
                    slotBooking.put("bookedSeats", 1);

                    // Create a user booking map with the current date and time and user email
                    Map<String, Object> userBooking = new HashMap<>();
                    userBooking.put(currentDateAndTime, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()); // Add user email

                    // Add the user booking to the slot booking
                    slotBooking.put("userBookings", userBooking);

                    // Update the database
                    slotRef.setValue(slotBooking).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(SlotBooking.this, "Slot booked successfully!", Toast.LENGTH_SHORT).show();
                            // Disable UI elements after booking the slot
                            disableSlotBooking();
                        } else {
                            Toast.makeText(SlotBooking.this, "Failed to book slot. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                /* Handle error */
                Toast.makeText(SlotBooking.this, "Failed to book slot. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to disable UI elements related to slot booking
    private void disableSlotBooking() {
        LinearLayout timeSlotsLayout = findViewById(R.id.timeSlotsLayout);
        for (int i = 0; i < timeSlotsLayout.getChildCount(); i++) {
            View slotView = timeSlotsLayout.getChildAt(i);
            slotView.setEnabled(false);
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
        }
    }

}
