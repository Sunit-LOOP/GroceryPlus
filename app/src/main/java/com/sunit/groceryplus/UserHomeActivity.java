package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.CategoryAdapter;
import com.sunit.groceryplus.adapters.ProductAdapter;
import com.sunit.groceryplus.models.Category;
import com.sunit.groceryplus.models.Product;

import com.sunit.groceryplus.utils.RecommendationEngine;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserHomeActivity extends AppCompatActivity {

    private static final String TAG = "UserHomeActivity";
    
    private RecyclerView categoriesRv, featuredRecyclerView, allProductsRecyclerView, buyAgainRecyclerView, recommendedRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter featuredAdapter, allProductsAdapter, buyAgainAdapter, recommendedAdapter;
    
    private TextView deliveryTimeTv, freeDeliveryTv;
    private com.google.android.material.progressindicator.LinearProgressIndicator freeDeliveryProgress;
    private View freeDeliveryGoalCard, buyAgainSection, recommendationSection;
    private ImageView sortIcon;
    
    private int userId;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private OrderRepository orderRepository;
    private RecommendationEngine recommendationEngine;
    
    private List<Category> categories = new ArrayList<>();
    private List<Product> featuredProducts = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> buyAgainProducts = new ArrayList<>();
    private List<Product> recommendedProducts = new ArrayList<>();
    
    private int selectedCategoryId = -1;
    private String currentSortOrder = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "UserHomeActivity onCreate called");
        setContentView(R.layout.activity_user_home);

        userId = getIntent().getIntExtra("user_id", -1);
        Log.d(TAG, "Received user_id from intent: " + userId);
        if (userId == -1) {
            android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            userId = sharedPreferences.getInt("userId", -1);
            Log.d(TAG, "Retrieved user_id from SharedPreferences: " + userId);
            
            // Also get other user details
            String userName = sharedPreferences.getString("userName", "");
            String userEmail = sharedPreferences.getString("userEmail", "");
            String userType = sharedPreferences.getString("userType", "");
            Log.d(TAG, "Retrieved user details - Name: " + userName + ", Email: " + userEmail + ", Type: " + userType);
        }

        if (userId == -1) {
            Log.e(TAG, "Invalid user ID - session expired. Redirecting to login.");
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            // Redirect to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }
        Log.d(TAG, "Valid user ID: " + userId);

        categoryRepository = new CategoryRepository(this);
        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);
        orderRepository = new OrderRepository(this);
        recommendationEngine = new RecommendationEngine(this);

        initViews();
        setupRecyclerViews();
        setupToolbar();
        setupSortFunctionality();

        loadData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent called - UserHomeActivity brought to front via navigation");
        
        // Update userId if provided in new intent
        int newUserId = intent.getIntExtra("user_id", -1);
        if (newUserId != -1) {
            userId = newUserId;
            Log.d(TAG, "Updated userId from new intent: " + userId);
        }
        
        // Refresh data
        loadData();
    }

    private void initViews() {
        deliveryTimeTv = findViewById(R.id.deliveryTimeTv);
        freeDeliveryTv = findViewById(R.id.freeDeliveryTv);
        freeDeliveryProgress = findViewById(R.id.freeDeliveryProgress);
        freeDeliveryGoalCard = findViewById(R.id.freeDeliveryGoalCard);
        buyAgainSection = findViewById(R.id.buyAgainSection);
        sortIcon = findViewById(R.id.sortIcon);

        categoriesRv = findViewById(R.id.categoriesRv);
        featuredRecyclerView = findViewById(R.id.featuredRecyclerView);
        allProductsRecyclerView = findViewById(R.id.allProductsRecyclerView);
        buyAgainRecyclerView = findViewById(R.id.buyAgainRecyclerView);
        recommendedRecyclerView = findViewById(R.id.recommendedRecyclerView);
        recommendationSection = findViewById(R.id.recommendationSection);

        findViewById(R.id.btnSearchField).setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        findViewById(R.id.exploreVendorsCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, VendorMapActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        com.sunit.groceryplus.utils.NavigationHelper.setupNavigation(this, userId);
    }

    private void setupRecyclerViews() {
        categoriesRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        allProductsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        buyAgainRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendedRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        categoryAdapter = new CategoryAdapter(this, categories, category -> {
            selectedCategoryId = category.getCategoryId();
            loadAllProducts();
            Toast.makeText(this, "Showing " + category.getCategoryName(), Toast.LENGTH_SHORT).show();
        });
        categoriesRv.setAdapter(categoryAdapter);

        featuredAdapter = new ProductAdapter(this, featuredProducts, userId);
        allProductsAdapter = new ProductAdapter(this, allProducts, userId);
        buyAgainAdapter = new ProductAdapter(this, buyAgainProducts, userId);
        recommendedAdapter = new ProductAdapter(this, recommendedProducts, userId);

        ProductAdapter.OnCartUpdateListener cartListener = this::updateFreeDeliveryGoal;
        featuredAdapter.setCartUpdateListener(cartListener);
        allProductsAdapter.setCartUpdateListener(cartListener);
        buyAgainAdapter.setCartUpdateListener(cartListener);
        recommendedAdapter.setCartUpdateListener(cartListener);

        featuredRecyclerView.setAdapter(featuredAdapter);
        allProductsRecyclerView.setAdapter(allProductsAdapter);
        buyAgainRecyclerView.setAdapter(buyAgainAdapter);
        recommendedRecyclerView.setAdapter(recommendedAdapter);
    }

    private void setupToolbar() {
        com.google.android.material.appbar.MaterialToolbar toolbar = findViewById(R.id.homeToolbar);
        if (toolbar != null) setSupportActionBar(toolbar);
    }

    private void setupSortFunctionality() {
        if (sortIcon != null) {
            sortIcon.setOnClickListener(v -> showSortOptions());
        }
    }

    private void loadData() {
        loadCategories();
        loadFeaturedProducts();
        loadAllProducts();
        loadBuyAgainProducts();
        loadRecommendedProducts();
        updateFreeDeliveryGoal();
        updateDeliveryTime();
    }

    private void loadCategories() {
        categories = categoryRepository.getAllCategories();
        if (categories != null) categoryAdapter.updateCategories(categories);
    }

    private void loadFeaturedProducts() {
        List<Product> all = productRepository.getAllProducts();
        if (all != null && !all.isEmpty()) {
            featuredProducts.clear();
            for (int i = 0; i < Math.min(6, all.size()); i++) {
                featuredProducts.add(all.get(i));
            }
            featuredAdapter.updateProducts(featuredProducts);
        }
    }

    private void loadAllProducts() {
        if (selectedCategoryId == -1) {
            allProducts = productRepository.getAllProducts();
        } else {
            allProducts = productRepository.getProductsByCategory(selectedCategoryId);
        }
        applySortAndNotify();
    }

    private void loadBuyAgainProducts() {
        com.sunit.groceryplus.models.Order lastOrder = orderRepository.getLastOrder(userId);
        if (lastOrder != null) {
            List<com.sunit.groceryplus.models.OrderItem> items = orderRepository.getOrderItems(lastOrder.getOrderId());
            buyAgainProducts.clear();
            for (com.sunit.groceryplus.models.OrderItem item : items) {
                Product p = productRepository.getProductById(item.getProductId());
                if (p != null) buyAgainProducts.add(p);
            }
            if (!buyAgainProducts.isEmpty()) {
                buyAgainSection.setVisibility(View.VISIBLE);
                buyAgainAdapter.updateProducts(buyAgainProducts);
            } else {
                buyAgainSection.setVisibility(View.GONE);
            }
        }
    }

    private void loadRecommendedProducts() {
        recommendedProducts = recommendationEngine.getRecommendations(userId, 10);
        if (recommendedProducts != null && !recommendedProducts.isEmpty()) {
            recommendationSection.setVisibility(View.VISIBLE);
            recommendedAdapter.updateProducts(recommendedProducts);
        } else {
            recommendationSection.setVisibility(View.GONE);
        }
    }

    private void updateFreeDeliveryGoal() {
        double total = cartRepository.getCartTotal(userId);
        double threshold = 500.0;
        if (total <= 0) {
            freeDeliveryGoalCard.setVisibility(View.GONE);
        } else if (total >= threshold) {
            freeDeliveryGoalCard.setVisibility(View.VISIBLE);
            freeDeliveryTv.setText("Yay! You get FREE delivery!");
            freeDeliveryProgress.setProgress(100);
        } else {
            freeDeliveryGoalCard.setVisibility(View.VISIBLE);
            double gap = threshold - total;
            freeDeliveryTv.setText("Add Rs. " + String.format("%.0f", gap) + " more for FREE delivery");
            int progress = (int) ((total / threshold) * 100);
            freeDeliveryProgress.setProgress(progress);
        }
    }

    private void updateDeliveryTime() {
        if (deliveryTimeTv != null) {
            deliveryTimeTv.setText("20 - 30 mins");
        }
    }

    private void applySortAndNotify() {
        List<Product> sortedList = new ArrayList<>(allProducts);
        if ("price_low_high".equals(currentSortOrder)) {
            sortedList = com.sunit.groceryplus.utils.SearchSortAlgorithms.quickSortByPrice(allProducts);
        } else if ("price_high_low".equals(currentSortOrder)) {
            sortedList = com.sunit.groceryplus.utils.SearchSortAlgorithms.quickSortByPrice(allProducts);
            Collections.reverse(sortedList);
        } else if ("rating".equals(currentSortOrder)) {
            sortedList = com.sunit.groceryplus.utils.SearchSortAlgorithms.sortByRatingThenPrice(allProducts);
        }
        allProductsAdapter.updateProducts(sortedList);
    }

    private void showSortOptions() {
        String[] options = {"Price: Low to High", "Price: High to Low", "Highest Rated"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sort By")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: currentSortOrder = "price_low_high"; break;
                        case 1: currentSortOrder = "price_high_low"; break;
                        case 2: currentSortOrder = "rating"; break;
                    }
                    applySortAndNotify();
                }).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}