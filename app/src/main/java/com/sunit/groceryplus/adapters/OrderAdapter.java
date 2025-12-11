package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Order;

import java.util.List;

/**
 * Adapter for displaying order history
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    public void updateOrders(List<Order> newOrders) {
        this.orders = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdTv;
        TextView orderDateTv;
        TextView orderStatusTv;
        TextView orderTotalTv;
        TextView orderItemCountTv;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            orderStatusTv = itemView.findViewById(R.id.orderStatusTv);
            orderTotalTv = itemView.findViewById(R.id.orderTotalTv);
            orderItemCountTv = itemView.findViewById(R.id.orderItemCountTv);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });
        }

        public void bind(Order order) {
            orderIdTv.setText("Order #" + order.getOrderId());
            orderDateTv.setText(order.getOrderDate());
            orderStatusTv.setText(order.getStatus().toUpperCase());
            orderTotalTv.setText("Rs. " + String.format("%.2f", order.getTotalAmount()));
            
            if (orderItemCountTv != null) {
                orderItemCountTv.setText(order.getItemCount() + " items");
            }

            // Set status color
            int statusColor = getStatusColor(order.getStatus());
            orderStatusTv.setTextColor(statusColor);
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return context.getResources().getColor(android.R.color.holo_orange_dark);
                case "processing":
                    return context.getResources().getColor(android.R.color.holo_blue_dark);
                case "shipped":
                    return context.getResources().getColor(android.R.color.holo_purple);
                case "delivered":
                    return context.getResources().getColor(android.R.color.holo_green_dark);
                case "cancelled":
                    return context.getResources().getColor(android.R.color.holo_red_dark);
                default:
                    return context.getResources().getColor(android.R.color.black);
            }
        }
    }
}
