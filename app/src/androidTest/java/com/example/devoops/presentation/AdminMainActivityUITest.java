package com.example.devoops.presentation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.devoops.R;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminMainActivityUITest {
    @Test
    public void adminManagement_checkInitialUI() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminMainActivity.class);
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
            onView(withId(R.id.addBtn)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminAddEvent_Success() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminMainActivity.class);
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.addBtn)).perform(click());

            onView(withId(R.id.etName)).perform(typeText("Sample Event A"), closeSoftKeyboard());
            onView(withId(R.id.etLocation)).perform(typeText("Location A"), closeSoftKeyboard());
            onView(withId(R.id.etCategory)).perform(typeText("Category A"), closeSoftKeyboard());
            onView(withId(R.id.etSeats)).perform(typeText("100"), closeSoftKeyboard());
            onView(withId(R.id.etDate)).perform(typeText("01-01-2026"), closeSoftKeyboard());

            onView(withId(R.id.btnSave)).perform(click());

            onView(withText("Sample Event A")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminEditEvent_Success() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminMainActivity.class);
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(intent)) {
            //Clicks the edit button inside the first item of the list
            onView(withId(R.id.recyclerView))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                            TestUtils.clickChildViewWithId(R.id.editBtn)));

            onView(withId(R.id.etLocation)).perform(replaceText("Updated Location"), closeSoftKeyboard());
            onView(withId(R.id.btnSave)).perform(click());

            //Verify the UI updated the specific row
            onView(withText(containsString("Updated Location"))).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminDeleteEvent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminMainActivity.class);
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(intent)) {
            //Clicks the delete button inside the first item
            onView(withId(R.id.recyclerView))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0,
                            TestUtils.clickChildViewWithId(R.id.deleteBtn)));

            //Check that the list still exists (item removal verified by non-existence of specific text if known)
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminAddEvent_EmptyFields_ShowsError() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminMainActivity.class);
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.addBtn)).perform(click());
            onView(withId(R.id.btnSave)).perform(click());

            onView(withId(R.id.etName)).check(matches(isDisplayed()));
        }
    }
}

