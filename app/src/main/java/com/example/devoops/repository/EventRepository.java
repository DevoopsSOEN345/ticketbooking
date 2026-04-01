package com.example.devoops.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.devoops.models.Event;
import com.example.devoops.models.EventStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventRepository {
    private DatabaseReference db;

        public EventRepository(DatabaseReference mockedDb) {
            this.db = mockedDb;
        }


        public EventRepository() {
            try {
                this.db = FirebaseDatabase.getInstance().getReference();
            } catch (Exception e) { }
        }


        public void createEvent(String name, String dateTime, String category, String location, int totalSeats) {
        String eventId = UUID.randomUUID().toString();
        Event event = new Event(eventId, name, dateTime, category, location, totalSeats);
        DatabaseReference eventRef = db.child("events").child(eventId);

        eventRef.setValue(event)
                .addOnSuccessListener(aVoid -> Log.d("RTDB", "Event saved!"))
                .addOnFailureListener(e -> Log.e("RTDB", "Save failed", e));
    }

    public void editEvent(String eventId, String name, String dateTime, String category, String location, int totalSeats) {
        DatabaseReference eventRef = db.child("events").child(eventId);
        eventRef.get().addOnSuccessListener(snapshot -> {
            Event event = snapshot.getValue(Event.class);
            if (event != null) {
                event.setName(name);
                event.setDateTime(dateTime);
                event.setCategory(category);
                event.setLocation(location);

                int diff = totalSeats - event.getTotalSeats();
                event.setTotalSeats(totalSeats);
                event.setOpenSeats(event.getOpenSeats() + diff);

                eventRef.setValue(event)
                        .addOnSuccessListener(aVoid -> Log.d("RTDB", "Event updated!"))
                        .addOnFailureListener(e -> Log.e("RTDB", "Update failed", e));
            }
        }).addOnFailureListener(e -> Log.e("RTDB", "Fetch failed", e));
    }

    public void cancelEvent(String eventId) {
        DatabaseReference eventRef = db.child("events").child(eventId);
        eventRef.get().addOnSuccessListener(snapshot -> {
            Event event = snapshot.getValue(Event.class);
            if (event != null) {
                event.setEventStatus(EventStatus.CANCELLED);
                eventRef.setValue(event)
                        .addOnSuccessListener(aVoid -> Log.d("RTDB", "Event cancelled!"))
                        .addOnFailureListener(e -> Log.e("RTDB", "Cancel failed", e));
            }
        }).addOnFailureListener(e -> Log.e("RTDB", "Fetch failed", e));
    }

    public LiveData<List<Event>> getEvents() {
        MutableLiveData<List<Event>> liveData = new MutableLiveData<>();
        db.child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Event> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Event event = data.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(data.getKey());
                        list.add(event);
                    }
                }
                Log.d("REPO_DEBUG", "Fetched " + list.size() + " events from Firebase");
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REPO_DEBUG", "Read failed: " + error.getMessage());
            }
        });
        return liveData;
    }

    public interface EventListCallback {
        void onSuccess(List<Event> events);
        void onError(String error);
    }

    public boolean reserveSeats(String eventId, int quantity) {
        DatabaseReference eventRef = db.child("events").child(eventId);
        return false;
    }

    public boolean cancelSeats(String eventId, int quantity) {
        DatabaseReference eventRef = db.child("events").child(eventId);
        return false;
    }
}