package com.example.devoops.repository;

import android.util.Log;

import com.example.devoops.models.Event;
import com.example.devoops.models.EventStatus;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public void listEvents(EventListCallback callback) {
        DatabaseReference eventsRef = db.child("events");
        eventsRef.get().addOnSuccessListener(snapshot -> {
            List<Event> events = new ArrayList<>();
            for (var child : snapshot.getChildren()) {
                Event event = child.getValue(Event.class);
                if (event != null) {
                    events.add(event);
                }
            }
            callback.onSuccess(events);
        }).addOnFailureListener(e -> {
            Log.e("RTDB", "Fetch failed", e);
            callback.onError(e.getMessage());
        });
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