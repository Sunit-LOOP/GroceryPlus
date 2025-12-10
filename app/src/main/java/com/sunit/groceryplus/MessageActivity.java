package com.sunit.groceryplus;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private TextInputEditText messageEditText;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
    }

    private void setClickListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        
        if (message.isEmpty()) {
            messageEditText.setError("Please enter a message");
            messageEditText.requestFocus();
            return;
        }
        
        // TODO: Implement actual message sending logic
        Toast.makeText(this, "Message sent successfully!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Message sent: " + message);
        
        // Clear the message field
        messageEditText.setText("");
    }
}