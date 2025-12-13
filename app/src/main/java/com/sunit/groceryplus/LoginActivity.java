package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView signupTextView;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);

        // Initialize views
        initViews();

        // Initialize repository
        userRepository = new UserRepository(this);

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.signupTextView);
    }

    private void setClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    private void performLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            passwordEditText.requestFocus();
            return;
        }

        // Perform login
        User user = userRepository.loginUser(email, password);
        if (user != null) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "User logged in: " + user.getName() + " (" + user.getUserType() + ")");

            // Save session
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userId", user.getUserId());
            editor.putString("userName", user.getName());
            editor.putString("userEmail", user.getEmail());
            editor.putString("userType", user.getUserType());
            editor.apply();

            // Navigate to appropriate screen based on user type
            if (user.isAdmin()) {
                Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                intent.putExtra("user_id", user.getUserId());
                startActivity(intent);
            } else {
                Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);
                intent.putExtra("user_id", user.getUserId());
                startActivity(intent);
            }
            finish();
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Login failed for email: " + email);
        }
    }
}