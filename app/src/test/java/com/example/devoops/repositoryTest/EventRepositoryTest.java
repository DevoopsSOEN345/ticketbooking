package com.example.devoops.repositoryTest;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.devoops.models.Event;
import com.example.devoops.repository.EventRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;

import android.util.Log;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class EventRepositoryTest {

    private EventRepository repo;

    @Mock private DatabaseReference mockDb;
    @Mock private DatabaseReference mockEventsChild;
    @Mock private DatabaseReference mockEventChild;
    @Mock private Task<Void> mockTask;

    @BeforeEach
    void setup() {
        lenient().when(mockDb.child("events")).thenReturn(mockEventsChild);
        lenient().when(mockEventsChild.child(anyString())).thenReturn(mockEventChild);
        lenient().when(mockEventChild.setValue(any())).thenReturn(mockTask);
        lenient().when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        lenient().when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        repo = new EventRepository(mockDb);
    }

    @Test
    void createEvent_Success() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            repo.createEvent("Concert", "2026-04-01 20:00", "Music", "Montreal", 100);

            ArgumentCaptor<OnSuccessListener<Void>> captor = ArgumentCaptor.forClass(OnSuccessListener.class);
            verify(mockTask).addOnSuccessListener(captor.capture());

            captor.getValue().onSuccess(null);

            mockedLog.verify(() -> Log.d(eq("RTDB"), eq("Event saved!")));
            verify(mockEventChild).setValue(any(Event.class));
        }
    }

    @Test
    void createEvent_Failure() {
        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            ArgumentCaptor<OnFailureListener> captor = ArgumentCaptor.forClass(OnFailureListener.class);

            repo.createEvent("Concert", "2026-04-01 20:00", "Music", "Montreal", 100);

            verify(mockTask).addOnFailureListener(captor.capture());

            Exception testException = new Exception("Database Error");
            captor.getValue().onFailure(testException);

            mockedLog.verify(() -> Log.e(eq("RTDB"), anyString(), eq(testException)));
        }
    }

    @Test
    void editEvent_Success() {
        //DataSnapshot is used here to simulate Firebase returning an Event
        //This allows to test repository logic (updateEvent/cancelEvent) instead of only verifying get() calls
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Event.class)).thenReturn(new Event(
                "eventId", "Updated Concert", "2026-04-02 21:00",
                "Music", "Montreal", 150
        ));

        repo.editEvent("eventId", "Updated Concert", "2026-04-02 21:00", "Music", "Montreal", 150);

        verify(mockEventChild).get();
    }

    @Test
    void editEvent_Failure() {
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Event.class)).thenReturn(null);

        repo.editEvent("eventId", "Updated Concert", "2026-04-02 21:00", "Music", "Montreal", 150);

        verify(mockEventChild).get();
    }

    @Test
    void cancelEvent_Success() {
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Event.class)).thenReturn(new Event());

        repo.cancelEvent("eventId");

        verify(mockEventChild).get();
    }

    @Test
    void cancelEvent_Failure() {
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Event.class)).thenReturn(null);

        repo.cancelEvent("eventId");

        verify(mockEventChild).get();
    }

    @Test
    void listEvents_Success() {
        EventRepository.EventListCallback callback = mock(EventRepository.EventListCallback.class);
        repo.listEvents(callback);

        verify(mockDb).child("events");
    }
}