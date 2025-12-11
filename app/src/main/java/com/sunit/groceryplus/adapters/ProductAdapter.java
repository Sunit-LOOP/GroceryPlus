package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Product;

import java.util.List;

/**
 * Adapter for displaying products in a RecyclerView
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onAddToCartClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products, OnProductClickListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products != null ? products.size() : 0;
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageIv;
        TextView productNameTv;
        TextView productPriceTv;
        TextView productDescriptionTv;
        TextView addToCartBtn;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageIv = itemView.findViewById(R.id.productImageIv);
            productNameTv = itemView.findViewById(R.id.productNameTv);
            productPriceTv = itemView.findViewById(R.id.productPriceTv);
            productDescriptionTv = itemView.findViewById(R.id.productDescriptionTv);
            addToCartBtn = itemView.findViewById(R.id.addToCartBtn);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onProductClick(products.get(position));
                }
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
