package com.example.evcharging.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
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
import com.example.evcharging.models.BookingApi;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<BookingApi> bookingApiList;
    private final ApiService apiService;
    private final String authToken;
    private Context context; // Keep context for resources

    public BookingAdapter(List<BookingApi> bookingApiList, ApiService apiService, String authToken) {
        this.bookingApiList = bookingApiList;
        this.apiService = apiService;
        this.authToken = authToken;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        BookingApi bookingApi = bookingApiList.get(position);
        holder.bind(bookingApi, context);

        // Cancellation logic is now handled inside the ViewHolder's bind method
        // to simplify state management and ensure buttons are only active when they should be.
        holder.btnCancelBooking.setOnClickListener(v -> {
            // Only allow cancellation if the booking status is 'Active' (0)
            if (bookingApi.status == 0) {
                cancelBooking(bookingApi, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingApiList.size();
    }

    private void cancelBooking(final BookingApi bookingApi, final int position) {
        if (position == RecyclerView.NO_POSITION) return;

        // Use the correct API endpoint for user cancellation
        apiService.cancelBooking(authToken, bookingApi.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Booking successfully cancelled.", Toast.LENGTH_SHORT).show();
                    // Update status locally to 'Cancelled' (3) and refresh the item
                    bookingApi.status = 3;
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "Failed to cancel booking (Error: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(context, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
        }
    }

    // --- ViewHolder ---
    public class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvStationName, tvBookingTime, tvStatus;
        ImageView ivQrCode;
        Button btnCancelBooking;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs now EXACTLY match the item_booking.xml layout file
            tvStationName = itemView.findViewById(R.id.tvBookingStationName);
            tvBookingTime = itemView.findViewById(R.id.tvBookingTime);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            ivQrCode = itemView.findViewById(R.id.ivQrCode);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
        }

        public void bind(final BookingApi bookingApi, Context context) {
            tvStationName.setText("Station: " + bookingApi.stationId);
            tvBookingTime.setText(formatDateTimeRange(bookingApi.startTime, bookingApi.endTime));

            // Hide everything by default, then show based on status
            ivQrCode.setVisibility(View.GONE);
            btnCancelBooking.setVisibility(View.GONE);

            // --- THE FINAL NUMERIC STATUS FIX ---
            // Backend Enum: Active=0, Confirmed=1, Completed=2, Cancelled=3, NoShow=4
            switch (bookingApi.status) {
                case 0: // Active (User sees this as "Pending Confirmation")
                    tvStatus.setText("ACTIVE");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_pending));
                    btnCancelBooking.setVisibility(View.VISIBLE); // Show cancel button for active bookings
                    break;

                case 1: // Confirmed (User sees this as "Approved")
                    tvStatus.setText("CONFIRMED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_approved));
                    ivQrCode.setVisibility(View.VISIBLE); // Show QR code for confirmed bookings
                    generateAndSetQrCode(ivQrCode, bookingApi.id);
                    break;

                case 2: // Completed
                    tvStatus.setText("COMPLETED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_completed)); // Assumes a blue-ish color
                    break;

                case 3: // Cancelled
                    tvStatus.setText("CANCELLED");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_rejected));
                    break;

                case 4: // NoShow
                default:
                    tvStatus.setText("UNKNOWN");
                    tvStatus.setBackground(ContextCompat.getDrawable(context, R.drawable.status_background_rejected));
                    break;
            }
        }

        private String formatDateTimeRange(String start, String end) {
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

                return String.format("%s | %s - %s", date, startTimeStr, endTimeStr);
            } catch (Exception e) {
                return "Invalid Date";
            }
        }
    }
}
