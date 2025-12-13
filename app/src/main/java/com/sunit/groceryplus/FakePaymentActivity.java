package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class FakePaymentActivity extends AppCompatActivity {

    private TextInputEditText cardNumberEt, cardExpiryEt, cardCvvEt, cardNameEt;
    private Button processPaymentBtn;
    private android.widget.TextView cardPreviewNumber, cardPreviewName, cardPreviewExpiry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_payment);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Checkout");
        }
        
        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Checkout");
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        initViews();
        setupTextWatchers();
        
        // Get amount
        double amount = getIntent().getDoubleExtra("amount", 0.0);
        if (amount > 0) {
            processPaymentBtn.setText("Pay ₹" + String.format("%.2f", amount));
        }
        
        setupListeners();
    }

    private void initViews() {
        cardNumberEt = findViewById(R.id.cardNumberEt);
        cardExpiryEt = findViewById(R.id.cardExpiryEt);
        cardCvvEt = findViewById(R.id.cardCvvEt);
        cardNameEt = findViewById(R.id.cardNameEt);
        processPaymentBtn = findViewById(R.id.processPaymentBtn);
        
        cardPreviewNumber = findViewById(R.id.cardPreviewNumber);
        cardPreviewName = findViewById(R.id.cardPreviewName);
        cardPreviewExpiry = findViewById(R.id.cardPreviewExpiry);
    }
    
    private void setupTextWatchers() {
        cardNumberEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String number = s.toString();
                if (number.isEmpty()) {
                    cardPreviewNumber.setText("**** **** **** ****");
                } else {
                    // Simple formatting for preview
                    StringBuilder formatted = new StringBuilder();
                    for (int i = 0; i < number.length(); i++) {
                        if (i > 0 && i % 4 == 0) formatted.append(" ");
                        formatted.append(number.charAt(i));
                    }
                    cardPreviewNumber.setText(formatted.toString());
                }
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        cardNameEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardPreviewName.setText(s.toString().isEmpty() ? "JOHN DOE" : s.toString().toUpperCase());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        cardExpiryEt.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cardPreviewExpiry.setText(s.toString().isEmpty() ? "MM/YY" : s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void setupListeners() {
        processPaymentBtn.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String cardNumber = cardNumberEt.getText().toString().trim();
        String expiry = cardExpiryEt.getText().toString().trim();
        String cvv = cardCvvEt.getText().toString().trim();
        String name = cardNameEt.getText().toString().trim();

        if (cardNumber.isEmpty() || expiry.isEmpty() || cvv.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cardNumber.length() < 12) {
            Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simulate network delay and success
        processPaymentBtn.setEnabled(false);
        processPaymentBtn.setText("Processing...");

        new Handler().postDelayed(() -> {
            Toast.makeText(FakePaymentActivity.this, "Payment Authorized", Toast.LENGTH_SHORT).show();
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("status", "success");
            setResult(RESULT_OK, resultIntent);
            finish();
        }, 1500); // 1.5 seconds delay
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
