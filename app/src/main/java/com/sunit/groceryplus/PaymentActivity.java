package com.sunit.groceryplus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.sunit.groceryplus.models.CartItem;
import com.sunit.groceryplus.network.ApiClient;
import com.sunit.groceryplus.network.PaymentIntentRequest;
import com.sunit.groceryplus.network.PaymentIntentResponse;
import com.sunit.groceryplus.utils.Config;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    // UI Elements
    private TextView totalAmountTv, summarySubtotal;
    private Button payNowBtn;
    
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;
    private double finalAmount = 0.0;
    private int userId = -1;
    
    // Database helper
    private com.sunit.groceryplus.DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        
        // Initialize Stripe
        PaymentConfiguration.init(this, Config.STRIPE_PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);
        
        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize database helper
        dbHelper = new com.sunit.groceryplus.DatabaseHelper(this);
        
        initViews();
        setupToolbar();
        loadCartData();
    }
    
    private void initViews() {
        totalAmountTv = findViewById(R.id.paymentTotalAmount);
        summarySubtotal = findViewById(R.id.summarySubtotal);
        payNowBtn = findViewById(R.id.paymentPayNowBtn);
        
        // Pay button click listener
        payNowBtn.setOnClickListener(v -> startStripePayment());
    }
    
    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.paymentToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void loadCartData() {
        // Simple mock cart data for now
        double subtotal = 100.0; // Mock subtotal
        double deliveryFee = 20.0;
        finalAmount = subtotal + deliveryFee;
        
        // Update UI
        summarySubtotal.setText("₹" + String.format("%.2f", subtotal));
        totalAmountTv.setText("₹" + String.format("%.2f", finalAmount));
        
        // Enable pay button
        payNowBtn.setEnabled(true);
        payNowBtn.setText("Pay ₹" + String.format("%.2f", finalAmount) + " with Stripe");
    }
    
    private void startStripePayment() {
        Log.d(TAG, "Starting Stripe payment");
        
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network and try again.", Toast.LENGTH_LONG).show();
            return;
        }
        
        // Validate minimum amount
        if (finalAmount < 50) { // ₹0.50 minimum
            Toast.makeText(this, "Minimum amount is ₹0.50", Toast.LENGTH_SHORT).show();
            return;
        }
        
        payNowBtn.setEnabled(false);
        payNowBtn.setText("Creating Payment...");
        
        createPaymentIntent();
    }
    
    private void createPaymentIntent() {
        Log.d(TAG, "Creating PaymentIntent for amount: " + finalAmount);
        
        // Convert amount to cents (Stripe expects amount in smallest currency unit)
        int amountInCents = (int) (finalAmount * 100);
        
        PaymentIntentRequest request = new PaymentIntentRequest(amountInCents, "inr");
        
        ApiClient.getStripeApi().createPaymentIntent(request)
            .enqueue(new Callback<PaymentIntentResponse>() {
                @Override
                public void onResponse(Call<PaymentIntentResponse> call, Response<PaymentIntentResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        paymentIntentClientSecret = response.body().getClientSecret();
                        Log.d(TAG, "PaymentIntent created successfully");
                        presentPaymentSheet();
                    } else {
                        String errorMsg = "Failed to create payment intent";
                        Log.e(TAG, errorMsg + " (Code: " + response.code() + ")");
                        Toast.makeText(PaymentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        resetPayButton();
                    }
                }
                
                @Override
                public void onFailure(Call<PaymentIntentResponse> call, Throwable t) {
                    Log.e(TAG, "Network error creating PaymentIntent", t);
                    String errorMsg = "Network error: " + t.getMessage();
                    
                    // More specific timeout and network error handling
                    if (t instanceof java.net.SocketTimeoutException) {
                        errorMsg = "Payment request timed out. Please check your connection and try again.";
                    } else if (t instanceof java.net.ConnectException) {
                        errorMsg = "Cannot connect to payment server. Please check internet connection.";
                    } else if (t instanceof java.net.UnknownHostException) {
                        errorMsg = "Payment server not found. Please check server URL.";
                    } else if (t instanceof java.io.IOException) {
                        errorMsg = "Network error. Please check your internet connection.";
                    }
                    
                    Toast.makeText(PaymentActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    resetPayButton();
                }
            });
    }
    
    private void presentPaymentSheet() {
        if (paymentIntentClientSecret != null) {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration("GroceryPlus");
            paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
        } else {
            Log.e(TAG, "Cannot present PaymentSheet - client secret is null");
            Toast.makeText(this, "Payment configuration error", Toast.LENGTH_SHORT).show();
            resetPayButton();
        }
    }
    
    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        resetPayButton();

        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            Toast.makeText(this, "Payment successfully completed!", Toast.LENGTH_SHORT).show();
            createOrder("stripe");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.d(TAG, "Payment canceled");
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) paymentSheetResult).getError();
            Log.e(TAG, "Payment failed", error);
            Toast.makeText(this, "Payment failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void createOrder(String paymentMethod) {
        Log.d(TAG, "Creating order with payment method: " + paymentMethod);
        
        // Create order using direct database method
        long orderId = dbHelper.createOrder(userId, finalAmount, "PENDING", -1);
        
        if (orderId != -1) {
            // Show success message
            String message = "Order placed successfully!";
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            
            // Navigate to order success
            Intent intent = new Intent(PaymentActivity.this, com.sunit.groceryplus.OrderSuccessActivity.class);
            intent.putExtra("user_id", userId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error creating order", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void resetPayButton() {
        payNowBtn.setEnabled(true);
        payNowBtn.setText("Pay ₹" + String.format("%.2f", finalAmount) + " with Stripe");
    }
    
    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}