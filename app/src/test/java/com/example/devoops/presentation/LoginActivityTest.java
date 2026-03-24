package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class LoginActivityTest {

    @Test
    void givenLoginActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        // Given / When / Then
        assertTrue(AppCompatActivity.class.isAssignableFrom(LoginActivity.class));
    }

    @Test
    void givenLoginActivityClass_whenInspectingMethods_thenContainsPrimeUiPaths() throws Exception {
        // Given / When
        Method onCreate = LoginActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);
        Method initViews = LoginActivity.class.getDeclaredMethod("initViews");

        // Then
        assertNotNull(onCreate);
        assertNotNull(initViews);
    }

    @Test
    void givenLoginActivityClass_whenInspectingFields_thenContainsViewModelAndInputs() throws Exception {
        // Given / When
        Field viewModel = LoginActivity.class.getDeclaredField("viewModel");
        Field etEmail = LoginActivity.class.getDeclaredField("etEmail");
        Field etPassword = LoginActivity.class.getDeclaredField("etPassword");
        Field btnLoginEmail = LoginActivity.class.getDeclaredField("btnLoginEmail");

        // Then
        assertNotNull(viewModel);
        assertNotNull(etEmail);
        assertNotNull(etPassword);
        assertNotNull(btnLoginEmail);
    }

    @Test
    void givenLoginActivityClass_whenInspectingName_thenMatchesExpected() {
        // Given / When / Then
        assertEquals("com.example.devoops.presentation.LoginActivity", LoginActivity.class.getName());
    }
}

