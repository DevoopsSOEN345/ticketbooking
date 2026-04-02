package com.example.devoops.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.devoops.models.Event;
import com.example.devoops.models.Reservation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReservationRepository {

    private static final String TAG = "ReservationRepo";
    private DatabaseReference db;

    public ReservationRepository() {
        this.db = FirebaseDatabase.getInstance().getReference();
    }

    // For testing — inject a mocked DatabaseReference
    public ReservationRepository(DatabaseReference mockedDb) {
        this.db = mockedDb;
    }

    /**
     * Create a reservation for the given user and event.
     * Uses a Firebase transaction to atomically decrement openSeats by 1.
     */
    public void createReservation(String userId, String eventId, ReservationCallback callback) {
        String reservationId = UUID.randomUUID().toString();
        DatabaseReference seatsRef = db.child("events").child(eventId).child("openSeats");

        // Transaction on openSeats only — customers don't need write access to the full event
        seatsRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                Integer seats = currentData.getValue(Integer.class);
                if (seats == null) {
                    return Transaction.success(currentData);
                }
                if (seats <= 0) {
                    return Transaction.abort();
                }
                currentData.setValue(seats - 1);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed: " + error.getMessage());
                    if (callback != null) callback.onError("Failed to reserve: " + error.getMessage());
                    return;
                }
                if (!committed) {
                    if (callback != null) callback.onError("No seats available");
                    return;
                }

                // Seat decremented successfully — now write the reservation record
                Reservation reservation = new Reservation(
                        reservationId, userId, eventId, System.currentTimeMillis());

                db.child("reservations").child(reservationId).setValue(reservation)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Reservation created: " + reservationId);
                            if (callback != null) callback.onSuccess(reservationId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Reservation write failed", e);
                            // Rollback: increment seat back
                            seatsRef.get().addOnSuccessListener(snap -> {
                                Integer s = snap.getValue(Integer.class);
                                if (s != null) {
                                    seatsRef.setValue(s + 1);
                                }
                            });
                            if (callback != null) callback.onError("Failed to save reservation");
                        });
            }
        });
    }

    
    // Cancel a reservation
    
    public void cancelReservation(String reservationId, String eventId, ReservationCallback callback) {
        // Remove the reservation record
        db.child("reservations").child(reservationId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Increment the seat count back — only touch openSeats
                    DatabaseReference seatsRef = db.child("events").child(eventId).child("openSeats");
                    seatsRef.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                            Integer seats = currentData.getValue(Integer.class);
                            if (seats == null) {
                                return Transaction.success(currentData);
                            }
                            currentData.setValue(seats + 1);
                            return Transaction.success(currentData);
                        }

                        @Override
                        public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                            if (error != null) {
                                Log.e(TAG, "Seat increment failed: " + error.getMessage());
                            }
                            if (callback != null) callback.onSuccess(reservationId);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Cancel failed", e);
                    if (callback != null) callback.onError("Failed to cancel reservation");
                });
    }

    
    //Get all reservations for a specific user (live-updating).
    
    public LiveData<List<Reservation>> getReservationsForUser(String userId) {
        MutableLiveData<List<Reservation>> liveData = new MutableLiveData<>();

        db.child("reservations")
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Reservation> list = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Reservation r = data.getValue(Reservation.class);
                            if (r != null) {
                                list.add(r);
                            }
                        }
                        Log.d(TAG, "Fetched " + list.size() + " reservations for user " + userId);
                        liveData.setValue(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Read failed: " + error.getMessage());
                    }
                });

        return liveData;
    }

    
     //Fetch a single event by ID (one-shot read).
     
    public void getEventById(String eventId, EventCallback callback) {
        db.child("events").child(eventId).get()
                .addOnSuccessListener(snapshot -> {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(snapshot.getKey());
                        callback.onSuccess(event);
                    } else {
                        callback.onError("Event not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // --- Callbacks ---

    public interface ReservationCallback {
        void onSuccess(String reservationId);
        void onError(String error);
    }

    public interface EventCallback {
        void onSuccess(Event event);
        void onError(String error);
    }
}
