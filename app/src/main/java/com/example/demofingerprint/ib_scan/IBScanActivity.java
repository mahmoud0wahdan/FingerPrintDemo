package com.example.demofingerprint.ib_scan;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demofingerprint.R;


public class IBScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ibscan);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}