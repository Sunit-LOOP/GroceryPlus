package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sunit.groceryplus.ProductDetailActivity;
import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Product;
import com.sunit.groceryplus.DatabaseHelper;

import java.util.List;
import java.io.File;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private DatabaseHelper dbHelper;
    private int userId;

    public ProductAdapter(Context context, List<Product> productList, int userId) {
        this.context = context;
        this.productList = productList;
        this.dbHelper = new DatabaseHelper(context);
        this.userId = userId;
    }

    public void setFilteredList(List<Product> filteredList) {
        this.productList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Updated to use现代 modern card layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_product_card_modern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productName.setText(product.getProductName());
        holder.productPrice.setText(String.format("$%.2f", product.getPrice()));
        
        // Category Name (if available, otherwise placeholder)
        // Ideally pass category name or fetch it, for now hardcoded or skipped if not in model
         holder.productCategory.setText("Groceries"); // Placeholder or fetch logic

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            Glide.with(context)
                    .load(new File(product.getImage()))
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(holder.productImage);
        } else {
             holder.productImage.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getProductId());
             intent.putExtra("user_id", userId);
            context.startActivity(intent);
        });

        // Add to Cart Click
        holder.addToCartBtn.setOnClickListener(v -> {
             dbHelper.addToCart(userId, product.getProductId(), 1);
             Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
        });
        
        // Favorite Logic
        boolean isFav = dbHelper.isFavorite(userId, product.getProductId());
        holder.favoriteBtn.setChecked(isFav);
        
        holder.favoriteBtn.setOnClickListener(v -> {
            if (holder.favoriteBtn.isChecked()) {
                dbHelper.addFavorite(userId, product.getProductId());
                Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.removeFavorite(userId, product.getProductId());
                Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
            }
        });

        // Animation
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productCategory;
        View addToCartBtn; // Changed to View to match MaterialButton generic usage or specific type
        ToggleButton favoriteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productCategory = itemView.findViewById(R.id.productCategory);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);
            });

            if (addToCartBtn != null) {
                addToCartBtn.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onAddToCartClick(products.get(position));
                    }
                });
            }
        }

        public void bind(Product product) {
            productNameTv.setText(product.getProductName());
            productPriceTv.setText("Rs. " + String.format("%.2f", product.getPrice()));
            
            if (productDescriptionTv != null) {
                productDescriptionTv.setText(product.getDescription());
            }

            // Set product image based on image name
            // For now, using a default icon
            if (productImageIv != null) {
                int imageResource = getImageResource(product.getImage());
                if (imageResource != 0) {
                    productImageIv.setImageResource(imageResource);
                } else {
                    productImageIv.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }
        }

        private int getImageResource(String imageName) {
            if (imageName == null || imageName.isEmpty()) {
                return 0;
            }
            
            try {
                return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            } catch (Exception e) {
                return 0;
            }
        }
    }
}
