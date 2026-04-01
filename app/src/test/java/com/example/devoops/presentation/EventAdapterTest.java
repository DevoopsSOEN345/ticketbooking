package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.devoops.R;
import com.example.devoops.models.Event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class EventAdapterTest {

    @Mock private EventAdapter.OnEventClickListener mockListener;
    @Mock private ViewGroup mockParent;
    @Mock private Context mockContext;
    @Mock private View mockItemView;
    @Mock private TextView mockTitle, mockDate, mockCategory, mockLocation, mockTotalSeats, mockAvailableSeats;
    @Mock private Button mockEditBtn, mockDeleteBtn;

    private void stubItemViewFinds() {
        lenient().when(mockItemView.findViewById(R.id.title)).thenReturn(mockTitle);
        lenient().when(mockItemView.findViewById(R.id.date)).thenReturn(mockDate);
        lenient().when(mockItemView.findViewById(R.id.category)).thenReturn(mockCategory);
        lenient().when(mockItemView.findViewById(R.id.location)).thenReturn(mockLocation);
        lenient().when(mockItemView.findViewById(R.id.totalSeats)).thenReturn(mockTotalSeats);
        lenient().when(mockItemView.findViewById(R.id.availableSeats)).thenReturn(mockAvailableSeats);
        lenient().when(mockItemView.findViewById(R.id.editBtn)).thenReturn(mockEditBtn);
        lenient().when(mockItemView.findViewById(R.id.deleteBtn)).thenReturn(mockDeleteBtn);
    }

    private EventAdapter createAdapterWithEvents(boolean isAdmin, List<Event> events) {
        EventAdapter adapter = spy(new EventAdapter(isAdmin, mockListener));
        doNothing().when(adapter).notifyDataSetChanged();
        adapter.setEvents(events);
        return adapter;
    }

    @Test
    void constructor_setsAdminAndListener_primePath13() {
        EventAdapter adapter = new EventAdapter(true, mockListener);
        assertNotNull(adapter);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    void setEvents_updatesListAndCount_primePath14() {
        List<Event> events = Arrays.asList(
                new Event("1", "Concert", "2026", "Music", "MTL", 100),
                new Event("2", "Game", "2026", "Sports", "TOR", 200)
        );
        EventAdapter adapter = createAdapterWithEvents(false, events);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    void onCreateViewHolder_returnsViewHolder_primePath15() {
        EventAdapter adapter = new EventAdapter(true, mockListener);
        when(mockParent.getContext()).thenReturn(mockContext);

        stubItemViewFinds();

        try (MockedStatic<LayoutInflater> liMock = mockStatic(LayoutInflater.class)) {
            LayoutInflater mockInflater = mock(LayoutInflater.class);
            liMock.when(() -> LayoutInflater.from(mockContext)).thenReturn(mockInflater);
            when(mockInflater.inflate(R.layout.item_event, mockParent, false)).thenReturn(mockItemView);

            EventAdapter.ViewHolder holder = adapter.onCreateViewHolder(mockParent, 0);
            assertNotNull(holder);
        }
    }

    @Test
    void onBindViewHolder_admin_showsButtonsAndBindsData_primePath16() {
        Event event = new Event("1", "Concert", "2026", "Music", "MTL", 100);
        event.setOpenSeats(50);
        EventAdapter adapter = createAdapterWithEvents(true, List.of(event));

        stubItemViewFinds();
        EventAdapter.ViewHolder holder = adapter.new ViewHolder(mockItemView);

        adapter.onBindViewHolder(holder, 0);

        verify(mockTitle).setText("Concert");
        verify(mockDate).setText("Date: 2026");
        verify(mockCategory).setText("Music");
        verify(mockLocation).setText("📍 MTL");
        verify(mockTotalSeats).setText("Total Seats: 100");
        verify(mockAvailableSeats).setText("Available: 50");

        verify(mockEditBtn).setVisibility(View.VISIBLE);
        verify(mockDeleteBtn).setVisibility(View.VISIBLE);

        ArgumentCaptor<View.OnClickListener> editCaptor =
                ArgumentCaptor.forClass(View.OnClickListener.class);
        verify(mockEditBtn).setOnClickListener(editCaptor.capture());
        editCaptor.getValue().onClick(mockEditBtn);
        verify(mockListener).onEdit(event);

        ArgumentCaptor<View.OnClickListener> deleteCaptor =
                ArgumentCaptor.forClass(View.OnClickListener.class);
        verify(mockDeleteBtn).setOnClickListener(deleteCaptor.capture());
        deleteCaptor.getValue().onClick(mockDeleteBtn);
        verify(mockListener).onDelete(event);
    }

    @Test
    void onBindViewHolder_notAdmin_hidesButtons_primePath17() {
        Event event = new Event("1", "Concert", "2026", "Music", "MTL", 100);
        event.setOpenSeats(50);
        EventAdapter adapter = createAdapterWithEvents(false, List.of(event));

        stubItemViewFinds();
        EventAdapter.ViewHolder holder = adapter.new ViewHolder(mockItemView);

        adapter.onBindViewHolder(holder, 0);

        verify(mockEditBtn).setVisibility(View.GONE);
        verify(mockDeleteBtn).setVisibility(View.GONE);
    }

    @Test
    void givenEventAdapterClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        Method onCreateViewHolder = EventAdapter.class.getDeclaredMethod(
                "onCreateViewHolder", android.view.ViewGroup.class, int.class);
        Method onBindViewHolder = EventAdapter.class.getDeclaredMethod(
                "onBindViewHolder", EventAdapter.ViewHolder.class, int.class);
        Method getItemCount = EventAdapter.class.getDeclaredMethod("getItemCount");
        Method setEvents = EventAdapter.class.getDeclaredMethod("setEvents", java.util.List.class);

        assertNotNull(onCreateViewHolder);
        assertNotNull(onBindViewHolder);
        assertNotNull(getItemCount);
        assertNotNull(setEvents);
    }

    @Test
    void givenEventAdapterClass_whenInspectingFields_thenContainsEventListAndListener() throws Exception {
        Field events = EventAdapter.class.getDeclaredField("events");
        Field listener = EventAdapter.class.getDeclaredField("listener");
        Field isAdmin = EventAdapter.class.getDeclaredField("isAdmin");

        assertNotNull(events);
        assertNotNull(listener);
        assertNotNull(isAdmin);
    }
}