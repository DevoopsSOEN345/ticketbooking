package com.example.devoops.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;
import com.example.devoops.repository.AuthRepository;

public class AdminMainActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private EventAdapter adapter;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        authRepository = new AuthRepository();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button addBtn = findViewById(R.id.addBtn);
        Button btnLogout = findViewById(R.id.btnLogout);

        adapter = new EventAdapter(true, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEdit(Event event) {
                EventDialogFragment dialog = new EventDialogFragment();
                dialog.setEventToEdit(event);
                dialog.show(getSupportFragmentManager(), "event");
            }

            @Override
            public void onDelete(Event event) {
                viewModel.cancelEvent(event.getEventId());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EventViewModel.class);

        viewModel.getEvents().observe(this, events -> {
            adapter.setEvents(events);
        });

        addBtn.setOnClickListener(v -> {
            new EventDialogFragment().show(getSupportFragmentManager(), "event");
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        authRepository.signOut();
                        Intent intent = new Intent(AdminMainActivity.this, WelcomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    public void createNewEvent(String name, String dateTime, String category, String location, int totalSeats) {
        viewModel.createEvent(name, dateTime, category, location, totalSeats);
        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
    }

    public void editEvent(String id, String name, String dateTime, String category, String location, int totalSeats) {
        viewModel.editEvent(id, name, dateTime, category, location, totalSeats);
        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
    }
}
