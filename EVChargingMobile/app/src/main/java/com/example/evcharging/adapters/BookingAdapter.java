/*
 * File: BookingAdapter.java
 * Purpose: RecyclerView adapter for bookings list
 */
package com.example.evcharging.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.evcharging.models.Booking;
import com.example.evcharging.R;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Button;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {
    private List<Booking> bookings;
    private Context ctx;
    private OnActionListener listener;

    public interface OnActionListener {
        void onModify(Booking b);
        void onCancel(Booking b);
    }

    public BookingAdapter(Context ctx, List<Booking> bookings, OnActionListener listener){
        this.ctx = ctx;
        this.bookings = bookings;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_booking, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking b = bookings.get(position);
        holder.tvTitle.setText("Reservation #" + (b.id != null ? b.id : "N/A"));
        holder.tvInfo.setText(b.startTime + " | " + b.status);
        holder.btnModify.setOnClickListener(v -> listener.onModify(b));
        holder.btnCancel.setOnClickListener(v -> listener.onCancel(b));
    }

    @Override public int getItemCount() { return bookings.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvInfo;
        Button btnModify, btnCancel;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvBookingTitle);
            tvInfo = itemView.findViewById(R.id.tvBookingInfo);
            btnModify = itemView.findViewById(R.id.btnModify);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
