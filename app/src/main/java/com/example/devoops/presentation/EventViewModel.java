package com.example.devoops.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.devoops.models.Event;
import com.example.devoops.repository.EventRepository;
import java.util.List;

public class EventViewModel extends ViewModel {
    private EventRepository repo;
    private LiveData<List<Event>> events;
    public EventViewModel() {
        repo = new EventRepository();
        events = repo.getEvents();
    }

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public void createEvent(String name, String dateTime, String category, String location, int totalSeats) {
        repo.createEvent(name, dateTime, category, location, totalSeats);
    }

    public void cancelEvent(String id) {
        repo.cancelEvent(id);
    }

    public void editEvent(String id, String name, String dateTime, String category, String location, int totalSeats) {
        repo.editEvent(id, name, dateTime, category, location, totalSeats);
    }
}
