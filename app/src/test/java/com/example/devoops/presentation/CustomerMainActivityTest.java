package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import androidx.appcompat.app.AppCompatActivity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class CustomerMainActivityTest {

    // ========================
    // Existing structural tests
    // ========================

    @Test
    void givenCustomerMainActivityClass_whenInspectingType_thenExtendsAppCompatActivity() {
        assertEquals(AppCompatActivity.class, CustomerMainActivity.class.getSuperclass());
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingMethods_thenContainsPrimePaths() throws Exception {
        Method onCreate = CustomerMainActivity.class.getDeclaredMethod("onCreate", android.os.Bundle.class);

        assertNotNull(onCreate);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingFields_thenContainsViewModelAndAdapter() throws Exception {
        Field viewModel = CustomerMainActivity.class.getDeclaredField("viewModel");
        Field adapter = CustomerMainActivity.class.getDeclaredField("adapter");

        assertNotNull(viewModel);
        assertNotNull(adapter);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingName_thenMatchesExpected() {
        assertEquals("com.example.devoops.presentation.CustomerMainActivity", CustomerMainActivity.class.getName());
    }

    // ========================
    // New tests for search & filter feature
    // ========================

    @Test
    void givenCustomerMainActivityClass_whenInspectingFields_thenContainsActiveFiltersTextView() throws Exception {
        Field tvActiveFilters = CustomerMainActivity.class.getDeclaredField("tvActiveFilters");

        assertNotNull(tvActiveFilters);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingFields_thenContainsFilterStateStrings() throws Exception {
        Field currentFilterDate = CustomerMainActivity.class.getDeclaredField("currentFilterDate");
        Field currentFilterLocation = CustomerMainActivity.class.getDeclaredField("currentFilterLocation");
        Field currentFilterCategory = CustomerMainActivity.class.getDeclaredField("currentFilterCategory");

        assertNotNull(currentFilterDate);
        assertNotNull(currentFilterLocation);
        assertNotNull(currentFilterCategory);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingMethods_thenContainsShowFilterDialog() throws Exception {
        Method showFilterDialog = CustomerMainActivity.class.getDeclaredMethod("showFilterDialog");

        assertNotNull(showFilterDialog);
    }

    @Test
    void givenCustomerMainActivityClass_whenInspectingMethods_thenContainsUpdateActiveFiltersLabel() throws Exception {
        Method updateActiveFiltersLabel = CustomerMainActivity.class.getDeclaredMethod("updateActiveFiltersLabel");

        assertNotNull(updateActiveFiltersLabel);
    }
}
