package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import androidx.appcompat.app.AppCompatActivity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class AdminMainActivityTest {

    @Test
    void givenAdminMainActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        // Given / When / Then
        assertEquals(AppCompatActivity.class, AdminMainActivity.class.getSuperclass());
    }

    @Test
    void givenAdminMainActivityClass_whenInspectingMethods_thenContainsPrimePaths() throws Exception {
        // Given / When
        Method onCreate = AdminMainActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);
        Method createNewEvent = AdminMainActivity.class.getDeclaredMethod("createNewEvent", String.class, String.class, String.class, String.class, int.class);
        Method editEvent = AdminMainActivity.class.getDeclaredMethod("editEvent", String.class, String.class, String.class, String.class, String.class, int.class);

        // Then
        assertNotNull(onCreate);
        assertNotNull(createNewEvent);
        assertNotNull(editEvent);
    }

    @Test
    void givenAdminMainActivityClass_whenInspectingFields_thenContainsViewModelAndAdapter() throws Exception {
        // Given / When
        Field viewModel = AdminMainActivity.class.getDeclaredField("viewModel");
        Field adapter = AdminMainActivity.class.getDeclaredField("adapter");

        // Then
        assertNotNull(viewModel);
        assertNotNull(adapter);
    }

    @Test
    void givenAdminMainActivityClass_whenInspectingName_thenMatchesExpected() {
        // Given / When / Then
        assertEquals("com.example.devoops.presentation.AdminMainActivity", AdminMainActivity.class.getName());
    }
}