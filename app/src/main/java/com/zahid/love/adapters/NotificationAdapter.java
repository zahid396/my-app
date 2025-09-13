package com.zahid.love.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zahid.love.R;
import com.zahid.love.models.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList;
    private SimpleDateFormat dateFormat;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        
        holder.titleTextView.setText(notification.getTitle());
        holder.messageTextView.setText(notification.getBody() != null ? notification.getBody() : notification.getMessage());
        
        // Parse created_at string to date
        try {
            if (notification.getCreatedAt() != null) {
                // Parse ISO date string from Laravel
                java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", java.util.Locale.getDefault());
                Date date = isoFormat.parse(notification.getCreatedAt());
                holder.dateTextView.setText(dateFormat.format(date));
            } else {
                holder.dateTextView.setText(dateFormat.format(new Date(notification.getTimestamp())));
            }
        } catch (Exception e) {
            holder.dateTextView.setText(dateFormat.format(new Date()));
        }
        
        // Set icon based on notification type
        int iconResource;
        String notificationType = notification.getType() != null ? notification.getType() : "general";
        switch (notificationType) {
            case "tournament":
            case "admin":
                iconResource = R.drawable.ic_tournament;
                break;
            case "wallet":
            case "system":
                iconResource = R.drawable.ic_wallet;
                break;
            default:
                iconResource = R.drawable.ic_notification;
                break;
        }
        holder.iconImageView.setImageResource(iconResource);
        
        // Set read/unread appearance
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.7f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }
        
        // Add click listener to mark as read
        holder.itemView.setOnClickListener(v -> {
            if (!notification.isRead()) {
                markNotificationAsRead(notification, position);
            }
        });
    }
    
    private void markNotificationAsRead(Notification notification, int position) {
        com.zahid.love.api.ApiClient.getApiService().markNotificationAsRead(notification.getIdInt()).enqueue(new retrofit2.Callback<com.zahid.love.models.api.ApiResponse<Object>>() {
            @Override
            public void onResponse(retrofit2.Call<com.zahid.love.models.api.ApiResponse<Object>> call, retrofit2.Response<com.zahid.love.models.api.ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.zahid.love.models.api.ApiResponse<Object> apiResponse = response.body();
                    
                    if (apiResponse.isSuccess()) {
                        notification.setRead(true);
                        notifyItemChanged(position);
                    }
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<com.zahid.love.models.api.ApiResponse<Object>> call, Throwable t) {
                // Ignore failure for now
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView titleTextView, messageTextView, dateTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}