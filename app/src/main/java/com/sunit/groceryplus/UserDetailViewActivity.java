package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class UserDetailViewActivity extends AppCompatActivity {

    private static final String TAG = "UserDetailViewActivity";
    private UserRepository userRepository;
    private int userId;

    // UI Elements
    private ImageButton backButton;
    private TextView userNameText;
    private TextView userEmailText;
    private TextView userPhoneText;
    private TextView fullNameValue;
    private TextView emailValue;
    private TextView phoneValue;
    private TextView userTypeValue;
    private ImageView settingsIcon;
    private ImageView userProfileImage;
    private MaterialButton editProfileButton;
    private MaterialButton changePasswordButton;
    private MaterialButton logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail_view);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Log.e(TAG, "Invalid user ID received");
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repository
        userRepository = new UserRepository(this);

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        userNameText = findViewById(R.id.userNameText);
        userEmailText = findViewById(R.id.userEmailText);
        userPhoneText = findViewById(R.id.userPhoneText);
        fullNameValue = findViewById(R.id.fullNameValue);
        emailValue = findViewById(R.id.emailValue);
        phoneValue = findViewById(R.id.phoneValue);
        userTypeValue = findViewById(R.id.userTypeValue);
        settingsIcon = findViewById(R.id.settingsIcon);
        userProfileImage = findViewById(R.id.userProfileImage);
        editProfileButton = findViewById(R.id.editProfileButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        logoutButton = findViewById(R.id.logoutButton);
    }

    private void loadUserData() {
        try {
            User user = userRepository.getUserById(userId);
            if (user != null) {
                // Update UI with user data
                userNameText.setText(user.getName());
                userEmailText.setText(user.getEmail());
                userPhoneText.setText(user.getPhone());
                fullNameValue.setText(user.getName());
                emailValue.setText(user.getEmail());
                phoneValue.setText(user.getPhone());
                userTypeValue.setText(user.getUserType().substring(0, 1).toUpperCase() + user.getUserType().substring(1));
            } else {
                Log.e(TAG, "User not found with ID: " + userId);
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data", e);
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setClickListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close the activity and go back
                finish();
            }
        });

        settingsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to settings activity
                Intent intent = new Intent(UserDetailViewActivity.this, UserSettingViewActivity.class);
                startActivity(intent);
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement edit profile functionality
                Toast.makeText(UserDetailViewActivity.this, "Edit Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement change password functionality
                Toast.makeText(UserDetailViewActivity.this, "Change Password clicked", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logout and navigate to login screen
                Toast.makeText(UserDetailViewActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserDetailViewActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}