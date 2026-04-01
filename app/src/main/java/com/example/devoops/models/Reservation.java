package com.example.devoops.models;

public class Reservation {

    private String reservationId;
    private String userId;
    private String eventId;
    private long timestamp;

    // Required empty constructor for Firebase
    public Reservation() {}

    public Reservation(String reservationId, String userId, String eventId, long timestamp) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.eventId = eventId;
        this.timestamp = timestamp;
    }

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
