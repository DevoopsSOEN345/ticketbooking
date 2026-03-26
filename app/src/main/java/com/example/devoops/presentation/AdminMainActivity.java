package com.example.devoops.presentation;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;

public class AdminMainActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button addBtn = findViewById(R.id.addBtn);

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

    }
    public void createNewEvent(String name, String dateTime, String category, String location, int totalSeats) {
        viewModel.createEvent(name, dateTime, category, location, totalSeats);
        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
    }

    public void editEvent(String id, String name, String dateTime, String category, String location, int totalSeats){
        viewModel.editEvent(id, name, dateTime, category, location, totalSeats);
        Toast.makeText(this, "Event added successfully!", Toast.LENGTH_SHORT).show();
    }

}
