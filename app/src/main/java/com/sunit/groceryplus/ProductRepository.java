package com.sunit.groceryplus;

import android.content.Context;
import android.util.Log;

public class ProductRepository {
    private static final String TAG = "ProductRepository";
    private DatabaseHelper dbHelper;

    public ProductRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Add a new product
     */
    public boolean addProduct(String productName, int categoryId, double price, String description, String image) {
        try {
            long result = dbHelper.addProduct(productName, categoryId, price, description, image);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding product", e);
            return false;
        }
    }

    /**
     * Get all products
     */
    public String[] getAllProducts() {
        // This is a simplified implementation
        // In a real app, you would query the database and return a list of Product objects
        return new String[]{"Apple", "Banana", "Milk", "Bread", "Chicken"};
    }

    /**
     * Get products by category
     */
    public String[] getProductsByCategory(int categoryId) {
        // This is a simplified implementation
        // In a real app, you would query the database and return a list of Product objects
        return new String[]{"Product 1", "Product 2", "Product 3"};
    }
}