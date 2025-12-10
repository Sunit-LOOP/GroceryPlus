package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserHomeActivity extends AppCompatActivity {

    private static final String TAG = "UserHomeActivity";
    private RecyclerView categoriesRv;
    private RecyclerView productsRv;
    private LinearLayout navHome;
    private LinearLayout navMessage;
    private LinearLayout navHistory;
    private LinearLayout navCart;
    private LinearLayout navProfile;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Log.e(TAG, "Invalid user ID received");
        }

        // Initialize views
        initViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Set bottom navigation listener
        setBottomNavigationListener();
    }

    private void initViews() {
        categoriesRv = findViewById(R.id.categoriesRv);
        productsRv = findViewById(R.id.productsRv);
        navHome = findViewById(R.id.navHome);
        navMessage = findViewById(R.id.navMessage);
        navHistory = findViewById(R.id.navHistory);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerViews() {
        // Setup categories RecyclerView
        categoriesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // TODO: Set adapter for categories

        // Setup products RecyclerView
        productsRv.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Set adapter for products
    }

    private void setBottomNavigationListener() {
        // Set click listeners for custom bottom navigation
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on home screen - no action needed
            }
        });

        navMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to message activity
                Intent intent = new Intent(UserHomeActivity.this, MessageActivity.class);
                startActivity(intent);
            }
        });

        navHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to history activity
                Intent intent = new Intent(UserHomeActivity.this, OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        navCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to cart activity
                Intent intent = new Intent(UserHomeActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to user detail view activity
                Intent intent = new Intent(UserHomeActivity.this, UserDetailViewActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }
}