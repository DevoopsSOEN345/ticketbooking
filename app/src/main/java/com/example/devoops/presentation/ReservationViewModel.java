package com.example.devoops.presentation;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.devoops.models.Event;
import com.example.devoops.models.Reservation;
import com.example.devoops.repository.ReservationRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReservationViewModel extends ViewModel {

    private static final String TAG = "ReservationVM";

    private ReservationRepository reservationRepo;
    private DatabaseReference eventsRef;

    private MutableLiveData<List<Event>> reservedEvents = new MutableLiveData<>();
    private MutableLiveData<Set<String>> reservedEventIds = new MutableLiveData<>(new HashSet<>());
    private MutableLiveData<String> statusMessage = new MutableLiveData<>();

    // Maps eventId -> reservationId for quick lookup when cancelling
    private Map<String, String> eventToReservationMap = new HashMap<>();

    private String userId;
    private boolean initialized = false;

    public ReservationViewModel() {
        this.reservationRepo = new ReservationRepository();
        this.eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
    }

    // For testing
    public ReservationViewModel(ReservationRepository reservationRepo, DatabaseReference eventsRef) {
        this.reservationRepo = reservationRepo;
        this.eventsRef = eventsRef;
    }

    
     //ViewModel with the current user's ID. (not admin)
     
     
    public void init(String userId) {
        if (initialized) return;
        this.userId = userId;
        this.initialized = true;
        listenToUserReservations();
    }

    protected boolean isInitialized() {
        return initialized;
    }

    
     //Attaches a listener on the user's reservations for changes and calls events
     
     
    private void listenToUserReservations() {
        FirebaseDatabase.getInstance().getReference()
                .child("reservations")
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Reservation> reservations = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Reservation r = data.getValue(Reservation.class);
                            if (r != null) {
                                reservations.add(r);
                            }
                        }
                        Log.d(TAG, "Got " + reservations.size() + " reservations");
                        processReservations(reservations);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Reservation listen failed: " + error.getMessage());
                    }
                });
    }

    
     //Takes the raw reservation list, updates the ID set and map,
     //then fetches each event one by one.
     
    private void processReservations(List<Reservation> reservations) {
        eventToReservationMap.clear();
        Set<String> ids = new HashSet<>();

        for (Reservation r : reservations) {
            ids.add(r.getEventId());
            eventToReservationMap.put(r.getEventId(), r.getReservationId());
        }
        reservedEventIds.setValue(ids);

        if (reservations.isEmpty()) {
            reservedEvents.setValue(new ArrayList<>());
            return;
        }

        // Fetch each event individually
        List<Event> fetchedEvents = new ArrayList<>();
        final int total = reservations.size();
        final int[] done = {0};

        for (Reservation r : reservations) {
            eventsRef.child(r.getEventId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        event.setEventId(snapshot.getKey());
                        fetchedEvents.add(event);
                    }
                    done[0]++;
                    if (done[0] == total) {
                        Log.d(TAG, "Resolved " + fetchedEvents.size() + " events");
                        reservedEvents.setValue(new ArrayList<>(fetchedEvents));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    done[0]++;
                    if (done[0] == total) {
                        reservedEvents.setValue(new ArrayList<>(fetchedEvents));
                    }
                }
            });
        }
    }

    
    //Events Reserved
    public LiveData<Set<String>> getReservedEventIds() {
        return reservedEventIds;
    }

    
    public LiveData<List<Event>> getReservedEvents() {
        return reservedEvents;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    
    //Reserve a seat for the current user on a given event.
     
    public void reserveEvent(Event event) {
        if (userId == null) {
            statusMessage.setValue("Not logged in");
            return;
        }

        reservationRepo.createReservation(userId, event.getEventId(),
                new ReservationRepository.ReservationCallback() {
                    @Override
                    public void onSuccess(String reservationId) {
                        statusMessage.setValue("Reserved successfully!");
                        onReservationSuccess(event, reservationId);
                    }

                    @Override
                    public void onError(String error) {
                        statusMessage.setValue(error);
                    }
                });
    }

    
    //Cancel the current user's reservation

    public void cancelReservation(Event event) {
        String reservationId = eventToReservationMap.get(event.getEventId());
        if (reservationId == null) {
            statusMessage.setValue("Reservation not found");
            return;
        }

        reservationRepo.cancelReservation(reservationId, event.getEventId(),
                new ReservationRepository.ReservationCallback() {
                    @Override
                    public void onSuccess(String id) {
                        statusMessage.setValue("Reservation cancelled");
                        onCancellationSuccess(event, id);
                    }

                    @Override
                    public void onError(String error) {
                        statusMessage.setValue(error);
                    }
                });
    }

    // ─── Template Method hooks (OCP) ───────────────────────────────────────────
    // Empty by default. Subclasses override to add cross-cutting behaviour
    // (e.g. notifications) without modifying this class.

    protected void onReservationSuccess(Event event, String reservationId) { }

    protected void onCancellationSuccess(Event event, String reservationId) { }
}
