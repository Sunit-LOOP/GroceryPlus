package com.sunit.groceryplus.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.sunit.groceryplus.OrderRepository;
import com.sunit.groceryplus.R;
import com.sunit.groceryplus.adapters.AdminOrderAdapter;
import com.sunit.groceryplus.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderManagementActivity extends AppCompatActivity {

    private RecyclerView ordersRv;
    private AdminOrderAdapter adapter;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_management);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ordersRv = findViewById(R.id.ordersRv);
        orderRepository = new OrderRepository(this);

        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        ordersRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminOrderAdapter(this, new ArrayList<>(), new AdminOrderAdapter.OnOrderActionListener() {
            @Override
            public void onUpdateStatusClick(Order order) {
                showUpdateStatusDialog(order);
            }
        });
        ordersRv.setAdapter(adapter);
    }

    private void loadOrders() {
        List<Order> orders = orderRepository.getAllOrders();
        adapter.updateOrders(orders);
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statuses = {"Pending", "Processing", "Delivered", "Cancelled"};
        
        new AlertDialog.Builder(this)
            .setTitle("Update Order Status")
            .setSingleChoiceItems(statuses, -1, (dialog, which) -> {
                String newStatus = statuses[which];
                // Update in DB
                boolean success = orderRepository.updateOrderStatus(order.getOrderId(), newStatus);
                if (success) {
                    Toast.makeText(this, "Order updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    loadOrders();
                } else {
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
