package com.sunit.groceryplus;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sunit.groceryplus.models.Product;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    
    private ImageView productImageIv;
    private TextView productNameTv;
    private TextView productPriceTv;
    private TextView productDescriptionTv;
    private TextView productCategoryTv;
    private TextView quantityTv;
    private ImageButton decreaseBtn;
    private ImageButton increaseBtn;
    private Button addToCartBtn;
    
    private int productId;
    private int userId;
    private int quantity = 1;
    
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get data from intent
        productId = getIntent().getIntExtra("product_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);

        if (productId == -1 || userId == -1) {
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize repositories
        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);

        // Initialize views
        initViews();

        // Load product details
        loadProductDetails();

        // Set click listeners
        setClickListeners();
    }

    private void initViews() {
        productImageIv = findViewById(R.id.productDetailImageIv);
        productNameTv = findViewById(R.id.productDetailNameTv);
        productPriceTv = findViewById(R.id.productDetailPriceTv);
        productDescriptionTv = findViewById(R.id.productDetailDescriptionTv);
        productCategoryTv = findViewById(R.id.productDetailCategoryTv);
        quantityTv = findViewById(R.id.productDetailQuantityTv);
        decreaseBtn = findViewById(R.id.productDetailDecreaseBtn);
        increaseBtn = findViewById(R.id.productDetailIncreaseBtn);
        addToCartBtn = findViewById(R.id.productDetailAddToCartBtn);
    }

    private void loadProductDetails() {
        try {
            product = productRepository.getProductById(productId);
            
            if (product != null) {
                productNameTv.setText(product.getProductName());
                productPriceTv.setText("Rs. " + String.format("%.2f", product.getPrice()));
                productDescriptionTv.setText(product.getDescription());
                productCategoryTv.setText(product.getCategoryName());
                quantityTv.setText(String.valueOf(quantity));

                // Set product image
                int imageResource = getImageResource(product.getImage());
                if (imageResource != 0) {
                    productImageIv.setImageResource(imageResource);
                } else {
                    productImageIv.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading product details", e);
            Toast.makeText(this, "Error loading product", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setClickListeners() {
        decreaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 1) {
                    quantity--;
                    quantityTv.setText(String.valueOf(quantity));
                }
            }
        });

        increaseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quantity++;
                quantityTv.setText(String.valueOf(quantity));
            }
        });

        addToCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    private void addToCart() {
        try {
            boolean success = cartRepository.addToCart(userId, productId, quantity);
            
            if (success) {
                Toast.makeText(this, quantity + " x " + product.getProductName() + " added to cart", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            Toast.makeText(this, "Error adding to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private int getImageResource(String imageName) {
        if (imageName == null || imageName.isEmpty()) {
            return R.drawable.product_icon; // Better default
        }
        
        // Try to find a specific image based on product name if no image name is provided
        if (imageName.equals("ic_launcher_foreground")) {
            // This is a fallback, try to assign specific images
            return R.drawable.product_icon;
        }
        
        try {
            return getResources().getIdentifier(imageName, "drawable", getPackageName());
        } catch (Exception e) {
            return R.drawable.product_icon; // Better fallback
        }
    }
}
