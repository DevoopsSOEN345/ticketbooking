package com.example.devoops.models;

import com.google.firebase.database.Exclude;

public class Event {
    private String eventId;
    private String name;
    private String dateTime;
    private String category;
    private String location;
    private int totalSeats;
    private int openSeats;
    private EventStatus eventStatus;

    public Event() { }

    public Event(String eventId, String name, String dateTime, String category, String location, int totalSeats) {
        this.eventId = eventId;
        this.name = name;
        this.dateTime = dateTime;
        this.category = category;
        this.location = location;
        this.totalSeats = totalSeats;
        this.openSeats = totalSeats;
        this.eventStatus = EventStatus.ACTIVE;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }
    public int getOpenSeats() { return openSeats; }
    public void setOpenSeats(int openSeats) { this.openSeats = openSeats; }
    public EventStatus getEventStatus() { return eventStatus; }
    public void setEventStatus(EventStatus eventStatus) { this.eventStatus = eventStatus; }

    @Exclude
    public boolean hasEnoughSeats(int quantity) { return openSeats >= quantity; }

    public void reserveSeats(int quantity) { openSeats -= quantity; }
    public void cancelSeats(int quantity) { openSeats = Math.min(totalSeats, openSeats + quantity); }
    public void cancelEvent() { this.eventStatus = EventStatus.CANCELLED; }

    @Exclude
    public boolean isActive() { return eventStatus == EventStatus.ACTIVE; }
}
