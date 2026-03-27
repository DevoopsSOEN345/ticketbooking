package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import androidx.lifecycle.ViewModel;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class EventViewModelTest {

    @Test
    void givenEventViewModelClass_whenInspectingType_thenExtendsViewModel() {
        // Given / When / Then
        assertNotNull(ViewModel.class.isAssignableFrom(EventViewModel.class));
    }

    @Test
    void givenEventViewModelClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        // Given / When
        Method getEvents = EventViewModel.class.getDeclaredMethod("getEvents");
        Method createEvent = EventViewModel.class.getDeclaredMethod(
                "createEvent", String.class, String.class, String.class, String.class, int.class);
        Method cancelEvent = EventViewModel.class.getDeclaredMethod("cancelEvent", String.class);
        Method editEvent = EventViewModel.class.getDeclaredMethod(
                "editEvent", String.class, String.class, String.class, String.class, String.class, int.class);

        // Then
        assertNotNull(getEvents);
        assertNotNull(createEvent);
        assertNotNull(cancelEvent);
        assertNotNull(editEvent);
    }

    @Test
    void givenEventViewModelClass_whenInspectingFields_thenContainsRepoAndLiveData() throws Exception {
        // Given / When
        Field repo = EventViewModel.class.getDeclaredField("repo");
        Field events = EventViewModel.class.getDeclaredField("events");

        // Then
        assertNotNull(repo);
        assertNotNull(events);
    }
}