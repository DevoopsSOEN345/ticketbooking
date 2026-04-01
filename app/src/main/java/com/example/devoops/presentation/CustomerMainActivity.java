package com.example.devoops.presentation;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;

import java.util.ArrayList;
import java.util.List;

public class CustomerMainActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private TextView tvActiveFilters;

    // Keep track of current filter values to pre-fill the dialog
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

        adapter = new EventAdapter(false, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEdit(Event event) {
                // empty bc customer do not have admin rights
            }

            @Override
            public void onDelete(Event event) {
                // empty bc customer do not have admin rights
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        // Observe filtered events instead of raw events
        viewModel.getFilteredEvents().observe(this, events -> {
            if (events != null) {
                adapter.setEvents(events);
            }
        });

        // Toggle search bar visibility when search icon is tapped
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

        // Live search as the user types
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Open filter dialog
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);

        EditText etFilterDate = dialogView.findViewById(R.id.etFilterDate);
        EditText etFilterLocation = dialogView.findViewById(R.id.etFilterLocation);
        EditText etFilterCategory = dialogView.findViewById(R.id.etFilterCategory);
        Button btnApply = dialogView.findViewById(R.id.btnApplyFilter);
        Button btnClear = dialogView.findViewById(R.id.btnClearFilter);

        // Pre-fill with current filter values
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
            tvActiveFilters.setText("Filters: " + String.join(" · ", active));
        }
    }
}
