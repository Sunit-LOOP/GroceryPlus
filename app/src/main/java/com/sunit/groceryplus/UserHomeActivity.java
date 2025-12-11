package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.CategoryAdapter;
import com.sunit.groceryplus.adapters.ProductAdapter;
import com.sunit.groceryplus.models.Category;
import com.sunit.groceryplus.models.Product;

import java.util.ArrayList;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity {

    private static final String TAG = "UserHomeActivity";
    private RecyclerView categoriesRv;
    private RecyclerView productsRv;
    private LinearLayout navHome;
    private LinearLayout navMessage;
    private LinearLayout navHistory;
    private LinearLayout navCart;
    private LinearLayout navProfile;
    private int userId;
    
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    
    private List<Category> categories = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Log.e(TAG, "Invalid user ID received");
            Toast.makeText(this, "Error: Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repositories
        categoryRepository = new CategoryRepository(this);
        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);

        // Initialize views
        initViews();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load data
        loadCategories();
        loadProducts();

        // Set bottom navigation listener
        setBottomNavigationListener();
    }

    private void initViews() {
        categoriesRv = findViewById(R.id.categoriesRv);
        productsRv = findViewById(R.id.productsRv);
        navHome = findViewById(R.id.navHome);
        navMessage = findViewById(R.id.navMessage);
        navHistory = findViewById(R.id.navHistory);
        navCart = findViewById(R.id.navCart);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerViews() {
        // Setup categories RecyclerView (horizontal)
        categoriesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(this, categories, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(Category category) {
                onCategorySelected(category);
            }
        });
        categoriesRv.setAdapter(categoryAdapter);

        // Setup products RecyclerView (grid)
        productsRv.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, products, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Navigate to product detail
                Intent intent = new Intent(UserHomeActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getProductId());
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }
        });
        productsRv.setAdapter(productAdapter);
    }

    private void loadCategories() {
        try {
            categories = categoryRepository.getAllCategories();
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.updateCategories(categories);
                Log.d(TAG, "Loaded " + categories.size() + " categories");
            } else {
                Log.w(TAG, "No categories found");
                Toast.makeText(this, "No categories available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading categories", e);
            Toast.makeText(this, "Error loading categories", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProducts() {
        try {
            if (selectedCategoryId == -1) {
                // Load all products
                products = productRepository.getAllProducts();
            } else {
                // Load products by category
                products = productRepository.getProductsByCategory(selectedCategoryId);
            }
            
            if (products != null && !products.isEmpty()) {
                productAdapter.updateProducts(products);
                Log.d(TAG, "Loaded " + products.size() + " products");
            } else {
                Log.w(TAG, "No products found");
                Toast.makeText(this, "No products available", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading products", e);
            Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
        }
    }

    private void onCategorySelected(Category category) {
        selectedCategoryId = category.getCategoryId();
        loadProducts();
        Toast.makeText(this, "Showing " + category.getCategoryName(), Toast.LENGTH_SHORT).show();
    }

    private void addToCart(Product product) {
        try {
            boolean success = cartRepository.addToCart(userId, product.getProductId(), 1);
            if (success) {
                Toast.makeText(this, product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private void setBottomNavigationListener() {
        // Set click listeners for custom bottom navigation
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on home screen - reload data
                selectedCategoryId = -1;
                loadProducts();
            }
        });

        navMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to message activity
                Intent intent = new Intent(UserHomeActivity.this, MessageActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        navHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to history activity
                Intent intent = new Intent(UserHomeActivity.this, OrderHistoryActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        navCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to cart activity
                Intent intent = new Intent(UserHomeActivity.this, CartActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });

        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to user detail view activity
                Intent intent = new Intent(UserHomeActivity.this, UserDetailViewActivity.class);
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload products when returning to this activity
        loadProducts();
    }
}