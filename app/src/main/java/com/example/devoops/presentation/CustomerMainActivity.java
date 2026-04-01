package com.example.devoops.presentation;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.devoops.R;
import com.example.devoops.models.Event;

public class CustomerMainActivity extends AppCompatActivity {
    private EventViewModel viewModel;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);

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

        viewModel.getEvents().observe(this, events -> {
            if (events != null) {
                adapter.setEvents(events);
            }
        });
    }
}