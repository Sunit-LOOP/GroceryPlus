package com.sunit.groceryplus;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.OrderAdapter;
import com.sunit.groceryplus.models.Order;
import com.sunit.groceryplus.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private static final String TAG = "OrderHistoryActivity";
    
    private RecyclerView ordersRecyclerView;
    private TextView emptyOrdersTv;
    
    private int userId;
    private OrderRepository orderRepository;
    private OrderAdapter orderAdapter;
    private List<Order> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repository
        orderRepository = new OrderRepository(this);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load orders
        loadOrders();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        emptyOrdersTv = findViewById(R.id.emptyOrdersTv);
    }

    private void setupRecyclerView() {
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderAdapter = new OrderAdapter(this, orders, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                showOrderDetails(order);
            }
        });
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        try {
            orders = orderRepository.getUserOrders(userId);
            
            if (orders != null && !orders.isEmpty()) {
                // Load order items for each order
                for (Order order : orders) {
                    List<OrderItem> items = orderRepository.getOrderItems(order.getOrderId());
                    order.setItems(items);
                }
                
                orderAdapter.updateOrders(orders);
                showOrders();
                Log.d(TAG, "Loaded " + orders.size() + " orders");
            } else {
                showEmptyOrders();
                Log.d(TAG, "No orders found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading orders", e);
            Toast.makeText(this, "Error loading orders", Toast.LENGTH_SHORT).show();
            showEmptyOrders();
        }
    }

    private void showOrders() {
        ordersRecyclerView.setVisibility(View.VISIBLE);
        emptyOrdersTv.setVisibility(View.GONE);
    }

    private void showEmptyOrders() {
        ordersRecyclerView.setVisibility(View.GONE);
        emptyOrdersTv.setVisibility(View.VISIBLE);
    }

    private void showOrderDetails(Order order) {
        // Build order details message
        StringBuilder details = new StringBuilder();
        details.append("Order #").append(order.getOrderId()).append("\n");
        details.append("Date: ").append(order.getOrderDate()).append("\n");
        details.append("Status: ").append(order.getStatus().toUpperCase()).append("\n");
        details.append("Total: Rs. ").append(String.format("%.2f", order.getTotalAmount())).append("\n\n");
        details.append("Items:\n");
        
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                details.append("- ").append(item.getProductName())
                       .append(" x").append(item.getQuantity())
                       .append(" @ Rs.").append(String.format("%.2f", item.getPrice()))
                       .append("\n");
            }
        }
        
        // Show in a dialog or toast
        Toast.makeText(this, details.toString(), Toast.LENGTH_LONG).show();
        Log.d(TAG, "Order details: " + details.toString());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}