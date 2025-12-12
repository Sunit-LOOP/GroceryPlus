package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Order;

import java.util.List;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onUpdateStatusClick(Order order);
    }

    public AdminOrderAdapter(Context context, List<Order> orders, OnOrderActionListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_admin_order, parent, false);
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
        TextView orderAmountTv;
        TextView orderStatusTv;
        Button updateStatusBtn;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTv = itemView.findViewById(R.id.orderIdTv);
            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            orderAmountTv = itemView.findViewById(R.id.orderAmountTv);
            orderStatusTv = itemView.findViewById(R.id.orderStatusTv);
            updateStatusBtn = itemView.findViewById(R.id.updateStatusBtn);

            updateStatusBtn.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onUpdateStatusClick(orders.get(position));
                }
            });
        }

        public void bind(Order order) {
            orderIdTv.setText("Order #" + order.getOrderId());
            orderDateTv.setText(order.getOrderDate()); // Assuming Date format is string
            orderAmountTv.setText("Rs. " + String.format("%.2f", order.getTotalAmount()));
            orderStatusTv.setText(order.getStatus());

            // Color code status
            String status = order.getStatus();
            if ("Pending".equalsIgnoreCase(status)) {
                orderStatusTv.setTextColor(Color.parseColor("#FF9800")); // Orange
            } else if ("Processing".equalsIgnoreCase(status)) {
                orderStatusTv.setTextColor(Color.parseColor("#2196F3")); // Blue
            } else if ("Delivered".equalsIgnoreCase(status)) {
                orderStatusTv.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else if ("Cancelled".equalsIgnoreCase(status)) {
                orderStatusTv.setTextColor(Color.parseColor("#F44336")); // Red
            } else {
                orderStatusTv.setTextColor(Color.DKGRAY);
            }
        }
    }
}
