package com.example.devoops.presentation;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;
import com.google.firebase.auth.FirebaseAuth;

public class MyReservationsActivity extends AppCompatActivity {

    private ReservationViewModel reservationViewModel;
    private EventAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reservations);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewReservations);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Reuse EventAdapter in customer mode
        adapter = new EventAdapter(false, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEdit(Event event) { }

            @Override
            public void onDelete(Event event) { }
        });

        adapter.setReserveListener(new EventAdapter.OnReserveClickListener() {
            @Override
            public void onReserve(Event event) { }

            @Override
            public void onCancelReservation(Event event) {
                new AlertDialog.Builder(MyReservationsActivity.this)
                        .setTitle("Cancel Reservation")
                        .setMessage("Cancel your reservation for \"" + event.getName() + "\"?")
                        .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                            reservationViewModel.cancelReservation(event);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        reservationViewModel = new ViewModelProvider(this).get(ReservationViewModel.class);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reservationViewModel.init(uid);

        // Mark all events as reserved so button shows "Cancel Reservation"
        reservationViewModel.getReservedEventIds().observe(this, ids -> {
            if (ids != null) {
                adapter.setReservedEventIds(ids);
            }
        });

        // Display the resolved events
        reservationViewModel.getReservedEvents().observe(this, events -> {
            if (events != null) {
                adapter.setEvents(events);
                tvEmpty.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        reservationViewModel.getStatusMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
