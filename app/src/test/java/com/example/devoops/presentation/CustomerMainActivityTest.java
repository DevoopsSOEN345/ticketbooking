package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import androidx.appcompat.app.AppCompatActivity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class CustomerMainActivityTest {

    @Test
    void givenCustomerMainActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        // Given / When / Then
        assertEquals(AppCompatActivity.class, CustomerMainActivity.class.getSuperclass());
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingMethods_thenContainsPrimePaths() throws Exception {
        // Given / When
        Method onCreate = CustomerMainActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);

        // Then
        assertNotNull(onCreate);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingFields_thenContainsViewModelAndAdapter() throws Exception {
        // Given / When
        Field viewModel = CustomerMainActivity.class.getDeclaredField("viewModel");
        Field adapter = CustomerMainActivity.class.getDeclaredField("adapter");

        // Then
        assertNotNull(viewModel);
        assertNotNull(adapter);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingName_thenMatchesExpected() {
        // Given / When / Then
        assertEquals("com.example.devoops.presentation.CustomerMainActivity", CustomerMainActivity.class.getName());
    }
}