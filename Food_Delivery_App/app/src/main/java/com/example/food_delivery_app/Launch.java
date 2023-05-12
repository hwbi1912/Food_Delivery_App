package com.example.food_delivery_app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Launch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        Thread timing = new Thread() {
            public void run() {
                try {
                    sleep(1000);
                } catch (Exception e) {
                    Log.d("Test", "Error Start");
                } finally {
                    Intent intent = new Intent(Launch.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_in_right, R.anim.anim_out_left);
                }
            }
        };
        timing.start();
    }

    protected void onPause() {
        super.onPause();
        finish();
    }
}