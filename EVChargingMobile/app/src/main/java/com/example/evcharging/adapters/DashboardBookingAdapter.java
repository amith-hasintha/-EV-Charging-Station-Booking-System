package com.example.evcharging.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evcharging.R;
import com.example.evcharging.models.Booking;
import java.util.List;

public class DashboardBookingAdapter extends RecyclerView.Adapter<DashboardBookingAdapter.ViewHolder> {

    // Make the list final, it will be managed by the updateData method
    private final List<Booking> bookingList;

    public DashboardBookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    // --- START: ADD THIS NEW METHOD ---
    /**
     * Updates the adapter's data set and refreshes the RecyclerView.
     * @param newBookings The new list of bookings to display.
     */
    public void updateData(List<Booking> newBookings) {
        this.bookingList.clear();
        this.bookingList.addAll(newBookings);
        notifyDataSetChanged(); // Let the adapter know the data has changed
    }
    // --- END: ADD THIS NEW METHOD ---


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvTime, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tvCardStationName);
            tvTime = itemView.findViewById(R.id.tvCardTime);
            tvStatus = itemView.findViewById(R.id.tvCardStatus);
        }

        void bind(Booking booking) {
            // In a real app, you'd fetch the station name from the ID. For now, we show the ID.
            tvStationName.setText(booking.stationId != null ? booking.stationId : "N/A");

            // You can add proper date formatting here later
            tvTime.setText(booking.startTime);
            tvStatus.setText(booking.status != null ? booking.status.toUpperCase() : "UNKNOWN");

            // Dynamically change status color
            if (tvStatus.getBackground() instanceof GradientDrawable) {
                GradientDrawable background = (GradientDrawable) tvStatus.getBackground().mutate();
                int statusColor = Color.parseColor("#E67E22"); // Default orange for pending
                if ("approved".equalsIgnoreCase(booking.status)) {
                    statusColor = Color.parseColor("#2ECC71"); // Green
                } else if ("cancelled".equalsIgnoreCase(booking.status) || "rejected".equalsIgnoreCase(booking.status)) {
                    statusColor = Color.parseColor("#E74C3C"); // Red
                }
                background.setColor(statusColor);
            }
        }
    }
}
