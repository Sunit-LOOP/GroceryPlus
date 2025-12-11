package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sunit.groceryplus.models.CartItem;
import com.sunit.groceryplus.models.Order;

import java.util.List;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    
    // Stripe API keys
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_REPLACE_WITH_YOUR_PUBLISHABLE_KEY";
    private static final String STRIPE_SECRET_KEY = "sk_test_REPLACE_WITH_YOUR_SECRET_KEY";
    
    // UI Elements
    private RadioGroup paymentMethodGroup;
    private RadioButton creditCardRadio;
    private RadioButton cashOnDeliveryRadio;
    
    private TextInputLayout cardNumberLayout;
    private TextInputLayout expiryDateLayout;
    private TextInputLayout cvvLayout;
    private TextInputLayout cardholderNameLayout;
    
    private TextInputEditText cardNumberEt;
    private TextInputEditText expiryDateEt;
    private TextInputEditText cvvEt;
    private TextInputEditText cardholderNameEt;
    
    private TextView totalAmountTv;
    private Button payNowBtn;
    
    // Data
    private int userId;
    private double totalAmount;
    private int totalItems;
    private String selectedPaymentMethod = "stripe"; // default
    
    private CartRepository cartRepository;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        userId = getIntent().getIntExtra("user_id", -1);
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
        totalItems = getIntent().getIntExtra("total_items", 0);

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

        // Set click listeners
        setClickListeners();

        // Display order summary
        displayOrderSummary();
    }

    private void initViews() {
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        creditCardRadio = findViewById(R.id.creditCardRadio);
        cashOnDeliveryRadio = findViewById(R.id.cashOnDeliveryRadio);
        
        cardNumberLayout = findViewById(R.id.paymentCardNumberLayout);
        expiryDateLayout = findViewById(R.id.paymentExpiryDateLayout);
        cvvLayout = findViewById(R.id.paymentCvvLayout);
        cardholderNameLayout = findViewById(R.id.paymentCardholderNameLayout);
        
        cardNumberEt = findViewById(R.id.paymentCardNumber);
        expiryDateEt = findViewById(R.id.paymentExpiryDate);
        cvvEt = findViewById(R.id.paymentCvv);
        cardholderNameEt = findViewById(R.id.paymentCardholderName);
        
        totalAmountTv = findViewById(R.id.paymentTotalAmount);
        payNowBtn = findViewById(R.id.paymentPayNowBtn);
    }

    private void setupPaymentMethodSelection() {
        paymentMethodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.creditCardRadio) {
                    selectedPaymentMethod = "stripe";
                    showCardFields();
                } else if (checkedId == R.id.cashOnDeliveryRadio) {
                    selectedPaymentMethod = "cod";
                    hideCardFields();
                }
            }
        });
        
        // Show card fields by default
        showCardFields();
    }

    private void showCardFields() {
        cardNumberLayout.setVisibility(View.VISIBLE);
        expiryDateLayout.setVisibility(View.VISIBLE);
        cvvLayout.setVisibility(View.VISIBLE);
        cardholderNameLayout.setVisibility(View.VISIBLE);
    }

    private void hideCardFields() {
        cardNumberLayout.setVisibility(View.GONE);
        expiryDateLayout.setVisibility(View.GONE);
        cvvLayout.setVisibility(View.GONE);
        cardholderNameLayout.setVisibility(View.GONE);
    }

    private void displayOrderSummary() {
        totalAmountTv.setText("Total Amount: Rs. " + String.format("%.2f", totalAmount));
    }

    private void setClickListeners() {
        payNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processPayment();
            }
        });
    }

    private void processPayment() {
        if (selectedPaymentMethod.equals("stripe")) {
            processStripePayment();
        } else {
            processCODPayment();
        }
    }

    private void processStripePayment() {
        // Validate card details
        String cardNumber = cardNumberEt.getText().toString().trim();
        String expiryDate = expiryDateEt.getText().toString().trim();
        String cvv = cvvEt.getText().toString().trim();
        String cardholderName = cardholderNameEt.getText().toString().trim();

        if (cardNumber.isEmpty()) {
            cardNumberEt.setError("Card number is required");
            cardNumberEt.requestFocus();
            return;
        }

        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            cardNumberEt.setError("Invalid card number");
            cardNumberEt.requestFocus();
            return;
        }

        if (expiryDate.isEmpty()) {
            expiryDateEt.setError("Expiry date is required");
            expiryDateEt.requestFocus();
            return;
        }

        if (cvv.isEmpty()) {
            cvvEt.setError("CVV is required");
            cvvEt.requestFocus();
            return;
        }

        if (cvv.length() < 3) {
            cvvEt.setError("Invalid CVV");
            cvvEt.requestFocus();
            return;
        }

        if (cardholderName.isEmpty()) {
            cardholderNameEt.setError("Cardholder name is required");
            cardholderNameEt.requestFocus();
            return;
        }

        // TODO: Integrate with Stripe SDK
        // For now, simulate successful payment
        Toast.makeText(this, "Processing Stripe payment...", Toast.LENGTH_SHORT).show();
        
        // Simulate payment processing
        payNowBtn.setEnabled(false);
        payNowBtn.setText("Processing...");
        
        // In a real app, you would call Stripe API here
        // For now, we'll simulate success after a delay
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Simulate successful payment
                createOrder("stripe");
            }
        }, 2000);
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
                    // Clear cart
                    cartRepository.clearCart(userId);
                    
                    // Show success message
                    String message = paymentMethod.equals("stripe") 
                        ? "Payment successful! Order placed." 
                        : "Order placed successfully! Pay on delivery.";
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate back to home
                    Intent intent = new Intent(PaymentActivity.this, UserHomeActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error creating order items", Toast.LENGTH_SHORT).show();
                    payNowBtn.setEnabled(true);
                    payNowBtn.setText("Pay Now");
                }
            } else {
                Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
                payNowBtn.setEnabled(true);
                payNowBtn.setText("Pay Now");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show();
            payNowBtn.setEnabled(true);
            payNowBtn.setText("Pay Now");
        }
    }

    /**
     * Method to update Stripe keys
     * Call this method with your actual Stripe keys
     */
    public static void setStripeKeys(String publishableKey, String secretKey) {
        // In a real app, you would store these securely
        // For now, this is just a placeholder
        Log.d(TAG, "Stripe keys updated");
    }
}
