package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.sunit.groceryplus.models.CartItem;
import com.sunit.groceryplus.models.Order;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private static final int FAKE_PAYMENT_REQUEST_CODE = 1001;

    // UI Elements
    private RadioGroup paymentMethodGroup;
    private MaterialCardView stripeCard, codCard;
    private android.widget.RadioButton paymentStripeRb, paymentCodRb;
    
    private TextView totalAmountTv, subtotalAmountTv;
    private Button payNowBtn;
    
    // Data
    private int userId;
    private double totalAmount;
    private double subtotalAmount;
    private String selectedPaymentMethod = "stripe"; // default
    
    private CartRepository cartRepository;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = sharedPreferences.getInt("userId", -1);
        }

        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
        subtotalAmount = getIntent().getDoubleExtra("subtotal_amount", totalAmount - 50.0); // Default to total minus delivery if not provided
        int totalItems = getIntent().getIntExtra("total_items", 0);

        if (userId == -1) {
            Toast.makeText(this, "Error: Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repositories
        cartRepository = new CartRepository(this);
        orderRepository = new OrderRepository(this);

        // Initialize views
        initViews();

        // Setup payment method selection
        setupPaymentMethodSelection();

        // Display order summary
        displayOrderSummary();

        // Set click listeners
        payNowBtn.setOnClickListener(v -> processPayment());
    }

    private void initViews() {
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        stripeCard = findViewById(R.id.stripeCard);
        codCard = findViewById(R.id.codCard);
        paymentStripeRb = findViewById(R.id.creditCardRadio);
        paymentCodRb = findViewById(R.id.cashOnDeliveryRadio);
        
        totalAmountTv = findViewById(R.id.paymentTotalAmount);
        subtotalAmountTv = findViewById(R.id.summarySubtotal);
        payNowBtn = findViewById(R.id.paymentPayNowBtn);
        
        // Setup Toolbar
        setSupportActionBar(findViewById(R.id.paymentToolbar));
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupPaymentMethodSelection() {
        View.OnClickListener stripeListener = v -> selectStripe();
        View.OnClickListener codListener = v -> selectCod();
        
        stripeCard.setOnClickListener(stripeListener);
        paymentStripeRb.setOnClickListener(stripeListener);
        
        codCard.setOnClickListener(codListener);
        paymentCodRb.setOnClickListener(codListener);
        
        // Initial state
        if (selectedPaymentMethod.equals("stripe")) {
             selectStripe();
        } else {
             selectCod();
        }
    }

    private void selectStripe() {
        selectedPaymentMethod = "stripe";
        paymentStripeRb.setChecked(true);
        paymentCodRb.setChecked(false);
        
        highlightCard(stripeCard, true);
        highlightCard(codCard, false);
        
        updatePayButtonText();
    }

    private void selectCod() {
        selectedPaymentMethod = "cod";
        paymentStripeRb.setChecked(false);
        paymentCodRb.setChecked(true);
        
        highlightCard(stripeCard, false);
        highlightCard(codCard, true);
        
        updatePayButtonText();
    }
    
    private void updatePayButtonText() {
        if (payNowBtn == null) return;
        
        if (selectedPaymentMethod.equals("stripe")) {
            payNowBtn.setText("Pay ₹" + String.format("%.2f", totalAmount));
        } else {
            payNowBtn.setText("Confirm Order");
        }
    }
    
    private void highlightCard(MaterialCardView card, boolean isSelected) {
        if (isSelected) {
            card.setStrokeColor(getResources().getColor(android.R.color.holo_green_dark));
            card.setStrokeWidth(4);
        } else {
            card.setStrokeColor(getResources().getColor(android.R.color.darker_gray));
            card.setStrokeWidth(2);
        }
    }

    private void displayOrderSummary() {
        double deliveryFee = 50.0;
        // Display subtotal and delivery fee separately
        subtotalAmountTv.setText("₹" + String.format("%.2f", subtotalAmount)); 
        totalAmountTv.setText("₹" + String.format("%.2f", totalAmount));
        updatePayButtonText();
    }

    private void processPayment() {
        if (selectedPaymentMethod.equals("stripe")) {
            Toast.makeText(this, "Starting Stripe Payment...", Toast.LENGTH_SHORT).show();
            // Launch Fake Payment Screen
            Intent intent = new Intent(this, FakePaymentActivity.class);
            intent.putExtra("amount", totalAmount);
            startActivityForResult(intent, FAKE_PAYMENT_REQUEST_CODE);
        } else {
            processCODPayment();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == FAKE_PAYMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                String status = data.getStringExtra("status");
                if ("success".equals(status)) {
                    createOrder("stripe");
                } else {
                    Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void processCODPayment() {
        Toast.makeText(this, "Processing Cash on Delivery order...", Toast.LENGTH_SHORT).show();
        createOrder("cod");
    }

    private void createOrder(String paymentMethod) {
        try {
            // Get cart items
            List<CartItem> cartItems = cartRepository.getCartItems(userId);
            
            if (cartItems == null || cartItems.isEmpty()) {
                Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Create order
            long orderId = orderRepository.createOrder(userId, totalAmount, Order.STATUS_PENDING);
            
            if (orderId != -1) {
                // Add order items
                boolean allItemsAdded = true;
                for (CartItem item : cartItems) {
                    boolean success = orderRepository.addOrderItem(
                        (int) orderId,
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                    );
                    if (!success) {
                        allItemsAdded = false;
                        break;
                    }
                }
                
                if (allItemsAdded) {
                    cartRepository.clearCart(userId);
                    
                    // Record payment
                    String transactionId = "TXN" + System.currentTimeMillis();
                    orderRepository.recordPayment((int) orderId, totalAmount, paymentMethod, transactionId);
                    
                    String message = paymentMethod.equals("stripe") 
                        ? "Payment successful! Order placed." 
                        : "Order placed successfully! Pay on delivery.";
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate to Order Success Screen
                    Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    
                    // Notification handled by OrderRepository
                    
                    finish();
                } else {
                    Toast.makeText(this, "Error creating order items", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            Toast.makeText(this, "Error processing payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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