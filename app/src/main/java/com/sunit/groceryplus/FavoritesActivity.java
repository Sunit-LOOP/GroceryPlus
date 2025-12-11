package com.sunit.groceryplus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.adapters.ProductAdapter;
import com.sunit.groceryplus.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";
    
    private RecyclerView favoritesRv;
    private TextView emptyFavoritesTv;
    
    private int userId;
    private FavoriteRepository favoriteRepository;
    private CartRepository cartRepository;
    private ProductAdapter productAdapter;
    
    private List<Product> favoriteProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Get user ID from intent
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Error: Invalid user session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repositories
        favoriteRepository = new FavoriteRepository(this);
        cartRepository = new CartRepository(this);

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load favorites
        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when returning to this activity
        loadFavorites();
    }

    private void initViews() {
        favoritesRv = findViewById(R.id.favoritesRv);
        emptyFavoritesTv = findViewById(R.id.emptyFavoritesTv);
    }

    private void setupRecyclerView() {
        favoritesRv.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, favoriteProducts, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onProductClick(Product product) {
                // Navigate to product detail
                Intent intent = new Intent(FavoritesActivity.this, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getProductId());
                intent.putExtra("user_id", userId);
                startActivity(intent);
            }

            @Override
            public void onAddToCartClick(Product product) {
                addToCart(product);
            }
        });
        favoritesRv.setAdapter(productAdapter);
    }

    private void loadFavorites() {
        try {
            favoriteProducts.clear();
            List<Product> products = favoriteRepository.getFavoriteProducts(userId);
            
            if (products != null && !products.isEmpty()) {
                favoriteProducts.addAll(products);
                productAdapter.updateProducts(favoriteProducts);
                showFavorites();
                Log.d(TAG, "Loaded " + products.size() + " favorite products");
            } else {
                productAdapter.updateProducts(favoriteProducts);
                showEmptyState();
                Log.d(TAG, "No favorite products found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading favorites", e);
            Toast.makeText(this, "Error loading favorites", Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
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

    private void showFavorites() {
        favoritesRv.setVisibility(View.VISIBLE);
        emptyFavoritesTv.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        favoritesRv.setVisibility(View.GONE);
        emptyFavoritesTv.setVisibility(View.VISIBLE);
    }
}
