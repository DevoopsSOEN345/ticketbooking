package com.example.devoops.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;

public class CustomerMainActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private ReservationViewModel reservationViewModel;
    private EventAdapter adapter;
    private TextView tvActiveFilters;

    private String currentFilterDate = "";
    private String currentFilterLocation = "";
    private String currentFilterCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        EditText etSearch = findViewById(R.id.etSearch);
        ImageView btnSearch = findViewById(R.id.btnSearch);
        ImageView btnFilter = findViewById(R.id.btnFilter);
        tvActiveFilters = findViewById(R.id.tvActiveFilters);
        Button btnMyReservations = findViewById(R.id.btnMyReservations);

        // Set up adapter (customer mode)
        adapter = new EventAdapter(false, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEdit(Event event) {
                // Not used for customers
            }

            @Override
            public void onDelete(Event event) {
                // Not used for customers
            }
        });

        // Reserve listener for the customer
        adapter.setReserveListener(new EventAdapter.OnReserveClickListener() {
            @Override
            public void onReserve(Event event) {
                new AlertDialog.Builder(CustomerMainActivity.this)
                        .setTitle("Confirm Reservation")
                        .setMessage("Reserve a seat for \"" + event.getName() + "\"?")
                        .setPositiveButton("Reserve", (dialog, which) -> {
                            reservationViewModel.reserveEvent(event);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

            @Override
            public void onCancelReservation(Event event) {
                new AlertDialog.Builder(CustomerMainActivity.this)
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

        // ViewModels
        viewModel = new ViewModelProvider(this).get(EventViewModel.class);
        reservationViewModel = new ViewModelProvider(this).get(ReservationViewModel.class);

        // Reservation VM with user's UID
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reservationViewModel.init(uid);

        // Observe  events
        viewModel.getFilteredEvents().observe(this, events -> {
            if (events != null) {
                adapter.setEvents(events);
            }
        });

        // Observe reserved event IDs so the adapter shows "Cancel Reservation"
        reservationViewModel.getReservedEventIds().observe(this, ids -> {
            if (ids != null) {
                adapter.setReservedEventIds(ids);
            }
        });

        // Observe status messages (success/error toasts)
        reservationViewModel.getStatusMessage().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        // Search Bar
        btnSearch.setOnClickListener(v -> {
            if (etSearch.getVisibility() == View.GONE) {
                etSearch.setVisibility(View.VISIBLE);
                etSearch.requestFocus();
            } else {
                etSearch.setVisibility(View.GONE);
                etSearch.setText("");
                viewModel.setSearchQuery("");
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Filter Button 
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // My Reservations button
        btnMyReservations.setOnClickListener(v -> {
            Intent intent = new Intent(CustomerMainActivity.this, MyReservationsActivity.class);
            startActivity(intent);
        });
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);

        EditText etFilterDate = dialogView.findViewById(R.id.etFilterDate);
        EditText etFilterLocation = dialogView.findViewById(R.id.etFilterLocation);
        EditText etFilterCategory = dialogView.findViewById(R.id.etFilterCategory);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);
        Button btnClear = dialogView.findViewById(R.id.btnClearFilter);

        etFilterDate.setText(currentFilterDate);
        etFilterLocation.setText(currentFilterLocation);
        etFilterCategory.setText(currentFilterCategory);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnApply.setOnClickListener(v -> {
            currentFilterDate = etFilterDate.getText().toString().trim();
            currentFilterLocation = etFilterLocation.getText().toString().trim();
            currentFilterCategory = etFilterCategory.getText().toString().trim();

            viewModel.setFilterDate(currentFilterDate);
            viewModel.setFilterLocation(currentFilterLocation);
            viewModel.setFilterCategory(currentFilterCategory);

            updateActiveFiltersLabel();
            dialog.dismiss();
        });

        btnClear.setOnClickListener(v -> {
            currentFilterDate = "";
            currentFilterLocation = "";
            currentFilterCategory = "";

            viewModel.clearFilters();
            updateActiveFiltersLabel();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateActiveFiltersLabel() {
        List<String> active = new ArrayList<>();
        if (!currentFilterDate.isEmpty()) active.add("Date: " + currentFilterDate);
        if (!currentFilterLocation.isEmpty()) active.add("Location: " + currentFilterLocation);
        if (!currentFilterCategory.isEmpty()) active.add("Category: " + currentFilterCategory);

        if (active.isEmpty()) {
            tvActiveFilters.setVisibility(View.GONE);
        } else {
            tvActiveFilters.setVisibility(View.VISIBLE);
            tvActiveFilters.setText("Filters: " + String.join(" \u00B7 ", active));
        }
    }
}
