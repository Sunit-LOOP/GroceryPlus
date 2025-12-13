package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;


public class OrderSuccessActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable = () -> {
        navigateToHome();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);
        
        // Start timer
        handler.postDelayed(navigateRunnable, 3000);
    }

    private void navigateToHome() {
        int userId = getIntent().getIntExtra("user_id", -1);
        Intent intent = new Intent(OrderSuccessActivity.this, UserHomeActivity.class);
        intent.putExtra("user_id", userId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Prevent navigation if activity is destroyed
        handler.removeCallbacks(navigateRunnable);
    }
    
    @Override
    public void onBackPressed() {
        // Go home immediately on back press instead of waiting or exiting
        handler.removeCallbacks(navigateRunnable);
        navigateToHome();
    }
}
