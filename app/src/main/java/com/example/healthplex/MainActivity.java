package com.example.healthplex;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonProfile = findViewById(R.id.buttonProfile);
        Button buttonBookSlot = findViewById(R.id.buttonBookSlot);
        Button buttonAttendance = findViewById(R.id.buttonAttendance);
        WebView webView = findViewById(R.id.webView);
        String video="<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/hVizUR70M0s?si=4Ioq3UhZgk_4pDeA\" title=\"YouTube video player\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>";
        webView.loadData(video,"text/html","utf-8");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        buttonProfile.setOnClickListener(v -> openProfileActivity());
        buttonBookSlot.setOnClickListener(v -> openSlotBookingActivity());
        buttonAttendance.setOnClickListener(v -> openAttendanceActivity());
    }

    private void openProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openSlotBookingActivity() {
        Intent intent = new Intent(this, SlotBooking.class);
        startActivity(intent);
    }

    private void openAttendanceActivity(){
        Intent intent=new Intent(this,Attendance.class);
        startActivity(intent);
    }

}


