package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

class WelcomeActivityTest {

    @Test
    void givenWelcomeActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        // Given / When / Then
        assertTrue(AppCompatActivity.class.isAssignableFrom(WelcomeActivity.class));
    }

    @Test
    void givenWelcomeActivityClass_whenInspectingMethods_thenContainsOnCreatePrimePath() throws Exception {
        // Given / When
        Method onCreate = WelcomeActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);

        // Then
        assertNotNull(onCreate);
    }

    @Test
    void givenWelcomeActivityClass_whenInspectingName_thenMatchesExpected() {
        // Given / When / Then
        assertEquals("com.example.devoops.presentation.WelcomeActivity", WelcomeActivity.class.getName());
    }
}

