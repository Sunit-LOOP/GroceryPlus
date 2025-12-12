package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Get user_id to pass back to home
        int userId = getIntent().getIntExtra("user_id", -1);

        // Wait 3 seconds then navigate to home
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(OrderSuccessActivity.this, UserHomeActivity.class);
            intent.putExtra("user_id", userId);
            // Clear back stack so user can't go back to success screen
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 3000);
    }
}
