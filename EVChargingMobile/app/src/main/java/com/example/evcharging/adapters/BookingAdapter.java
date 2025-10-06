package com.example.evcharging.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.evcharging.R;
import com.example.evcharging.api.ApiService;
import com.example.evcharging.models.Booking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookingList;
    private ApiService apiService;
    private String authToken;
    private Context context; // Add context for colors and other resources

    public BookingAdapter(List<Booking> bookingList, ApiService apiService, String authToken) {
        this.bookingList = bookingList;
        this.apiService = apiService;
        this.authToken = authToken;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext(); // Get context from the parent view
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookingList.get(position);

        // Here you would ideally fetch Station details by booking.stationId to show the name
        holder.tvStationName.setText(booking.stationId);
        holder.tvStatus.setText(booking.status.toUpperCase());

        // --- NEW: Format the date and time for display ---
        holder.tvStartTime.setText(formatDateString(booking.startTime));
        holder.tvEndTime.setText(formatDateString(booking.endTime));

        // --- UI LOGIC BASED ON STATUS ---

        // Set status color dynamically
        GradientDrawable statusBackground = (GradientDrawable) holder.tvStatus.getBackground().mutate();
        int statusColor = ContextCompat.getColor(context, R.color.orange_soda); // Default for pending
        if ("approved".equalsIgnoreCase(booking.status)) {
            statusColor = ContextCompat.getColor(context, R.color.emerald_green);
        } else if ("completed".equalsIgnoreCase(booking.status)) {
            statusColor = ContextCompat.getColor(context, R.color.cyan_blue);
        } else if ("cancelled".equalsIgnoreCase(booking.status) || "rejected".equalsIgnoreCase(booking.status)) {
            statusColor = ContextCompat.getColor(context, R.color.holo_red_dark);
        }
        statusBackground.setColor(statusColor);


        // Show QR code for "Approved" bookings
        if ("approved".equalsIgnoreCase(booking.status)) {
            holder.ivQrCode.setVisibility(View.VISIBLE);
            holder.btnCancelBooking.setVisibility(View.GONE);
            generateAndSetQrCode(holder.ivQrCode, booking.id);
        }
        // Show "Cancel" button ONLY for "Pending" bookings
        else if ("pending".equalsIgnoreCase(booking.status)) {
            holder.ivQrCode.setVisibility(View.GONE);
            holder.btnCancelBooking.setVisibility(View.VISIBLE);
            // *** THE FIX: Pass the holder object to the listener ***
            holder.btnCancelBooking.setOnClickListener(v -> cancelBooking(booking, holder));
        }
        // Hide QR and Cancel button for all other statuses (Completed, Rejected, etc.)
        else {
            holder.ivQrCode.setVisibility(View.GONE);
            holder.btnCancelBooking.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    // *** THE FIX: Updated method signature to accept the holder ***
    private void cancelBooking(final Booking booking, final BookingViewHolder holder) {
        final int position = holder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return; // Item already removed, ignore.

        apiService.cancelBooking(authToken, booking.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Booking successfully cancelled.", Toast.LENGTH_SHORT).show();
                    // Update status locally and refresh the item view
                    booking.status = "cancelled";
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Failed to cancel booking. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Helper method to generate QR Code ---
    private void generateAndSetQrCode(ImageView imageView, String bookingId) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(bookingId, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imageView.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(context, "Could not generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- NEW: Helper method to format date strings ---
    private String formatDateString(String isoString) {
        if (isoString == null) return "N/A";
        // Input format from server: "YYYY-MM-DDTHH:MM:SSZ"
        SimpleDateFormat serverFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        serverFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Desired output format: "dd MMM yyyy, hh:mm a" (e.g., 28 Oct 2025, 10:00 AM)
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        displayFormat.setTimeZone(TimeZone.getDefault()); // Display in user's local timezone

        try {
            Date date = serverFormat.parse(isoString);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            // Fallback to showing the raw string if parsing fails
            return isoString;
        }
    }


    // --- ViewHolder ---
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvStatus, tvStartTime, tvEndTime; // Updated to match layout
        ImageView ivQrCode;
        Button btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStationName = itemView.findViewById(R.id.tvBookingStationName);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvStartTime = itemView.findViewById(R.id.tvStartTime); // From improved layout
            tvEndTime = itemView.findViewById(R.id.tvEndTime);     // From improved layout
            ivQrCode = itemView.findViewById(R.id.ivQrCode);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }
    }
}
