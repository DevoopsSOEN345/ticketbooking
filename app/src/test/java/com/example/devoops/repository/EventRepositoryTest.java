package com.example.devoops.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.devoops.models.Event;
import com.example.devoops.models.EventStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import androidx.annotation.NonNull;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EventRepositoryTest {

    private EventRepository repo;

    @Mock private DatabaseReference mockDb;
    @Mock private DatabaseReference mockEventsRef;
    @Mock private DatabaseReference mockEventRef;

    @Mock private Task<Void> mockWriteTask;
    @Mock private Task<DataSnapshot> mockGetTask;
    @Mock private DataSnapshot mockSnapshot;

    @BeforeEach
    void setup() {
        lenient().when(mockDb.child("events")).thenReturn(mockEventsRef);
        lenient().when(mockEventsRef.child(anyString())).thenReturn(mockEventRef);

        lenient().when(mockEventRef.setValue(any())).thenReturn(mockWriteTask);
        lenient().when(mockWriteTask.addOnSuccessListener(any())).thenReturn(mockWriteTask);
        lenient().when(mockWriteTask.addOnFailureListener(any())).thenReturn(mockWriteTask);

        lenient().when(mockEventRef.get()).thenReturn(mockGetTask);
        lenient().when(mockGetTask.addOnSuccessListener(any())).thenReturn(mockGetTask);
        lenient().when(mockGetTask.addOnFailureListener(any())).thenReturn(mockGetTask);

        repo = new EventRepository(mockDb);
    }

    @Test
    void createEvent_success_primePath1() {
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {

            repo.createEvent("Concert", "2026", "Music", "MTL", 100);

            verify(mockEventRef).setValue(any(Event.class));

            ArgumentCaptor<OnSuccessListener<Void>> captor =
                    ArgumentCaptor.forClass(OnSuccessListener.class);

            verify(mockWriteTask).addOnSuccessListener(captor.capture());
            captor.getValue().onSuccess(null);

            logMock.verify(() -> Log.d("RTDB", "Event saved!"));
        }
    }

    @Test
    void createEvent_failure_primePath2() {
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {

            repo.createEvent("Concert", "2026", "Music", "MTL", 100);

            ArgumentCaptor<OnFailureListener> captor =
                    ArgumentCaptor.forClass(OnFailureListener.class);

            verify(mockWriteTask).addOnFailureListener(captor.capture());

            Exception ex = new Exception("fail");
            captor.getValue().onFailure(ex);

            logMock.verify(() -> Log.e(eq("RTDB"), eq("Save failed"), eq(ex)));
        }
    }

    @Test
    void editEvent_success_updatesFieldsAndSeats_primePath3() {
        Event event = new Event("id", "Old", "2026", "Music", "MTL", 100);
        event.setOpenSeats(80);

        when(mockSnapshot.getValue(Event.class)).thenReturn(event);

        repo.editEvent("id", "New", "2026", "Music", "MTL", 120);

        ArgumentCaptor<OnSuccessListener<DataSnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(mockGetTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(mockSnapshot);

        assertEquals("New", event.getName());
        assertEquals(120, event.getTotalSeats());
        assertEquals(100, event.getOpenSeats()); // 80 + 40

        verify(mockEventRef).setValue(event);
    }

    @Test
    void editEvent_eventNull_noUpdate_primePath4() {
        when(mockSnapshot.getValue(Event.class)).thenReturn(null);

        repo.editEvent("id", "New", "2026", "Music", "MTL", 120);

        ArgumentCaptor<OnSuccessListener<DataSnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(mockGetTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(mockSnapshot);

        verify(mockEventRef, never()).setValue(any());
    }

    @Test
    void cancelEvent_success_setsCancelled_primePath5() {
        Event event = new Event();
        when(mockSnapshot.getValue(Event.class)).thenReturn(event);

        repo.cancelEvent("id");

        ArgumentCaptor<OnSuccessListener<DataSnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(mockGetTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(mockSnapshot);

        assertEquals(EventStatus.CANCELLED, event.getEventStatus());
        verify(mockEventRef).setValue(event);
    }

    @Test
    void cancelEvent_eventNull_noUpdate_primePath6() {
        when(mockSnapshot.getValue(Event.class)).thenReturn(null);

        repo.cancelEvent("id");

        ArgumentCaptor<OnSuccessListener<DataSnapshot>> captor =
                ArgumentCaptor.forClass(OnSuccessListener.class);

        verify(mockGetTask).addOnSuccessListener(captor.capture());
        captor.getValue().onSuccess(mockSnapshot);

        verify(mockEventRef, never()).setValue(any());
    }

    @Test
    void getEvents_returnsLiveData_primePath7() {
        LiveData<List<Event>> result = repo.getEvents();
        assertNotNull(result);
    }

    @Test
    void defaultConstructor_notNull_primePath8() {
        EventRepository defaultRepo = new EventRepository();
        assertNotNull(defaultRepo);
    }
    @Test
void getEvents_onDataChange_populatesLiveData_primePath9() {
    try (MockedStatic<Log> logMock = mockStatic(Log.class)) {

        // Allow LiveData.setValue to work without Android main thread
        androidx.arch.core.executor.ArchTaskExecutor.getInstance()
            .setDelegate(new androidx.arch.core.executor.TaskExecutor() {
                @Override
                public void executeOnDiskIO(@NonNull Runnable runnable) { runnable.run(); }
                @Override
                public void postToMainThread(@NonNull Runnable runnable) { runnable.run(); }
                @Override
                public boolean isMainThread() { return true; }
            });

        DataSnapshot childSnap = mock(DataSnapshot.class);
        Event event = new Event("id1", "Concert", "2026", "Music", "MTL", 100);
        when(childSnap.getValue(Event.class)).thenReturn(event);
        when(childSnap.getKey()).thenReturn("id1");

        when(mockSnapshot.getChildren()).thenReturn(List.of(childSnap));

        ArgumentCaptor<ValueEventListener> captor =
                ArgumentCaptor.forClass(ValueEventListener.class);

        LiveData<List<Event>> result = repo.getEvents();

        verify(mockEventsRef).addValueEventListener(captor.capture());
        captor.getValue().onDataChange(mockSnapshot);

        List<Event> events = result.getValue();
        assertNotNull(events);
        assertEquals(1, events.size());
        assertEquals("id1", events.get(0).getEventId());

        logMock.verify(() -> Log.d("REPO_DEBUG", "Fetched 1 events from Firebase"));

        // Clean up
        androidx.arch.core.executor.ArchTaskExecutor.getInstance().setDelegate(null);
    }
}
        

    @Test
    void getEvents_onCancelled_logsError_primePath10() {
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {

            DatabaseError mockError = mock(DatabaseError.class);
            when(mockError.getMessage()).thenReturn("Permission denied");

            ArgumentCaptor<ValueEventListener> captor =
                    ArgumentCaptor.forClass(ValueEventListener.class);

            repo.getEvents();

            verify(mockEventsRef).addValueEventListener(captor.capture());
            captor.getValue().onCancelled(mockError);

            logMock.verify(() -> Log.e("REPO_DEBUG", "Read failed: Permission denied"));
        }
    }

    @Test
    void reserveSeats_returnsFalse_primePath11() {
        boolean result = repo.reserveSeats("id", 2);
        assertFalse(result);
    }

    @Test
    void cancelSeats_returnsFalse_primePath12() {
        boolean result = repo.cancelSeats("id", 2);
        assertFalse(result);
    }
}