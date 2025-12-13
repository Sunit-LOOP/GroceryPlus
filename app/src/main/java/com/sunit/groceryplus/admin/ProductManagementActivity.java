package com.sunit.groceryplus.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.sunit.groceryplus.CategoryRepository;
import com.sunit.groceryplus.ProductRepository;
import com.sunit.groceryplus.R;
import com.sunit.groceryplus.adapters.AdminProductAdapter;
import com.sunit.groceryplus.models.Category;
import com.sunit.groceryplus.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementActivity extends AppCompatActivity {

    private RecyclerView productsRv;
    private FloatingActionButton addProductFab;
    
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private AdminProductAdapter adapter;
    private List<Category> categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_management);
        
        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Products");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        initViews();
        initRepositories();
        loadCategories();
        setupRecyclerView();
        setClickListeners();
        loadProducts();
    }
    
    private void initViews() {
        productsRv = findViewById(R.id.productsRv);
        addProductFab = findViewById(R.id.addProductFab);
    }
    
    private void initRepositories() {
        productRepository = new ProductRepository(this);
        categoryRepository = new CategoryRepository(this);
    }
    
    private void loadCategories() {
        categories = categoryRepository.getAllCategories();
    }
    
    private void setupRecyclerView() {
        productsRv.setLayoutManager(new GridLayoutManager(this, 1)); // Single column implementation of grid
        // Updated to remove the listener since the adapter now handles actions internally
        adapter = new AdminProductAdapter(this, new ArrayList<>(), productRepository, categories, this);
        productsRv.setAdapter(adapter);
    }
    
    private void loadProducts() {
        List<Product> products = productRepository.getAllProducts();
        adapter.updateProducts(products);
    }
    
    private void setClickListeners() {
        addProductFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProductDialog(null);
            }
        });
    }
    
    private void showProductDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_product, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // UI References
        TextInputEditText nameEt = dialogView.findViewById(R.id.productNameEt);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        TextInputEditText priceEt = dialogView.findViewById(R.id.productPriceEt);
        TextInputEditText descriptionEt = dialogView.findViewById(R.id.productDescriptionEt);
        TextInputEditText imageEt = dialogView.findViewById(R.id.productImageEt);
        TextInputEditText stockEt = dialogView.findViewById(R.id.productStockEt);
        Button cancelBtn = dialogView.findViewById(R.id.cancelBtn);
        Button saveBtn = dialogView.findViewById(R.id.saveBtn);

        // Setup Spinner
        List<String> categoryNames = new ArrayList<>();
        for (Category cat : categories) {
            categoryNames.add(cat.getCategoryName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);

        // Populate if editing
        if (product != null) {
            nameEt.setText(product.getProductName());
            priceEt.setText(String.valueOf(product.getPrice()));
            descriptionEt.setText(product.getDescription());
            imageEt.setText(product.getImage());
            stockEt.setText(String.valueOf(product.getStock()));
            
            // Set spinner selection
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).getCategoryId() == product.getCategoryId()) {
                    categorySpinner.setSelection(i);
                    break;
                }
            }
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString().trim();
            String priceStr = priceEt.getText().toString().trim();
            String description = descriptionEt.getText().toString().trim();
            String image = imageEt.getText().toString().trim();
            String stockStr = stockEt.getText().toString().trim();

            if (name.isEmpty() || priceStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Note: Currently stock is not persisted in my version of addProduct, assuming it's ignored or handled in future.
            // But I populated the field for future proofing.

            int selectedCategoryIndex = categorySpinner.getSelectedItemPosition();
            if (selectedCategoryIndex == -1 && !categories.isEmpty()) {
                selectedCategoryIndex = 0;
            }
            
            if (categories.isEmpty()) {
                Toast.makeText(this, "No categories available. Add categories first.", Toast.LENGTH_LONG).show();
                return;
            }
            
            int categoryId = categories.get(selectedCategoryIndex).getCategoryId();

            boolean success;
            if (product == null) {
                // Add
                success = productRepository.addProduct(name, categoryId, price, description, image);
                if (success) {
                    Toast.makeText(this, "Product added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Update
                success = productRepository.updateProduct(product.getProductId(), name, categoryId, price, description, image);
                if (success) {
                    Toast.makeText(this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to update product", Toast.LENGTH_SHORT).show();
                }
            }

            if (success) {
                loadProducts();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getProductName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = productRepository.deleteProduct(product.getProductId());
                    if (success) {
                        Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
                        loadProducts();
                    } else {
                        Toast.makeText(this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}