package com.example.evcharging.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evcharging.R;
import com.example.evcharging.models.Notification; // You need to create this model
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void updateData(List<Notification> newNotifications) {
        notificationList.clear();
        notificationList.addAll(newNotifications);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivNotificationIcon;
        TextView tvNotificationTitle, tvNotificationMessage;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivNotificationIcon = itemView.findViewById(R.id.ivNotificationIcon);
            tvNotificationTitle = itemView.findViewById(R.id.tvNotificationTitle);
            tvNotificationMessage = itemView.findViewById(R.id.tvNotificationMessage);
        }

        void bind(Notification notification) {
            tvNotificationTitle.setText(notification.title);
            tvNotificationMessage.setText(notification.message);
            // You can set different icons based on notification.type
            ivNotificationIcon.setImageResource(R.drawable.ic_notifications);
        }
    }
}
