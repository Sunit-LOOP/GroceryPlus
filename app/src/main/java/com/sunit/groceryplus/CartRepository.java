package com.sunit.groceryplus;

import android.content.Context;
import android.util.Log;

public class CartRepository {
    private static final String TAG = "CartRepository";
    private DatabaseHelper dbHelper;

    public CartRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Add item to cart
     */
    public boolean addToCart(int userId, int productId, int quantity) {
        try {
            long result = dbHelper.addToCart(userId, productId, quantity);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            return false;
        }
    }

    /**
     * Get cart items for user
     */
    public String[] getCartItems(int userId) {
        // This is a simplified implementation
        // In a real app, you would query the database and return a list of CartItem objects
        return new String[]{"Item 1", "Item 2", "Item 3"};
    }

    /**
     * Remove item from cart
     */
    public boolean removeFromCart(int cartId) {
        // This is a simplified implementation
        // In a real app, you would delete the item from the database
        return true;
    }

    /**
     * Clear cart for user
     */
    public boolean clearCart(int userId) {
        // This is a simplified implementation
        // In a real app, you would delete all items from the cart for the user
        return true;
    }
}