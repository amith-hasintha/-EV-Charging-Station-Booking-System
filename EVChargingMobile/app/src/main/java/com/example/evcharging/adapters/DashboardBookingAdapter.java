package com.example.evcharging.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.evcharging.R;
import com.example.evcharging.models.BookingApi;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardBookingAdapter extends RecyclerView.Adapter<DashboardBookingAdapter.ViewHolder> {

    private final List<BookingApi> bookingApiList;
    private Context context; // Context for accessing resources

    public DashboardBookingAdapter(List<BookingApi> bookingApiList) {
        this.bookingApiList = bookingApiList;
    }

    /**
     * Updates the adapter's data set and refreshes the RecyclerView.
     * @param newBookingApis The new list of bookings to display.
     */
    public void updateBookings(List<BookingApi> newBookingApis) {
        this.bookingApiList.clear();
        this.bookingApiList.addAll(newBookingApis);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_dashboard_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BookingApi bookingApi = bookingApiList.get(position);
        holder.bind(bookingApi, context); // Pass context to the bind method
    }

    @Override
    public int getItemCount() {
        return bookingApiList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvTime, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs must match your item_dashboard_booking.xml
            tvStationName = itemView.findViewById(R.id.tvCardStationName);
            tvTime = itemView.findViewById(R.id.tvCardTime);
            tvStatus = itemView.findViewById(R.id.tvCardStatus);
        }

        void bind(BookingApi bookingApi, Context context) {
            tvStationName.setText(bookingApi.stationId != null ? bookingApi.stationId : "N/A");
            tvTime.setText(formatSimpleTime(bookingApi.startTime));

            int status = bookingApi.status;
            String statusText;
            int statusColor;

            // --- THIS IS THE FINAL FIX ---
            // Backend Enum: Active=0, Confirmed=1, Completed=2, Cancelled=3
            switch (status) {
                case 0: // Active
                    statusText = "ACTIVE";
                    statusColor = ContextCompat.getColor(context, R.color.orange_soda);
                    break;
                case 1: // Confirmed
                    statusText = "CONFIRMED";
                    statusColor = ContextCompat.getColor(context, R.color.emerald_green);
                    break;
                case 3: // Cancelled
                    statusText = "CANCELLED";
                    statusColor = ContextCompat.getColor(context, R.color.red_error);
                    break;
                case 2: // Completed
                default:
                    statusText = "COMPLETED";
                    statusColor = ContextCompat.getColor(context, R.color.cyan_blue);
                    break;
            }

            tvStatus.setText(statusText);

            // Dynamically change status color safely
            if (tvStatus.getBackground() instanceof GradientDrawable) {
                GradientDrawable background = (GradientDrawable) tvStatus.getBackground().mutate();
                background.setColor(statusColor);
            }
        }

        private String formatSimpleTime(String isoString) {
            if (isoString == null) return "N/A";
            try {
                // The java.time APIs are cleaner and safer than SimpleDateFormat
                DateTimeFormatter inputFormatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
                }
                DateTimeFormatter timeFormatter = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
                }
                ZonedDateTime zonedDateTime = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    zonedDateTime = ZonedDateTime.parse(isoString, inputFormatter);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    return zonedDateTime.format(timeFormatter);
                }
            } catch (Exception e) {
                // Fallback in case of parsing error
                return "Invalid Time";
            }
            return isoString;
        }
    }
}
