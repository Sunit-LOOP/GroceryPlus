package com.sunit.groceryplus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.sunit.groceryplus.DatabaseContract.CartItemEntry;
import static com.sunit.groceryplus.DatabaseContract.CategoryEntry;
import static com.sunit.groceryplus.DatabaseContract.OrderEntry;
import static com.sunit.groceryplus.DatabaseContract.OrderItemEntry;
import static com.sunit.groceryplus.DatabaseContract.ProductEntry;
import static com.sunit.groceryplus.DatabaseContract.UserEntry;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "GroceryPlus.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create all tables using DatabaseContract constants
        db.execSQL(DatabaseContract.SQL_CREATE_USERS_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_PRODUCTS_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_ORDERS_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_ORDER_ITEMS_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_CART_ITEMS_TABLE);

        // Insert default admin user
        insertDefaultAdmin(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables
        db.execSQL(DatabaseContract.SQL_DELETE_USERS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_CATEGORIES_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_PRODUCTS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_ORDERS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_ORDER_ITEMS_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_CART_ITEMS_TABLE);

        onCreate(db);
    }

    /** Insert default admin user */
    private void insertDefaultAdmin(SQLiteDatabase db) {
        try {
            String adminPassword = "admin123";
            String salt = generateSalt();
            String hashedPassword = hashPassword(adminPassword, salt);

            ContentValues values = new ContentValues();
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_NAME, "Admin User");
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL, "admin@gmail.com");
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_PHONE, "9815689963");
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_PASSWORD, hashedPassword);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_SALT, salt);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_TYPE, "admin");

            db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, values);
            Log.d(TAG, "Default admin created");
        } catch (Exception e) {
            Log.e(TAG, "Error creating default admin", e);
        }
    }

    /** Generate random salt */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    /** Hash password with SHA-256 + salt */
    private String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes());
        byte[] hashed = md.digest(password.getBytes());
        return bytesToHex(hashed);
    }

    /** Convert byte array to hex string */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Add a new user to the database
     */
    public long addUser(String name, String email, String phone, String password, String userType) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            String salt = generateSalt();
            String hashedPassword = hashPassword(password, salt);
            
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_NAME, name);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL, email);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_PHONE, phone);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_PASSWORD, hashedPassword);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_SALT, salt);
            values.put(DatabaseContract.UserEntry.COLUMN_NAME_USER_TYPE, userType);
            
            // Inserting Row
            long userId = db.insert(DatabaseContract.UserEntry.TABLE_NAME, null, values);
            return userId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding user", e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Check user credentials for login
     */
    public User authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + DatabaseContract.UserEntry.TABLE_NAME + " WHERE " + DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email});
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int saltIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_SALT);
                int passwordIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_PASSWORD);
                int userIdIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_ID);
                int userNameIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_NAME);
                int userEmailIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL);
                int userPhoneIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_PHONE);
                int userTypeIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_TYPE);
                
                if (saltIndex >= 0 && passwordIndex >= 0) {
                    String storedSalt = cursor.getString(saltIndex);
                    String storedPassword = cursor.getString(passwordIndex);
                    String hashedInputPassword = hashPassword(password, storedSalt);
                    
                    if (hashedInputPassword.equals(storedPassword)) {
                        // Password matches
                        int userId = (userIdIndex >= 0) ? cursor.getInt(userIdIndex) : -1;
                        String userName = (userNameIndex >= 0) ? cursor.getString(userNameIndex) : "";
                        String userEmail = (userEmailIndex >= 0) ? cursor.getString(userEmailIndex) : "";
                        String userPhone = (userPhoneIndex >= 0) ? cursor.getString(userPhoneIndex) : "";
                        String userType = (userTypeIndex >= 0) ? cursor.getString(userTypeIndex) : "";
                        
                        User user = new User(userId, userName, userEmail, userPhone, userType);
                        return user;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error authenticating user", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (cursor != null) {
            cursor.close();
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + DatabaseContract.UserEntry.TABLE_NAME + " WHERE " + DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email});
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int userIdIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_ID);
                int userNameIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_NAME);
                int userEmailIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL);
                int userPhoneIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_PHONE);
                int userTypeIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_TYPE);
                
                int userId = (userIdIndex >= 0) ? cursor.getInt(userIdIndex) : -1;
                String userName = (userNameIndex >= 0) ? cursor.getString(userNameIndex) : "";
                String userEmail = (userEmailIndex >= 0) ? cursor.getString(userEmailIndex) : "";
                String userPhone = (userPhoneIndex >= 0) ? cursor.getString(userPhoneIndex) : "";
                String userType = (userTypeIndex >= 0) ? cursor.getString(userTypeIndex) : "";
                
                User user = new User(userId, userName, userEmail, userPhone, userType);
                return user;
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by email", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (cursor != null) {
            cursor.close();
        }
        
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + DatabaseContract.UserEntry.TABLE_NAME + " WHERE " + DatabaseContract.UserEntry.COLUMN_NAME_USER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int userIdIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_ID);
                int userNameIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_NAME);
                int userEmailIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL);
                int userPhoneIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_PHONE);
                int userTypeIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_USER_TYPE);
                
                int id = (userIdIndex >= 0) ? cursor.getInt(userIdIndex) : -1;
                String userName = (userNameIndex >= 0) ? cursor.getString(userNameIndex) : "";
                String userEmail = (userEmailIndex >= 0) ? cursor.getString(userEmailIndex) : "";
                String userPhone = (userPhoneIndex >= 0) ? cursor.getString(userPhoneIndex) : "";
                String userType = (userTypeIndex >= 0) ? cursor.getString(userTypeIndex) : "";
                
                User user = new User(id, userName, userEmail, userPhone, userType);
                return user;
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by ID", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (cursor != null) {
            cursor.close();
        }
        
        return null;
    }
    
    /**
     * Check if user exists
     */
    public boolean isUserExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + DatabaseContract.UserEntry.TABLE_NAME + " WHERE " + DatabaseContract.UserEntry.COLUMN_NAME_USER_EMAIL + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{email});
        
        boolean exists = (cursor != null && cursor.getCount() > 0);
        
        if (cursor != null) {
            cursor.close();
        }
        
        return exists;
    }
    
    /**
     * Add a new category
     */
    public long addCategory(String categoryName, String categoryDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.CategoryEntry.COLUMN_NAME_CATEGORY_NAME, categoryName);
        values.put(DatabaseContract.CategoryEntry.COLUMN_NAME_CATEGORY_DESCRIPTION, categoryDescription);
        
        long categoryId = db.insert(DatabaseContract.CategoryEntry.TABLE_NAME, null, values);
        db.close();
        return categoryId;
    }
    
    /**
     * Add a new product
     */
    public long addProduct(String productName, int categoryId, double price, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME_PRODUCT_NAME, productName);
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME_CATEGORY_ID, categoryId);
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME_PRICE, price);
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME_DESCRIPTION, description);
        values.put(DatabaseContract.ProductEntry.COLUMN_NAME_IMAGE, image);
        
        long productId = db.insert(DatabaseContract.ProductEntry.TABLE_NAME, null, values);
        db.close();
        return productId;
    }
    
    /**
     * Add item to cart
     */
    public long addToCart(int userId, int productId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(CartItemEntry.COLUMN_NAME_USER_ID, userId);
            values.put(CartItemEntry.COLUMN_NAME_PRODUCT_ID, productId);
            values.put(CartItemEntry.COLUMN_NAME_QUANTITY, quantity);
            
            // Inserting Row
            long cartId = db.insert(CartItemEntry.TABLE_NAME, null, values);
            return cartId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get cart items for user
     */
    public Cursor getCartItems(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selectQuery = "SELECT * FROM " + CartItemEntry.TABLE_NAME + " WHERE " + CartItemEntry.COLUMN_NAME_USER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(userId)});
        return cursor;
    }
    
    /**
     * Remove item from cart
     */
    public int removeFromCart(int cartId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int result = db.delete(CartItemEntry.TABLE_NAME, CartItemEntry.COLUMN_NAME_CART_ID + " = ?", 
                              new String[]{String.valueOf(cartId)});
        db.close();
        return result;
    }
    
    /**
     * Clear cart for user
     */
    public int clearCart(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        int result = db.delete(CartItemEntry.TABLE_NAME, CartItemEntry.COLUMN_NAME_USER_ID + " = ?", 
                              new String[]{String.valueOf(userId)});
        db.close();
        return result;
    }
    
    /**
     * Create a new order
     */
    public long createOrder(int userId, double totalAmount, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(OrderEntry.COLUMN_NAME_USER_ID, userId);
            values.put(OrderEntry.COLUMN_NAME_TOTAL_AMOUNT, totalAmount);
            values.put(OrderEntry.COLUMN_NAME_STATUS, status);
            
            // Inserting Row
            long orderId = db.insert(OrderEntry.TABLE_NAME, null, values);
            return orderId;
        } catch (Exception e) {
            Log.e(TAG, "Error creating order", e);
            return -1;
        } finally {
            db.close();
        }
    }
    
    /**
     * Add order item
     */
    public long addOrderItem(int orderId, int productId, int quantity, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(OrderItemEntry.COLUMN_NAME_ORDER_ID, orderId);
            values.put(OrderItemEntry.COLUMN_NAME_PRODUCT_ID, productId);
            values.put(OrderItemEntry.COLUMN_NAME_QUANTITY, quantity);
            values.put(OrderItemEntry.COLUMN_NAME_PRICE, price);
            
            // Inserting Row
            long orderItemId = db.insert(OrderItemEntry.TABLE_NAME, null, values);
            return orderItemId;
        } catch (Exception e) {
            Log.e(TAG, "Error adding order item", e);
            return -1;
        } finally {
            db.close();
        }
    }
}