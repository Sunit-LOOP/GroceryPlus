package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.sunit.groceryplus.models.CartItem;
import com.sunit.groceryplus.models.Order;
import com.sunit.groceryplus.network.PaymentIntentRequest;
import com.sunit.groceryplus.network.PaymentIntentResponse;
import com.sunit.groceryplus.network.StripeApi;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    
    // REPLACE with your actual publishable key
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_REPLACE_WITH_YOUR_PUBLISHABLE_KEY";
    
    // Backend URL (Use 10.0.2.2 for Android Emulator to access localhost)
    private static final String BACKEND_URL = "http://10.0.2.2:4567/";

    // UI Elements
    private RadioGroup paymentMethodGroup;
    private MaterialCardView stripeCard, codCard;
    
    private TextView totalAmountTv, subtotalAmountTv;
    private Button payNowBtn;
    
    // Data
    private int userId;
    private double totalAmount;
    private String selectedPaymentMethod = "stripe"; // default
    
    private CartRepository cartRepository;
    private OrderRepository orderRepository;

    // Stripe
    private PaymentSheet paymentSheet;
    private String paymentIntentClientSecret;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        userId = getIntent().getIntExtra("user_id", -1);
        totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);
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

        // Initialize Stripe
        PaymentConfiguration.init(getApplicationContext(), STRIPE_PUBLISHABLE_KEY);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        // Set click listeners
        payNowBtn.setOnClickListener(v -> processPayment());
    }

    private void initViews() {
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        stripeCard = findViewById(R.id.stripeCard);
        codCard = findViewById(R.id.codCard);
        
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
        paymentMethodGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.creditCardRadio) {
                selectedPaymentMethod = "stripe";
                highlightCard(stripeCard, true);
                highlightCard(codCard, false);
            } else if (checkedId == R.id.cashOnDeliveryRadio) {
                selectedPaymentMethod = "cod";
                highlightCard(stripeCard, false);
                highlightCard(codCard, true);
            }
        });
        
        // Initial state
        if (selectedPaymentMethod.equals("stripe")) {
             highlightCard(stripeCard, true);
             highlightCard(codCard, false);
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
        double subtotal = totalAmount > deliveryFee ? totalAmount - deliveryFee : totalAmount;
        
        // If the passed totalAmount doesn't account for delivery logic, we just display it.
        // Assuming totalAmount is the final payable amount.
        
        subtotalAmountTv.setText("₹" + String.format("%.2f", totalAmount)); // Simplified for this demo
        totalAmountTv.setText("₹" + String.format("%.2f", totalAmount));
        payNowBtn.setText("Pay ₹" + String.format("%.2f", totalAmount));
    }

    private void processPayment() {
        if (selectedPaymentMethod.equals("stripe")) {
            fetchPaymentIntentAndPay();
        } else {
            processCODPayment();
        }
    }

    private void fetchPaymentIntentAndPay() {
        payNowBtn.setEnabled(false);
        payNowBtn.setText("Processing...");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StripeApi stripeApi = retrofit.create(StripeApi.class);
        
        // Convert amount to smallest currency unit (e.g. cents/paisa)
        int amountInSmallestUnit = (int) (totalAmount * 100);
        
        PaymentIntentRequest request = new PaymentIntentRequest(amountInSmallestUnit, "npr");

        stripeApi.createPaymentIntent(request).enqueue(new Callback<PaymentIntentResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentIntentResponse> call, @NonNull Response<PaymentIntentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    paymentIntentClientSecret = response.body().getClientSecret();
                    presentPaymentSheet();
                } else {
                    payNowBtn.setEnabled(true);
                    payNowBtn.setText("Pay Now");
                    Toast.makeText(PaymentActivity.this, "Failed to initialize payment", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Backend Response Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentIntentResponse> call, @NonNull Throwable t) {
                payNowBtn.setEnabled(true);
                payNowBtn.setText("Pay Now");
                Toast.makeText(PaymentActivity.this, "Network error: Make sure backend is running", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network Error", t);
            }
        });
    }

    private void presentPaymentSheet() {
        payNowBtn.setText("Pay Now"); // Reset
        payNowBtn.setEnabled(true);
        
        if (paymentIntentClientSecret != null) {
             PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("GroceryPlus")
                    .merchantDisplayName("GroceryPlus Inc.")
                    .build();
            paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration);
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            createOrder("stripe");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Throwable error = ((PaymentSheetResult.Failed) paymentSheetResult).getError();
            Toast.makeText(this, "Payment Failed: " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
                // Add order items (Simulated transaction logic)
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
                    
                    String message = paymentMethod.equals("stripe") 
                        ? "Payment successful! Order placed." 
                        : "Order placed successfully! Pay on delivery.";
                    
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    
                    // Navigate to Order Success Screen
                    Intent intent = new Intent(PaymentActivity.this, OrderSuccessActivity.class);
                    intent.putExtra("user_id", userId);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error creating order items", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            Toast.makeText(this, "Error processing payment", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
