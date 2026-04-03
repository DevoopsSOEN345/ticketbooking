package com.example.devoops.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.devoops.models.Event;
import com.example.devoops.repository.EventRepository;
import java.util.ArrayList;
import java.util.List;


public class EventViewModel extends ViewModel {
    private EventRepository repo;
    private LiveData<List<Event>> events;

    // Filter state
    private MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private MutableLiveData<String> filterDate = new MutableLiveData<>("");
    private MutableLiveData<String> filterLocation = new MutableLiveData<>("");
    private MutableLiveData<String> filterCategory = new MutableLiveData<>("");

    // Filtered results exposed to the UI
    private MediatorLiveData<List<Event>> filteredEvents = new MediatorLiveData<>();

    public EventViewModel() {
        repo = new EventRepository();
        events = repo.getActiveEvents();
        setupFiltering();
    }

    public EventViewModel(EventRepository repo) {
        this.repo = repo;
        this.events = repo.getActiveEvents();
        setupFiltering();
    }

    private void setupFiltering() {
        // Re-filter whenever the source list or any filter criterion changes
        filteredEvents.addSource(events, list -> applyFilters());
        filteredEvents.addSource(searchQuery, q -> applyFilters());
        filteredEvents.addSource(filterDate, d -> applyFilters());
        filteredEvents.addSource(filterLocation, l -> applyFilters());
        filteredEvents.addSource(filterCategory, c -> applyFilters());
    }

    private void applyFilters() {
        List<Event> source = events.getValue();
        if (source == null) {
            filteredEvents.setValue(new ArrayList<>());
            return;
        }

        String query = searchQuery.getValue() != null ? searchQuery.getValue().toLowerCase().trim() : "";
        String date = filterDate.getValue() != null ? filterDate.getValue().toLowerCase().trim() : "";
        String location = filterLocation.getValue() != null ? filterLocation.getValue().toLowerCase().trim() : "";
        String category = filterCategory.getValue() != null ? filterCategory.getValue().toLowerCase().trim() : "";

        List<Event> result = new ArrayList<>();
        for (Event event : source) {
            // Search query matches event name
            if (!query.isEmpty() && !event.getName().toLowerCase().contains(query)) {
                continue;
            }
            // Date filter matches dateTime field
            if (!date.isEmpty() && !event.getDateTime().toLowerCase().contains(date)) {
                continue;
            }
            // Location filter
            if (!location.isEmpty() && !event.getLocation().toLowerCase().contains(location)) {
                continue;
            }
            // Category filter
            if (!category.isEmpty() && !event.getCategory().toLowerCase().contains(category)) {
                continue;
            }
            result.add(event);
        }
        filteredEvents.setValue(result);
    }

    // --- Getters for the UI ---

    public LiveData<List<Event>> getEvents() {
        return events;
    }

    public LiveData<List<Event>> getFilteredEvents() {
        return filteredEvents;
    }

    // --- Setters called from the UI ---

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setFilterDate(String date) {
        filterDate.setValue(date);
    }

    public void setFilterLocation(String location) {
        filterLocation.setValue(location);
    }

    public void setFilterCategory(String category) {
        filterCategory.setValue(category);
    }

    public void clearFilters() {
        searchQuery.setValue("");
        filterDate.setValue("");
        filterLocation.setValue("");
        filterCategory.setValue("");
    }

    // --- Existing CRUD operations ---

    public void createEvent(String name, String dateTime, String category, String location, int totalSeats) {
        repo.createEvent(name, dateTime, category, location, totalSeats);
        events = repo.getEvents();
    }

    public void cancelEvent(String id) {
        repo.cancelEvent(id);
        events = repo.getEvents();
    }

    public void editEvent(String id, String name, String dateTime, String category, String location, int totalSeats) {
        repo.editEvent(id, name, dateTime, category, location, totalSeats);
        events = repo.getEvents();
    }
}
