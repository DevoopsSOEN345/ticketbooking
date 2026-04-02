package com.example.devoops.presentation;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;
    private OnReserveClickListener reserveListener;
    private boolean isAdmin;
    private Set<String> reservedEventIds = new HashSet<>();

    public interface OnEventClickListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public interface OnReserveClickListener {
        void onReserve(Event event);
        void onCancelReservation(Event event);
    }

    public EventAdapter(boolean isAdmin, OnEventClickListener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setReserveListener(OnReserveClickListener reserveListener) {
        this.reserveListener = reserveListener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    
     //Whether the button says "Reserve" or "Cancel Reservation".
    
    public void setReservedEventIds(Set<String> reservedEventIds) {
        this.reservedEventIds = reservedEventIds != null ? reservedEventIds : new HashSet<>();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, category, location, totalSeats, availableSeats;
        Button editBtn, deleteBtn, reserveBtn;
        LinearLayout adminButtonsLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            category = itemView.findViewById(R.id.category);
            location = itemView.findViewById(R.id.location);
            totalSeats = itemView.findViewById(R.id.totalSeats);
            availableSeats = itemView.findViewById(R.id.availableSeats);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
            reserveBtn = itemView.findViewById(R.id.reserveBtn);
            adminButtonsLayout = itemView.findViewById(R.id.adminButtonsLayout);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Event event = events.get(position);

        holder.title.setText(event.getName());
        holder.date.setText("Date: " + event.getDateTime());
        holder.category.setText(event.getCategory());
        holder.location.setText("\uD83D\uDCCD " + event.getLocation());
        holder.totalSeats.setText("Total Seats: " + event.getTotalSeats());
        holder.availableSeats.setText("Available: " + event.getOpenSeats());

        if (isAdmin) {
            // Admin: show Edit/Cancel, hide Reserve
            holder.adminButtonsLayout.setVisibility(View.VISIBLE);
            holder.reserveBtn.setVisibility(View.GONE);

            holder.editBtn.setOnClickListener(v -> listener.onEdit(event));
            holder.deleteBtn.setOnClickListener(v -> listener.onDelete(event));
        } else {
            // Customer: hide Edit/Cancel, show Reserve
            holder.adminButtonsLayout.setVisibility(View.GONE);
            holder.reserveBtn.setVisibility(View.VISIBLE);

            boolean alreadyReserved = reservedEventIds.contains(event.getEventId());

            if (alreadyReserved) {
                // User already has a reservation — show cancel option
                holder.reserveBtn.setText("Cancel Reservation");
                holder.reserveBtn.setBackgroundTintList(
                        ColorStateList.valueOf(0xFFD32F2F)); // red
                holder.reserveBtn.setEnabled(true);
                holder.reserveBtn.setOnClickListener(v -> {
                    if (reserveListener != null) {
                        reserveListener.onCancelReservation(event);
                    }
                });
            } else if (event.getOpenSeats() <= 0) {
                // No seats left
                holder.reserveBtn.setText("Sold Out");
                holder.reserveBtn.setEnabled(false);
                holder.reserveBtn.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF9E9E9E)); // grey
            } else {
                // Available to reserve
                holder.reserveBtn.setText("Reserve");
                holder.reserveBtn.setEnabled(true);
                holder.reserveBtn.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF388E3C)); // green
                holder.reserveBtn.setOnClickListener(v -> {
                    if (reserveListener != null) {
                        reserveListener.onReserve(event);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
