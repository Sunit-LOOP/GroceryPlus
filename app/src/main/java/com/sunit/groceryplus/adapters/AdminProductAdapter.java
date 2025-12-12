package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Product;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public AdminProductAdapter(Context context, List<Product> products, OnProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_product, parent, false);
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
        Button editProductBtn;
        Button deleteProductBtn;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageIv = itemView.findViewById(R.id.productImageIv);
            productNameTv = itemView.findViewById(R.id.productNameTv);
            productPriceTv = itemView.findViewById(R.id.productPriceTv);
            productDescriptionTv = itemView.findViewById(R.id.productDescriptionTv);
            editProductBtn = itemView.findViewById(R.id.editProductBtn);
            deleteProductBtn = itemView.findViewById(R.id.deleteProductBtn);

            editProductBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(products.get(position));
                }
            });

            deleteProductBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteClick(products.get(position));
                }
            });
        }

        public void bind(Product product) {
            productNameTv.setText(product.getProductName());
            productPriceTv.setText("Rs. " + String.format("%.2f", product.getPrice()));
            
            if (productDescriptionTv != null) {
                productDescriptionTv.setText(product.getDescription());
            }

            // Set product image based on image name
            // For now, using a default icon or checking resource
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
