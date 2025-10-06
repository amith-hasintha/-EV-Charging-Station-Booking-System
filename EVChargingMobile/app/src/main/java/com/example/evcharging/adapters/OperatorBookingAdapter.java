package com.example.evcharging.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evcharging.R;
import com.example.evcharging.models.Booking;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OperatorBookingAdapter extends RecyclerView.Adapter<OperatorBookingAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final BookingListener listener;
    private final Context context;

    public interface BookingListener {
        void onApprove(String bookingId);
        void onReject(String bookingId);
    }

    public OperatorBookingAdapter(Context context, List<Booking> bookingList, BookingListener listener) {
        this.context = context;
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_operator_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking, listener, context);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public void updateData(List<Booking> newBookings) {
        bookingList.clear();
        bookingList.addAll(newBookings);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvStatus, tvStationId, tvTime, tvUserId;
        LinearLayout actionButtonsLayout;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ensure these IDs match your item_operator_booking.xml
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvStationId = itemView.findViewById(R.id.tvStationId);
            tvTime = itemView.findViewById(R.id.tvBookingTime);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            actionButtonsLayout = itemView.findViewById(R.id.action_buttons_layout);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(final Booking booking, final BookingListener listener, Context context) {
            // --- DATA FORMATTING FIXES ---
            tvBookingId.setText("Booking ID: #" + formatId(booking.id));
            tvStationId.setText("Station: " + booking.stationId);
            // Use userEmail if available, otherwise fallback to userId
            String userIdentifier = booking.userId != null ? booking.userId : booking.userId;
            tvUserId.setText("User: " + userIdentifier);
            tvTime.setText(formatDateTimeRange(booking.startTime, booking.endTime));

            // --- STATUS AND BUTTON VISIBILITY FIX ---
            String status = booking.status != null ? booking.status.toLowerCase() : "unknown";
            tvStatus.setText(status.toUpperCase());

            switch (status) {
                case "pending":
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_pending));
                    actionButtonsLayout.setVisibility(View.VISIBLE); // SHOW buttons for pending
                    break;
                case "approved":
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_approved));
                    actionButtonsLayout.setVisibility(View.GONE); // HIDE for others
                    break;
                default: // Handles "rejected", "cancelled", etc.
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_rejected));
                    actionButtonsLayout.setVisibility(View.GONE); // HIDE for others
                    break;
            }

            btnApprove.setOnClickListener(v -> listener.onApprove(booking.id));
            btnReject.setOnClickListener(v -> listener.onReject(booking.id));
        }

        // Helper function to format a long ID string
        private String formatId(String id) {
            if (id == null || id.length() < 8) {
                return "N/A";
            }
            return id.substring(id.length() - 8);
        }

        // Helper function to format the date and time strings (Simplified and Corrected)
        private String formatDateTimeRange(String start, String end) {
            // The java.time APIs require Android API level 26+
            // Your project settings should support this, making the version checks unnecessary.
            try {
                DateTimeFormatter inputFormatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
                }
                DateTimeFormatter dateFormatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
                }
                DateTimeFormatter timeFormatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
                }

                ZonedDateTime startTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startTime = ZonedDateTime.parse(start, inputFormatter);
                }
                ZonedDateTime endTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    endTime = ZonedDateTime.parse(end, inputFormatter);
                }

                String date = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    date = startTime.format(dateFormatter);
                }
                String startTimeStr = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startTimeStr = startTime.format(timeFormatter);
                }
                String endTimeStr = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    endTimeStr = endTime.format(timeFormatter);
                }

                return String.format("%s  |  %s - %s", date, startTimeStr, endTimeStr);
            } catch (Exception e) {
                // If parsing fails for any reason, return a safe fallback string.
                // This prevents the app from crashing due to unexpected date formats.
                return "Invalid Date Format";
            }
        }
    }
}
