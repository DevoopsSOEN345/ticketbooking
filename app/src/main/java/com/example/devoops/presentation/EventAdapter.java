package com.example.devoops.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> events = new ArrayList<>();
    private OnEventClickListener listener;
    private boolean isAdmin;

    public interface OnEventClickListener {
        void onEdit(Event event);
        void onDelete(Event event);
    }

    public EventAdapter(boolean isAdmin, OnEventClickListener listener) {
        this.isAdmin = isAdmin;
        this.listener = listener;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, category, location, totalSeats, availableSeats;
        Button editBtn, deleteBtn;

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
        holder.location.setText("📍 " + event.getLocation());
        holder.totalSeats.setText("Total Seats: " + event.getTotalSeats());
        holder.availableSeats.setText("Available: " + event.getOpenSeats());

        if (isAdmin) {
            holder.editBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setVisibility(View.VISIBLE);

            holder.editBtn.setOnClickListener(v -> listener.onEdit(event));
            holder.deleteBtn.setOnClickListener(v -> listener.onDelete(event));
        } else {
            holder.editBtn.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}