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
        db.execSQL(DatabaseContract.SQL_CREATE_FAVORITES_TABLE);
        db.execSQL(DatabaseContract.SQL_CREATE_MESSAGES_TABLE);

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
        db.execSQL(DatabaseContract.SQL_DELETE_FAVORITES_TABLE);
        db.execSQL(DatabaseContract.SQL_DELETE_MESSAGES_TABLE);

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
     * Update user profile
     */
    public boolean updateUser(int userId, String name, String email, String phone, String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(UserEntry.COLUMN_NAME_USER_NAME, name);
            values.put(UserEntry.COLUMN_NAME_USER_EMAIL, email);
            values.put(UserEntry.COLUMN_NAME_USER_PHONE, phone);
            // Address field removed - User table doesn't have address column
            
            int result = db.update(UserEntry.TABLE_NAME, values, 
                                  UserEntry.COLUMN_NAME_USER_ID + " = ?", 
                                  new String[]{String.valueOf(userId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return false;
        } finally {
            db.close();
        }
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

    // ==================== PRODUCT METHODS ====================
    
    /**
     * Get all products with category information
     */
    public java.util.List<com.sunit.groceryplus.models.Product> getAllProducts() {
        java.util.List<com.sunit.groceryplus.models.Product> products = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT p.*, c." + CategoryEntry.COLUMN_NAME_CATEGORY_NAME + 
                      " FROM " + ProductEntry.TABLE_NAME + " p " +
                      " LEFT JOIN " + CategoryEntry.TABLE_NAME + " c " +
                      " ON p." + ProductEntry.COLUMN_NAME_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_NAME_CATEGORY_ID;
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_ID));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_CATEGORY_ID));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRICE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_DESCRIPTION));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                    
                    com.sunit.groceryplus.models.Product product = new com.sunit.groceryplus.models.Product(
                        productId, productName, categoryId, categoryName, price, description, image, 100
                    );
                    products.add(product);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing product", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return products;
    }
    
    /**
     * Get product by ID
     */
    public com.sunit.groceryplus.models.Product getProductById(int productId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT p.*, c." + CategoryEntry.COLUMN_NAME_CATEGORY_NAME + 
                      " FROM " + ProductEntry.TABLE_NAME + " p " +
                      " LEFT JOIN " + CategoryEntry.TABLE_NAME + " c " +
                      " ON p." + ProductEntry.COLUMN_NAME_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_NAME_CATEGORY_ID +
                      " WHERE p." + ProductEntry.COLUMN_NAME_PRODUCT_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(productId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_ID));
                String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_CATEGORY_ID));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRICE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_DESCRIPTION));
                String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                
                cursor.close();
                return new com.sunit.groceryplus.models.Product(
                    id, productName, categoryId, categoryName, price, description, image, 100
                );
            } catch (Exception e) {
                Log.e(TAG, "Error getting product by ID", e);
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        
        return null;
    }
    
    /**
     * Get products by category
     */
    public java.util.List<com.sunit.groceryplus.models.Product> getProductsByCategory(int categoryId) {
        java.util.List<com.sunit.groceryplus.models.Product> products = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT p.*, c." + CategoryEntry.COLUMN_NAME_CATEGORY_NAME + 
                      " FROM " + ProductEntry.TABLE_NAME + " p " +
                      " LEFT JOIN " + CategoryEntry.TABLE_NAME + " c " +
                      " ON p." + ProductEntry.COLUMN_NAME_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_NAME_CATEGORY_ID +
                      " WHERE p." + ProductEntry.COLUMN_NAME_CATEGORY_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_ID));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                    int catId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_CATEGORY_ID));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRICE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_DESCRIPTION));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                    
                    com.sunit.groceryplus.models.Product product = new com.sunit.groceryplus.models.Product(
                        productId, productName, catId, categoryName, price, description, image, 100
                    );
                    products.add(product);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing product", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return products;
    }
    
    /**
     * Search products by name
     */
    public java.util.List<com.sunit.groceryplus.models.Product> searchProducts(String query) {
        java.util.List<com.sunit.groceryplus.models.Product> products = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String searchQuery = "SELECT p.*, c." + CategoryEntry.COLUMN_NAME_CATEGORY_NAME + 
                            " FROM " + ProductEntry.TABLE_NAME + " p " +
                            " LEFT JOIN " + CategoryEntry.TABLE_NAME + " c " +
                            " ON p." + ProductEntry.COLUMN_NAME_CATEGORY_ID + " = c." + CategoryEntry.COLUMN_NAME_CATEGORY_ID +
                            " WHERE p." + ProductEntry.COLUMN_NAME_PRODUCT_NAME + " LIKE ?";
        
        Cursor cursor = db.rawQuery(searchQuery, new String[]{"%" + query + "%"});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_ID));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_CATEGORY_ID));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRICE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_DESCRIPTION));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                    
                    com.sunit.groceryplus.models.Product product = new com.sunit.groceryplus.models.Product(
                        productId, productName, categoryId, categoryName, price, description, image, 100
                    );
                    products.add(product);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing product", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return products;
    }
    
    /**
     * Update product
     */
    public boolean updateProduct(int productId, String productName, int categoryId, double price, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_NAME_PRODUCT_NAME, productName);
            values.put(ProductEntry.COLUMN_NAME_CATEGORY_ID, categoryId);
            values.put(ProductEntry.COLUMN_NAME_PRICE, price);
            values.put(ProductEntry.COLUMN_NAME_DESCRIPTION, description);
            values.put(ProductEntry.COLUMN_NAME_IMAGE, image);
            
            int result = db.update(ProductEntry.TABLE_NAME, values, 
                                  ProductEntry.COLUMN_NAME_PRODUCT_ID + " = ?", 
                                  new String[]{String.valueOf(productId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating product", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Delete product
     */
    public boolean deleteProduct(int productId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            int result = db.delete(ProductEntry.TABLE_NAME, 
                                  ProductEntry.COLUMN_NAME_PRODUCT_ID + " = ?", 
                                  new String[]{String.valueOf(productId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting product", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    // ==================== CATEGORY METHODS ====================
    
    /**
     * Get all categories
     */
    public java.util.List<com.sunit.groceryplus.models.Category> getAllCategories() {
        java.util.List<com.sunit.groceryplus.models.Category> categories = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + CategoryEntry.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_ID));
                    String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                    String categoryDescription = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_DESCRIPTION));
                    
                    com.sunit.groceryplus.models.Category category = new com.sunit.groceryplus.models.Category(
                        categoryId, categoryName, categoryDescription
                    );
                    categories.add(category);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing category", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return categories;
    }
    
    /**
     * Get category by ID
     */
    public com.sunit.groceryplus.models.Category getCategoryById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + CategoryEntry.TABLE_NAME + 
                      " WHERE " + CategoryEntry.COLUMN_NAME_CATEGORY_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            try {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_ID));
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_NAME));
                String categoryDescription = cursor.getString(cursor.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME_CATEGORY_DESCRIPTION));
                
                cursor.close();
                return new com.sunit.groceryplus.models.Category(id, categoryName, categoryDescription);
            } catch (Exception e) {
                Log.e(TAG, "Error getting category by ID", e);
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        
        return null;
    }
    
    /**
     * Update category
     */
    public boolean updateCategory(int categoryId, String categoryName, String categoryDescription) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(CategoryEntry.COLUMN_NAME_CATEGORY_NAME, categoryName);
            values.put(CategoryEntry.COLUMN_NAME_CATEGORY_DESCRIPTION, categoryDescription);
            
            int result = db.update(CategoryEntry.TABLE_NAME, values, 
                                  CategoryEntry.COLUMN_NAME_CATEGORY_ID + " = ?", 
                                  new String[]{String.valueOf(categoryId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating category", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Delete category
     */
    public boolean deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            int result = db.delete(CategoryEntry.TABLE_NAME, 
                                  CategoryEntry.COLUMN_NAME_CATEGORY_ID + " = ?", 
                                  new String[]{String.valueOf(categoryId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleting category", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    // ==================== CART METHODS ====================
    
    /**
     * Get cart items with product details
     */
    public java.util.List<com.sunit.groceryplus.models.CartItem> getCartItemsWithDetails(int userId) {
        java.util.List<com.sunit.groceryplus.models.CartItem> cartItems = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT c.*, p." + ProductEntry.COLUMN_NAME_PRODUCT_NAME + ", p." + ProductEntry.COLUMN_NAME_PRICE + ", p." + ProductEntry.COLUMN_NAME_IMAGE +
                      " FROM " + CartItemEntry.TABLE_NAME + " c " +
                      " JOIN " + ProductEntry.TABLE_NAME + " p " +
                      " ON c." + CartItemEntry.COLUMN_NAME_PRODUCT_ID + " = p." + ProductEntry.COLUMN_NAME_PRODUCT_ID +
                      " WHERE c." + CartItemEntry.COLUMN_NAME_USER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int cartId = cursor.getInt(cursor.getColumnIndexOrThrow(CartItemEntry.COLUMN_NAME_CART_ID));
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow(CartItemEntry.COLUMN_NAME_PRODUCT_ID));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRICE));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(CartItemEntry.COLUMN_NAME_QUANTITY));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                    
                    com.sunit.groceryplus.models.CartItem cartItem = new com.sunit.groceryplus.models.CartItem(
                        cartId, userId, productId, productName, price, quantity, image
                    );
                    cartItems.add(cartItem);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing cart item", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return cartItems;
    }
    
    /**
     * Update cart item quantity
     */
    public boolean updateCartQuantity(int cartId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(CartItemEntry.COLUMN_NAME_QUANTITY, quantity);
            
            int result = db.update(CartItemEntry.TABLE_NAME, values, 
                                  CartItemEntry.COLUMN_NAME_CART_ID + " = ?", 
                                  new String[]{String.valueOf(cartId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating cart quantity", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    /**
     * Get cart total
     */
    public double getCartTotal(int userId) {
        double total = 0.0;
        java.util.List<com.sunit.groceryplus.models.CartItem> items = getCartItemsWithDetails(userId);
        
        for (com.sunit.groceryplus.models.CartItem item : items) {
            total += item.getSubtotal();
        }
        
        return total;
    }
    
    // ==================== ORDER METHODS ====================
    
    /**
     * Get all orders (for admin)
     */
    public java.util.List<com.sunit.groceryplus.models.Order> getAllOrders() {
        java.util.List<com.sunit.groceryplus.models.Order> orders = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT o.*, u." + UserEntry.COLUMN_NAME_USER_NAME + ", u." + UserEntry.COLUMN_NAME_USER_EMAIL + ", u." + UserEntry.COLUMN_NAME_USER_PHONE +
                      " FROM " + OrderEntry.TABLE_NAME + " o " +
                      " JOIN " + UserEntry.TABLE_NAME + " u " +
                      " ON o." + OrderEntry.COLUMN_NAME_USER_ID + " = u." + UserEntry.COLUMN_NAME_USER_ID +
                      " ORDER BY o." + OrderEntry.COLUMN_NAME_ORDER_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int orderId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ORDER_ID));
                    int userId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_USER_ID));
                    String userName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_NAME));
                    String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_EMAIL));
                    String userPhone = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_PHONE));
                    double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_TOTAL_AMOUNT));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_STATUS));
                    String orderDate = cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ORDER_DATE));
                    
                    com.sunit.groceryplus.models.Order order = new com.sunit.groceryplus.models.Order(
                        orderId, userId, userName, userEmail, userPhone, totalAmount, status, orderDate
                    );
                    orders.add(order);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing order", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return orders;
    }
    
    /**
     * Get orders by user
     */
    public java.util.List<com.sunit.groceryplus.models.Order> getOrdersByUser(int userId) {
        java.util.List<com.sunit.groceryplus.models.Order> orders = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT o.*, u." + UserEntry.COLUMN_NAME_USER_NAME + ", u." + UserEntry.COLUMN_NAME_USER_EMAIL + ", u." + UserEntry.COLUMN_NAME_USER_PHONE +
                      " FROM " + OrderEntry.TABLE_NAME + " o " +
                      " JOIN " + UserEntry.TABLE_NAME + " u " +
                      " ON o." + OrderEntry.COLUMN_NAME_USER_ID + " = u." + UserEntry.COLUMN_NAME_USER_ID +
                      " WHERE o." + OrderEntry.COLUMN_NAME_USER_ID + " = ?" +
                      " ORDER BY o." + OrderEntry.COLUMN_NAME_ORDER_DATE + " DESC";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int orderId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ORDER_ID));
                    String userName = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_NAME));
                    String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_EMAIL));
                    String userPhone = cursor.getString(cursor.getColumnIndexOrThrow(UserEntry.COLUMN_NAME_USER_PHONE));
                    double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_TOTAL_AMOUNT));
                    String status = cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_STATUS));
                    String orderDate = cursor.getString(cursor.getColumnIndexOrThrow(OrderEntry.COLUMN_NAME_ORDER_DATE));
                    
                    com.sunit.groceryplus.models.Order order = new com.sunit.groceryplus.models.Order(
                        orderId, userId, userName, userEmail, userPhone, totalAmount, status, orderDate
                    );
                    orders.add(order);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing order", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return orders;
    }
    
    /**
     * Get order items for an order
     */
    public java.util.List<com.sunit.groceryplus.models.OrderItem> getOrderItems(int orderId) {
        java.util.List<com.sunit.groceryplus.models.OrderItem> orderItems = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT oi.*, p." + ProductEntry.COLUMN_NAME_PRODUCT_NAME + ", p." + ProductEntry.COLUMN_NAME_IMAGE +
                      " FROM " + OrderItemEntry.TABLE_NAME + " oi " +
                      " JOIN " + ProductEntry.TABLE_NAME + " p " +
                      " ON oi." + OrderItemEntry.COLUMN_NAME_PRODUCT_ID + " = p." + ProductEntry.COLUMN_NAME_PRODUCT_ID +
                      " WHERE oi." + OrderItemEntry.COLUMN_NAME_ORDER_ID + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    int orderItemId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_NAME_ORDER_ITEM_ID));
                    int productId = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_NAME_PRODUCT_ID));
                    String productName = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_PRODUCT_NAME));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_NAME_QUANTITY));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow(OrderItemEntry.COLUMN_NAME_PRICE));
                    String image = cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_NAME_IMAGE));
                    
                    com.sunit.groceryplus.models.OrderItem orderItem = new com.sunit.groceryplus.models.OrderItem(
                        orderItemId, orderId, productId, productName, quantity, price, image
                    );
                    orderItems.add(orderItem);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing order item", e);
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return orderItems;
    }
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            ContentValues values = new ContentValues();
            values.put(OrderEntry.COLUMN_NAME_STATUS, status);
            
            int result = db.update(OrderEntry.TABLE_NAME, values, 
                                  OrderEntry.COLUMN_NAME_ORDER_ID + " = ?", 
                                  new String[]{String.valueOf(orderId)});
            return result > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating order status", e);
            return false;
        } finally {
            db.close();
        }
    }
    
    // ==================== SAMPLE DATA METHODS ====================
    
    /**
     * Insert sample categories and products
     */
    public void insertSampleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            // Check if data already exists
            Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + CategoryEntry.TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                int count = cursor.getInt(0);
                cursor.close();
                if (count > 0) {
                    Log.d(TAG, "Sample data already exists");
                    return;
                }
            }
            
            // Insert categories
            long fruitsId = addCategory("Fruits", "Fresh fruits");
            long vegetablesId = addCategory("Vegetables", "Fresh vegetables");
            long dairyId = addCategory("Dairy", "Milk and dairy products");
            long bakeryId = addCategory("Bakery", "Bread and baked goods");
            long meatId = addCategory("Meat", "Fresh meat and poultry");
            
            // Insert products
            addProduct("Apple", (int)fruitsId, 120.0, "Fresh red apples", "apple", "1kg");
            addProduct("Banana", (int)fruitsId, 60.0, "Fresh bananas", "banana", "1 dozen");
            addProduct("Orange", (int)fruitsId, 100.0, "Fresh oranges", "orange", "1kg");
            addProduct("Mango", (int)fruitsId, 150.0, "Sweet mangoes", "mango", "1kg");
            
            addProduct("Tomato", (int)vegetablesId, 40.0, "Fresh tomatoes", "tomato", "1kg");
            addProduct("Potato", (int)vegetablesId, 30.0, "Fresh potatoes", "potato", "1kg");
            addProduct("Onion", (int)vegetablesId, 35.0, "Fresh onions", "onion", "1kg");
            addProduct("Carrot", (int)vegetablesId, 50.0, "Fresh carrots", "carrot", "1kg");
            
            addProduct("Milk", (int)dairyId, 70.0, "Fresh milk", "milk", "1 liter");
            addProduct("Cheese", (int)dairyId, 200.0, "Cheddar cheese", "cheese", "250g");
            addProduct("Yogurt", (int)dairyId, 80.0, "Plain yogurt", "yogurt", "500g");
            
            addProduct("Bread", (int)bakeryId, 45.0, "White bread", "bread", "1 loaf");
            addProduct("Croissant", (int)bakeryId, 100.0, "Butter croissant", "croissant", "6 pieces");
            
            addProduct("Chicken", (int)meatId, 350.0, "Fresh chicken", "chicken", "1kg");
            addProduct("Mutton", (int)meatId, 800.0, "Fresh mutton", "mutton", "1kg");
            
            Log.d(TAG, "Sample data inserted successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data", e);
        }
    }
    
    /**
     * Add product with stock
     */
    public long addProduct(String productName, int categoryId, double price, String description, String image, String unit) {
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
}
