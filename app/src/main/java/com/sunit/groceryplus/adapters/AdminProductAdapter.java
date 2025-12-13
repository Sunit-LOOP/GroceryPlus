package com.sunit.groceryplus.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.admin.ProductManagementActivity;
import com.sunit.groceryplus.models.Category;
import com.sunit.groceryplus.models.Product;
import com.sunit.groceryplus.ProductRepository;

import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private ProductRepository productRepository;
    private List<Category> categoryList;
    private ProductManagementActivity activity;

    public AdminProductAdapter(Context context, List<Product> productList, ProductRepository productRepository, List<Category> categoryList, ProductManagementActivity activity) {
        this.context = context;
        this.productList = productList;
        this.productRepository = productRepository;
        this.categoryList = categoryList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.productNameTv.setText(product.getProductName());
        holder.productPriceTv.setText("Rs. " + String.format("%.2f", product.getPrice()));
        if (holder.productDescriptionTv != null) {
            holder.productDescriptionTv.setText(product.getDescription());
        }

        // Set product image based on image name with better fallbacks
        String imageName = product.getImage();
        if (imageName != null && !imageName.isEmpty()) {
            int resourceId = context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
            if (resourceId != 0) {
                holder.productImageIv.setImageResource(resourceId);
            } else {
                // Try to assign specific images based on product name
                int specificImage = getSpecificImageForProduct(product.getProductName());
                holder.productImageIv.setImageResource(specificImage);
            }
        } else {
            // Assign specific images based on product name
            int specificImage = getSpecificImageForProduct(product.getProductName());
            holder.productImageIv.setImageResource(specificImage);
        }
    }

    /**
     * Get specific image resource based on product name
     */
    private int getSpecificImageForProduct(String productName) {
        if (productName == null) {
            return R.drawable.product_icon;
        }
        
        String lowerName = productName.toLowerCase();
        
        // Match specific products to images
        if (lowerName.contains("milk") || lowerName.contains("dairy")) {
            return R.drawable.bottle_milk;
        } else if (lowerName.contains("cheese")) {
            return R.drawable.cheese_slice;
        } else if (lowerName.contains("curd") || lowerName.contains("yogurt") || lowerName.contains("dahi")) {
            return R.drawable.curd;
        } else if (lowerName.contains("tomato")) {
            return R.drawable.tomato_red;
        } else if (lowerName.contains("cabbage")) {
            return R.drawable.cabbage;
        } else if (lowerName.contains("cauliflower")) {
            return R.drawable.cauliflower;
        } else if (lowerName.contains("lettuce") || lowerName.contains("leaf")) {
            return R.drawable.lettuce_leaf;
        } else if (lowerName.contains("paneer")) {
            return R.drawable.paneer_cubes;
        } else if (lowerName.contains("bottle")) {
            return R.drawable.bottle_gourd;
        } else if (lowerName.contains("green") && (lowerName.contains("vegetable") || lowerName.contains("leaf"))) {
            return R.drawable.green_vegetable;
        } else {
            // Default product image
            return R.drawable.product_icon;
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProducts(List<Product> products) {
        this.productList = products;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView productNameTv, productPriceTv, productDescriptionTv;
        ImageView productImageIv;
        Button editBtn, deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Corrected the view IDs to match the actual layout
            productNameTv = itemView.findViewById(R.id.productNameTv);
            productPriceTv = itemView.findViewById(R.id.productPriceTv);
            productDescriptionTv = itemView.findViewById(R.id.productDescriptionTv);
            productImageIv = itemView.findViewById(R.id.productImageIv);
            editBtn = itemView.findViewById(R.id.editProductBtn);
            deleteBtn = itemView.findViewById(R.id.deleteProductBtn);

            editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showEditDialog(position);
                    }
                }
            });

            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        showDeleteConfirmation(position);
                    }
                }
            });
        }

        private void showEditDialog(int position) {
            Product product = productList.get(position);
            
            // Inflate the dialog layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_product, null);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(dialogView);
            
            TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
            EditText productNameEt = dialogView.findViewById(R.id.productNameEt);
            Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
            EditText productPriceEt = dialogView.findViewById(R.id.productPriceEt);
            EditText productStockEt = dialogView.findViewById(R.id.productStockEt);
            EditText productDescriptionEt = dialogView.findViewById(R.id.productDescriptionEt);
            EditText productImageEt = dialogView.findViewById(R.id.productImageEt);
            
            dialogTitle.setText("Edit Product");
            productNameEt.setText(product.getProductName());
            productPriceEt.setText(String.valueOf(product.getPrice()));
            productDescriptionEt.setText(product.getDescription());
            productImageEt.setText(product.getImage());
            
            // TODO: Populate category spinner with categories and set selection
            
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = productNameEt.getText().toString().trim();
                    String priceStr = productPriceEt.getText().toString().trim();
                    String description = productDescriptionEt.getText().toString().trim();
                    String image = productImageEt.getText().toString().trim();
                    String stockStr = productStockEt.getText().toString().trim();
                    
                    if (!name.isEmpty() && !priceStr.isEmpty()) {
                        try {
                            double price = Double.parseDouble(priceStr);
                            int stock = stockStr.isEmpty() ? 100 : Integer.parseInt(stockStr);
                            
                            boolean success = productRepository.updateProduct(
                                product.getProductId(), name, product.getCategoryId(), price, description, image
                            );
                            
                            if (success) {
                                product.setProductName(name);
                                product.setPrice(price);
                                product.setDescription(description);
                                product.setImage(image);
                                notifyItemChanged(position);
                                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to update product", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Invalid price format", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        private void showDeleteConfirmation(int position) {
            Product product = productList.get(position);
            
            new AlertDialog.Builder(context)
                .setTitle("Delete Product")
                .setMessage("Are you sure you want to delete " + product.getProductName() + "?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = productRepository.deleteProduct(product.getProductId());
                        if (success) {
                            productList.remove(position);
                            notifyItemRemoved(position);
                            Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete product", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    }
}