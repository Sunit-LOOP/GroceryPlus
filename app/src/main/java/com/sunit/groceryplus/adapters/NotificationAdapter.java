package com.sunit.groceryplus.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.sunit.groceryplus.R;
import com.sunit.groceryplus.models.Notification;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context context;
    private List<Notification> notificationList;

    public NotificationAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);

        holder.titleTv.setText(notification.getTitle());
        holder.messageTv.setText(notification.getMessage());
        holder.dateTv.setText(notification.getCreatedAt());

        if (!notification.isRead()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue for unread
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTv, messageTv, dateTv;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.notificationTitle);
            messageTv = itemView.findViewById(R.id.notificationMessage);
            dateTv = itemView.findViewById(R.id.notificationDate);
            cardView = (CardView) itemView;
        }
    }
}
