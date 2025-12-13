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
        productAdapter = new ProductAdapter(this, products, userId);
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
        // Use common NavigationHelper
        com.sunit.groceryplus.utils.NavigationHelper.setupNavigation(this, userId);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.user_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_notification) {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Banner Rotation
    private android.widget.ImageView bannerImage;
    private android.os.Handler bannerHandler = new android.os.Handler();
    private int[] bannerResources = {
        R.drawable.banner_1,
        R.drawable.banner_2, 
        R.drawable.banner_3, 
        R.drawable.banner_4, 
        R.drawable.banner_5
    };
    private int currentBannerIndex = 0;
    
    // Runnable to rotate banner
    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            if (bannerImage != null) {
                try {
                    // Update Image
                    currentBannerIndex = (currentBannerIndex + 1) % bannerResources.length;
                    
                    // Standard approach: R.drawable.banner_1
                    
                    bannerImage.setImageResource(bannerResources[currentBannerIndex]);
                } catch (Exception e) {
                   Log.e(TAG, "Error setting banner image", e); 
                }
            }
            // Schedule next update
            bannerHandler.postDelayed(this, 3000); // 3 seconds
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        // Reload products
        loadProducts();
        
        // Start Banner Rotation
        bannerImage = findViewById(R.id.bannerImage);
        if (bannerImage != null && bannerResources.length > 0) {
            bannerHandler.postDelayed(bannerRunnable, 3000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop Banner Rotation to prevent leaks
        bannerHandler.removeCallbacks(bannerRunnable);
    }
}