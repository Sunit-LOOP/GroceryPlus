package com.sunit.groceryplus;

import android.content.Context;
import android.util.Log;

public class CategoryRepository {
    private static final String TAG = "CategoryRepository";
    private DatabaseHelper dbHelper;

    public CategoryRepository(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    /**
     * Add a new category
     */
    public boolean addCategory(String categoryName, String categoryDescription) {
        try {
            long result = dbHelper.addCategory(categoryName, categoryDescription);
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error adding category", e);
            return false;
        }
    }

    /**
     * Get all categories
     */
    public String[] getAllCategories() {
        // This is a simplified implementation
        // In a real app, you would query the database and return a list of Category objects
        return new String[]{"Fruits", "Vegetables", "Dairy", "Meat", "Bakery"};
    }
}