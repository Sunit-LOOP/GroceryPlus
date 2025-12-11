package com.sunit.groceryplus;

import android.provider.BaseColumns;

/**
 * Database contract class that defines the database schema
 */
public final class DatabaseContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseContract() {}

    /* Inner class that defines the users table contents */
    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "users";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_USER_NAME = "user_name";
        public static final String COLUMN_NAME_USER_EMAIL = "user_email";
        public static final String COLUMN_NAME_USER_PHONE = "user_phone";
        public static final String COLUMN_NAME_USER_PASSWORD = "user_password";
        public static final String COLUMN_NAME_USER_SALT = "user_salt";
        public static final String COLUMN_NAME_USER_TYPE = "user_type";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
    }

    /* Inner class that defines the categories table contents */
    public static class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME_CATEGORY_NAME = "category_name";
        public static final String COLUMN_NAME_CATEGORY_DESCRIPTION = "category_description";
    }

    /* Inner class that defines the products table contents */
    public static class ProductEntry implements BaseColumns {
        public static final String TABLE_NAME = "products";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_PRODUCT_NAME = "product_name";
        public static final String COLUMN_NAME_CATEGORY_ID = "category_id";
        public static final String COLUMN_NAME_PRICE = "price";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMAGE = "image";
    }

    /* Inner class that defines the orders table contents */
    public static class OrderEntry implements BaseColumns {
        public static final String TABLE_NAME = "orders";
        public static final String COLUMN_NAME_ORDER_ID = "order_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_TOTAL_AMOUNT = "total_amount";
        public static final String COLUMN_NAME_STATUS = "status";
        public static final String COLUMN_NAME_ORDER_DATE = "order_date";
    }

    /* Inner class that defines the order_items table contents */
    public static class OrderItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "order_items";
        public static final String COLUMN_NAME_ORDER_ITEM_ID = "order_item_id";
        public static final String COLUMN_NAME_ORDER_ID = "order_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
        public static final String COLUMN_NAME_PRICE = "price";
    }

    /* Inner class that defines the cart_items table contents */
    public static class CartItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "cart_items";
        public static final String COLUMN_NAME_CART_ID = "cart_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_QUANTITY = "quantity";
    }

    /* Inner class that defines the favorites table contents */
    public static class FavoriteEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_NAME_FAVORITE_ID = "favorite_id";
        public static final String COLUMN_NAME_USER_ID = "user_id";
        public static final String COLUMN_NAME_PRODUCT_ID = "product_id";
        public static final String COLUMN_NAME_ADDED_AT = "added_at";
    }

    /* Inner class that defines the messages table contents */
    public static class MessageEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages";
        public static final String COLUMN_NAME_MESSAGE_ID = "message_id";
        public static final String COLUMN_NAME_SENDER_ID = "sender_id";
        public static final String COLUMN_NAME_RECEIVER_ID = "receiver_id";
        public static final String COLUMN_NAME_MESSAGE_TEXT = "message_text";
        public static final String COLUMN_NAME_IS_READ = "is_read";
        public static final String COLUMN_NAME_CREATED_AT = "created_at";
    }

    // SQL statements to create tables
    public static final String SQL_CREATE_USERS_TABLE =
            "CREATE TABLE " + UserEntry.TABLE_NAME + " (" +
                    UserEntry.COLUMN_NAME_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    UserEntry.COLUMN_NAME_USER_NAME + " TEXT," +
                    UserEntry.COLUMN_NAME_USER_EMAIL + " TEXT UNIQUE," +
                    UserEntry.COLUMN_NAME_USER_PHONE + " TEXT," +
                    UserEntry.COLUMN_NAME_USER_PASSWORD + " TEXT," +
                    UserEntry.COLUMN_NAME_USER_SALT + " TEXT," +
                    UserEntry.COLUMN_NAME_USER_TYPE + " TEXT," +
                    UserEntry.COLUMN_NAME_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP)";

    public static final String SQL_CREATE_CATEGORIES_TABLE =
            "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                    CategoryEntry.COLUMN_NAME_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CategoryEntry.COLUMN_NAME_CATEGORY_NAME + " TEXT," +
                    CategoryEntry.COLUMN_NAME_CATEGORY_DESCRIPTION + " TEXT)";

    public static final String SQL_CREATE_PRODUCTS_TABLE =
            "CREATE TABLE " + ProductEntry.TABLE_NAME + " (" +
                    ProductEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ProductEntry.COLUMN_NAME_PRODUCT_NAME + " TEXT," +
                    ProductEntry.COLUMN_NAME_CATEGORY_ID + " INTEGER," +
                    ProductEntry.COLUMN_NAME_PRICE + " REAL," +
                    ProductEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    ProductEntry.COLUMN_NAME_IMAGE + " TEXT," +
                    "FOREIGN KEY(" + ProductEntry.COLUMN_NAME_CATEGORY_ID + ") REFERENCES " + CategoryEntry.TABLE_NAME + "(" + CategoryEntry.COLUMN_NAME_CATEGORY_ID + "))";

    public static final String SQL_CREATE_ORDERS_TABLE =
            "CREATE TABLE " + OrderEntry.TABLE_NAME + " (" +
                    OrderEntry.COLUMN_NAME_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    OrderEntry.COLUMN_NAME_USER_ID + " INTEGER," +
                    OrderEntry.COLUMN_NAME_TOTAL_AMOUNT + " REAL," +
                    OrderEntry.COLUMN_NAME_STATUS + " TEXT," +
                    OrderEntry.COLUMN_NAME_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + OrderEntry.COLUMN_NAME_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USER_ID + "))";

    public static final String SQL_CREATE_ORDER_ITEMS_TABLE =
            "CREATE TABLE " + OrderItemEntry.TABLE_NAME + " (" +
                    OrderItemEntry.COLUMN_NAME_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    OrderItemEntry.COLUMN_NAME_ORDER_ID + " INTEGER," +
                    OrderItemEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER," +
                    OrderItemEntry.COLUMN_NAME_QUANTITY + " INTEGER," +
                    OrderItemEntry.COLUMN_NAME_PRICE + " REAL," +
                    "FOREIGN KEY(" + OrderItemEntry.COLUMN_NAME_ORDER_ID + ") REFERENCES " + OrderEntry.TABLE_NAME + "(" + OrderEntry.COLUMN_NAME_ORDER_ID + ")," +
                    "FOREIGN KEY(" + OrderItemEntry.COLUMN_NAME_PRODUCT_ID + ") REFERENCES " + ProductEntry.TABLE_NAME + "(" + ProductEntry.COLUMN_NAME_PRODUCT_ID + "))";

    public static final String SQL_CREATE_CART_ITEMS_TABLE =
            "CREATE TABLE " + CartItemEntry.TABLE_NAME + " (" +
                    CartItemEntry.COLUMN_NAME_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CartItemEntry.COLUMN_NAME_USER_ID + " INTEGER," +
                    CartItemEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER," +
                    CartItemEntry.COLUMN_NAME_QUANTITY + " INTEGER," +
                    "FOREIGN KEY(" + CartItemEntry.COLUMN_NAME_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USER_ID + ")," +
                    "FOREIGN KEY(" + CartItemEntry.COLUMN_NAME_PRODUCT_ID + ") REFERENCES " + ProductEntry.TABLE_NAME + "(" + ProductEntry.COLUMN_NAME_PRODUCT_ID + "))";

    public static final String SQL_CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                    FavoriteEntry.COLUMN_NAME_FAVORITE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FavoriteEntry.COLUMN_NAME_USER_ID + " INTEGER," +
                    FavoriteEntry.COLUMN_NAME_PRODUCT_ID + " INTEGER," +
                    FavoriteEntry.COLUMN_NAME_ADDED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + FavoriteEntry.COLUMN_NAME_USER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USER_ID + ")," +
                    "FOREIGN KEY(" + FavoriteEntry.COLUMN_NAME_PRODUCT_ID + ") REFERENCES " + ProductEntry.TABLE_NAME + "(" + ProductEntry.COLUMN_NAME_PRODUCT_ID + "))";

    public static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + MessageEntry.TABLE_NAME + " (" +
                    MessageEntry.COLUMN_NAME_MESSAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    MessageEntry.COLUMN_NAME_SENDER_ID + " INTEGER," +
                    MessageEntry.COLUMN_NAME_RECEIVER_ID + " INTEGER," +
                    MessageEntry.COLUMN_NAME_MESSAGE_TEXT + " TEXT," +
                    MessageEntry.COLUMN_NAME_IS_READ + " INTEGER DEFAULT 0," +
                    MessageEntry.COLUMN_NAME_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(" + MessageEntry.COLUMN_NAME_SENDER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USER_ID + ")," +
                    "FOREIGN KEY(" + MessageEntry.COLUMN_NAME_RECEIVER_ID + ") REFERENCES " + UserEntry.TABLE_NAME + "(" + UserEntry.COLUMN_NAME_USER_ID + "))";

    // SQL statements to delete tables
    public static final String SQL_DELETE_USERS_TABLE =
            "DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME;
            
    public static final String SQL_DELETE_CATEGORIES_TABLE =
            "DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME;
            
    public static final String SQL_DELETE_PRODUCTS_TABLE =
            "DROP TABLE IF EXISTS " + ProductEntry.TABLE_NAME;
            
    public static final String SQL_DELETE_ORDERS_TABLE =
            "DROP TABLE IF EXISTS " + OrderEntry.TABLE_NAME;
            
    public static final String SQL_DELETE_ORDER_ITEMS_TABLE =
            "DROP TABLE IF EXISTS " + OrderItemEntry.TABLE_NAME;
            
    public static final String SQL_DELETE_CART_ITEMS_TABLE =
            "DROP TABLE IF EXISTS " + CartItemEntry.TABLE_NAME;

    public static final String SQL_DELETE_FAVORITES_TABLE =
            "DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME;

    public static final String SQL_DELETE_MESSAGES_TABLE =
            "DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME;
}