package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.CartAdapter;
import com.sunit.groceryplus.models.CartItem;
// Fixed import - CartRepository is in the same package
import com.sunit.groceryplus.CartRepository;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    
    private RecyclerView cartRecyclerView;
    private TextView emptyCartTv;
    private TextView totalPriceTv;
    private Button checkoutBtn;
    
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        
        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize repository
        cartRepository = new CartRepository(this);
        
        // Initialize views
        initViews();
        
        // Setup RecyclerView
        setupRecyclerView();

        // Setup Bottom Navigation
        com.sunit.groceryplus.utils.NavigationHelper.setupNavigation(this, userId);
        
        // Set click listeners
        setClickListeners();
        
        // Load cart items
        loadCartItems();
        
        // Setup Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Cart");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    
    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        emptyCartTv = findViewById(R.id.emptyCartTv);
        totalPriceTv = findViewById(R.id.cartTotalPriceTv);
        checkoutBtn = findViewById(R.id.checkoutBtn);
    }
    
    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems, new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onQuantityChanged(int cartItemId, int newQuantity) {
                updateQuantity(cartItemId, newQuantity);
            }

            @Override
            public void onItemRemoved(int cartItemId) {
                removeItem(cartItemId);
            }
        });
        cartRecyclerView.setAdapter(cartAdapter);
    }
    
    private void loadCartItems() {
        try {
            cartItems = cartRepository.getCartItems(userId);
            
            if (cartItems != null && !cartItems.isEmpty()) {
                cartAdapter.updateCartItems(cartItems);
                updateTotalPrice();
                showCart();
            } else {
                showEmptyCart();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading cart items", e);
            Toast.makeText(this, "Error loading cart", Toast.LENGTH_SHORT).show();
            showEmptyCart();
        }
    }
    
    private void updateQuantity(int cartItemId, int newQuantity) {
        try {
            boolean success = cartRepository.updateCartQuantity(cartItemId, newQuantity);
            
            if (success) {
                // Update the item in our list
                for (CartItem item : cartItems) {
                    if (item.getCartId() == cartItemId) {
                        item.setQuantity(newQuantity);
                        break;
                    }
                }
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating quantity", e);
            Toast.makeText(this, "Error updating quantity", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void removeItem(int cartItemId) {
        try {
            boolean success = cartRepository.removeFromCart(cartItemId);
            
            if (success) {
                // Remove the item from our list
                CartItem itemToRemove = null;
                for (CartItem item : cartItems) {
                    if (item.getCartId() == cartItemId) {
                        itemToRemove = item;
                        break;
                    }
                }
                
                if (itemToRemove != null) {
                    cartItems.remove(itemToRemove);
                }
                
                cartAdapter.notifyDataSetChanged();
                updateTotalPrice();
                Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                
                if (cartItems.isEmpty()) {
                    showEmptyCart();
                }
            } else {
                Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing item", e);
            Toast.makeText(this, "Error removing item", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateTotalPrice() {
        double total = cartAdapter.getTotalPrice();
        totalPriceTv.setText("Total: Rs. " + String.format("%.2f", total));
    }
    
    private void showCart() {
        cartRecyclerView.setVisibility(View.VISIBLE);
        totalPriceTv.setVisibility(View.VISIBLE);
        checkoutBtn.setVisibility(View.VISIBLE);
        emptyCartTv.setVisibility(View.GONE);
    }
    
    private void showEmptyCart() {
        cartRecyclerView.setVisibility(View.GONE);
        totalPriceTv.setVisibility(View.GONE);
        checkoutBtn.setVisibility(View.GONE);
        emptyCartTv.setVisibility(View.VISIBLE);
    }
    
    private void setClickListeners() {
        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToCheckout();
            }
        });
    }
    
    private void proceedToCheckout() {
        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // Calculate total
            double total = cartAdapter.getTotalPrice();
            int itemCount = cartItems.size();
            
            // Navigate to payment activity
            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("total_amount", total);
            intent.putExtra("total_items", itemCount);
            startActivity(intent);
            
        } catch (Exception e) {
            Log.e(TAG, "Error during checkout", e);
            Toast.makeText(this, "Error processing checkout", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}