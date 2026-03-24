package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class SignupActivityTest {

    @Test
    void givenSignupActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        // Given / When / Then
        assertTrue(AppCompatActivity.class.isAssignableFrom(SignupActivity.class));
    }

    @Test
    void givenSignupActivityClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        // Given / When
        Method onCreate = SignupActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);
        Method initViews = SignupActivity.class.getDeclaredMethod("initViews");

        // Then
        assertNotNull(onCreate);
        assertNotNull(initViews);
    }

    @Test
    void givenSignupActivityClass_whenInspectingFields_thenContainsSignupInputs() throws Exception {
        // Given / When
        Field etEmail = SignupActivity.class.getDeclaredField("etEmail");
        Field etPassword = SignupActivity.class.getDeclaredField("etPassword");
        Field etName = SignupActivity.class.getDeclaredField("etName");
        Field btnEmailSignup = SignupActivity.class.getDeclaredField("btnEmailSignup");

        // Then
        assertNotNull(etEmail);
        assertNotNull(etPassword);
        assertNotNull(etName);
        assertNotNull(btnEmailSignup);
    }

    @Test
    void givenSignupActivityClass_whenInspectingName_thenMatchesExpected() {
        // Given / When / Then
        assertEquals("com.example.devoops.presentation.SignupActivity", SignupActivity.class.getName());
    }
}

