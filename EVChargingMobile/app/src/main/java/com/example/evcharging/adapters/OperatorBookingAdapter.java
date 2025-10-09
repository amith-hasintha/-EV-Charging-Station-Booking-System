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
import com.example.evcharging.models.BookingApi;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class OperatorBookingAdapter extends RecyclerView.Adapter<OperatorBookingAdapter.ViewHolder> {

    private final List<BookingApi> bookingApiList;
    private final BookingListener listener;
    private final Context context;

    public interface BookingListener {
        void onConfirm(String bookingId); // Renamed for clarity
        void onCancelByOperator(String bookingId); // Renamed for clarity
    }

    public OperatorBookingAdapter(Context context, List<BookingApi> bookingApiList, BookingListener listener) {
        this.context = context;
        this.bookingApiList = bookingApiList;
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
        BookingApi bookingApi = bookingApiList.get(position);
        holder.bind(bookingApi, listener, context);
    }

    @Override
    public int getItemCount() {
        return bookingApiList.size();
    }

    public void updateData(List<BookingApi> newBookingApis) {
        bookingApiList.clear();
        bookingApiList.addAll(newBookingApis);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvStatus, tvStationId, tvTime, tvUserId;
        LinearLayout actionButtonsLayout;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvStationId = itemView.findViewById(R.id.tvStationId);
            tvTime = itemView.findViewById(R.id.tvBookingTime);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            actionButtonsLayout = itemView.findViewById(R.id.action_buttons_layout);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(final BookingApi bookingApi, final BookingListener listener, Context context) {
            tvBookingId.setText("Booking ID: #" + formatId(bookingApi.id));
            tvStationId.setText("Station: " + bookingApi.stationId);
            tvUserId.setText("User NIC: " + bookingApi.ownerNIC);
            tvTime.setText(formatDateTimeRange(bookingApi.startTime, bookingApi.endTime));

            // --- STATUS AND BUTTON VISIBILITY FIX ---
            // Backend Enum: Active = 0, Confirmed = 1, Completed = 2, Cancelled = 3

            switch (bookingApi.status) {
                case 0: // Active (This is what an operator acts on)
                    tvStatus.setText("ACTIVE");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_pending));
                    actionButtonsLayout.setVisibility(View.VISIBLE); // SHOW buttons
                    btnApprove.setText("Confirm"); // Set button text to "Confirm"
                    btnReject.setText("Cancel"); // Set button text to "Cancel"
                    break;
                case 1: // Confirmed
                    tvStatus.setText("CONFIRMED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_approved));
                    actionButtonsLayout.setVisibility(View.GONE); // HIDE buttons
                    break;
                case 3: // Cancelled
                    tvStatus.setText("CANCELLED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_rejected));
                    actionButtonsLayout.setVisibility(View.GONE); // HIDE buttons
                    break;
                case 2: // Completed
                default:
                    tvStatus.setText("COMPLETED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_completed));
                    actionButtonsLayout.setVisibility(View.GONE); // HIDE buttons
                    break;
            }

            // --- CORRECTED LISTENERS ---
            btnApprove.setOnClickListener(v -> listener.onConfirm(bookingApi.id));
            btnReject.setOnClickListener(v -> listener.onCancelByOperator(bookingApi.id));
        }


        // Helper functions remain the same
        private String formatId(String id) {
            if (id == null || id.length() < 8) return "N/A";
            return id.substring(id.length() - 8);
        }

        private String formatDateTimeRange(String start, String end) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());

                    ZonedDateTime startTime = ZonedDateTime.parse(start, inputFormatter);
                    ZonedDateTime endTime = ZonedDateTime.parse(end, inputFormatter);

                    String date = startTime.format(dateFormatter);
                    String startTimeStr = startTime.format(timeFormatter);
                    String endTimeStr = endTime.format(timeFormatter);

                    return String.format("%s  |  %s - %s", date, startTimeStr, endTimeStr);
                } catch (Exception e) {
                    return "Invalid Date Format";
                }
            }
            return "Date formatting unavailable";
        }
    }
}
