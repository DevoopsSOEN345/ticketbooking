package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class EventAdapterTest {

    @Test
    void givenEventAdapterClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        // Given / When
        Method onCreateViewHolder = EventAdapter.class.getDeclaredMethod(
                "onCreateViewHolder", android.view.ViewGroup.class, int.class);
        Method onBindViewHolder = EventAdapter.class.getDeclaredMethod(
                "onBindViewHolder", EventAdapter.ViewHolder.class, int.class);
        Method getItemCount = EventAdapter.class.getDeclaredMethod("getItemCount");
        Method setEvents = EventAdapter.class.getDeclaredMethod("setEvents", java.util.List.class);

        // Then
        assertNotNull(onCreateViewHolder);
        assertNotNull(onBindViewHolder);
        assertNotNull(getItemCount);
        assertNotNull(setEvents);
    }

    @Test
    void givenEventAdapterClass_whenInspectingFields_thenContainsEventListAndListener() throws Exception {
        // Given / When
        Field events = EventAdapter.class.getDeclaredField("events");
        Field listener = EventAdapter.class.getDeclaredField("listener");
        Field isAdmin = EventAdapter.class.getDeclaredField("isAdmin");

        // Then
        assertNotNull(events);
        assertNotNull(listener);
        assertNotNull(isAdmin);
    }
}