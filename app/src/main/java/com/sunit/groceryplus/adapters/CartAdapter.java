package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.CartItem;

import java.util.List;

/**
 * Adapter for displaying cart items with quantity controls
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateCartItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public double getTotalPrice() {
        double total = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getSubtotal();
            }
        }
        return total;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageIv;
        TextView productNameTv;
        TextView productPriceTv;
        TextView quantityTv;
        TextView subtotalTv;
        ImageButton decreaseBtn;
        ImageButton increaseBtn;
        ImageButton removeBtn;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageIv = itemView.findViewById(R.id.cartProductImageIv);
            productNameTv = itemView.findViewById(R.id.cartProductNameTv);
            productPriceTv = itemView.findViewById(R.id.cartProductPriceTv);
            quantityTv = itemView.findViewById(R.id.cartQuantityTv);
            subtotalTv = itemView.findViewById(R.id.cartSubtotalTv);
            decreaseBtn = itemView.findViewById(R.id.decreaseQuantityBtn);
            increaseBtn = itemView.findViewById(R.id.increaseQuantityBtn);
            removeBtn = itemView.findViewById(R.id.removeCartItemBtn);

            decreaseBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    CartItem item = cartItems.get(position);
                    int newQuantity = item.getQuantity() - 1;
                    if (newQuantity > 0) {
                        listener.onQuantityChanged(item, newQuantity);
                    } else {
                        listener.onRemoveItem(item);
                    }
                }
            });

            increaseBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    CartItem item = cartItems.get(position);
                    listener.onQuantityChanged(item, item.getQuantity() + 1);
                }
            });

            removeBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onRemoveItem(cartItems.get(position));
                }
            });
        }

        public void bind(CartItem item) {
            productNameTv.setText(item.getProductName());
            productPriceTv.setText("Rs. " + String.format("%.2f", item.getPrice()));
            quantityTv.setText(String.valueOf(item.getQuantity()));
            subtotalTv.setText("Rs. " + String.format("%.2f", item.getSubtotal()));

            // Set product image
            if (productImageIv != null) {
                int imageResource = getImageResource(item.getImage());
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
