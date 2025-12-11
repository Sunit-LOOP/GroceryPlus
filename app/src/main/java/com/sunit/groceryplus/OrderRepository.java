package com.sunit.groceryplus;

import android.content.Context;
import android.util.Log;

import com.sunit.groceryplus.models.Order;
import com.sunit.groceryplus.models.OrderItem;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private static final String TAG = "OrderRepository";
    private DatabaseHelper dbHelper;

    public OrderRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Create a new order
     */
    public long createOrder(int userId, double totalAmount, String status) {
        try {
            return dbHelper.createOrder(userId, totalAmount, status);
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            return -1;
        }
    }

    /**
     * Add order item
     */
    public boolean addOrderItem(int orderId, int productId, int quantity, double price) {
        try {
            long result = dbHelper.addOrderItem(orderId, productId, quantity, price);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding order item", e);
            return false;
        }
    }

    /**
     * Get all orders (for admin)
     */
    public List<Order> getAllOrders() {
        try {
            return dbHelper.getAllOrders();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all orders", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get orders for user
     */
    public List<Order> getUserOrders(int userId) {
        try {
            return dbHelper.getOrdersByUser(userId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting user orders", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get order items
     */
    public List<OrderItem> getOrderItems(int orderId) {
        try {
            return dbHelper.getOrderItems(orderId);
        } catch (Exception e) {
            Log.e(TAG, "Error getting order items", e);
            return new ArrayList<>();
        }
    }

    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String status) {
        try {
            return dbHelper.updateOrderStatus(orderId, status);
        } catch (Exception e) {
            Log.e(TAG, "Error updating order status", e);
            return false;
        }
    }
}