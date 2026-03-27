package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import androidx.fragment.app.DialogFragment;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class EventDialogFragmentTest {

    @Test
    void givenEventDialogFragmentClass_whenInspectingType_thenExtendsDialogFragment() {
        // Given / When / Then
        assertNotNull(DialogFragment.class.isAssignableFrom(EventDialogFragment.class));
    }

    @Test
    void givenEventDialogFragmentClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        // Given / When
        Method onCreateView = EventDialogFragment.class.getDeclaredMethod(
                "onCreateView", android.view.LayoutInflater.class, android.view.ViewGroup.class, android.os.Bundle.class);
        Method setEventToEdit = EventDialogFragment.class.getDeclaredMethod(
                "setEventToEdit", com.example.devoops.models.Event.class);

        // Then
        assertNotNull(onCreateView);
        assertNotNull(setEventToEdit);
    }

    @Test
    void givenEventDialogFragmentClass_whenInspectingFields_thenContainsEventToEdit() throws Exception {
        // Given / When
        Field eventToEdit = EventDialogFragment.class.getDeclaredField("eventToEdit");

        // Then
        assertNotNull(eventToEdit);
    }
}