package com.example.devoops.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.devoops.R;
import com.example.devoops.models.Event;

public class EventDialogFragment extends DialogFragment {

    private Event eventToEdit;

    public void setEventToEdit(Event event) {
        this.eventToEdit = event;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_event, container, false);

        EditText etName = v.findViewById(R.id.etName);
        EditText etCategory = v.findViewById(R.id.etCategory);
        EditText etLocation = v.findViewById(R.id.etLocation);
        EditText etSeats = v.findViewById(R.id.etSeats);
        EditText etDate = v.findViewById(R.id.etDate);

        TextView tvTitle = v.findViewById(R.id.tvTitle);
        Button btnSave = v.findViewById(R.id.btnSave);

        if (eventToEdit != null) {
            etName.setText(eventToEdit.getName());
            etCategory.setText(eventToEdit.getCategory());
            etLocation.setText(eventToEdit.getLocation());
            etSeats.setText(String.valueOf(eventToEdit.getTotalSeats()));
            etDate.setText(eventToEdit.getDateTime());
            btnSave.setText("Update Event");

            if (tvTitle != null) {
                tvTitle.setText("Edit Event");
            }
        }

        btnSave.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            String seatsStr = etSeats.getText().toString().trim();
            String date = etDate.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String category = etCategory.getText().toString().trim();

            if (name.isEmpty() || seatsStr.isEmpty() || date.isEmpty() || location.isEmpty() || category.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!date.matches("\\d{2}-\\d{2}-\\d{4}")) {
                etDate.setError("Use format DD-MM-YYYY");
                return;
            }

            try {
                int totalSeats = Integer.parseInt(seatsStr);

                if (eventToEdit == null) {
                    ((AdminMainActivity) getActivity()).createNewEvent(name, date, category, location, totalSeats);
                } else {
                    ((AdminMainActivity) getActivity()).editEvent(eventToEdit.getEventId(), name, date, category, location, totalSeats);
                }

                dismiss();
            } catch (NumberFormatException e) {
                etSeats.setError("Enter a valid number");
            }
        });

        return v;
    }
}