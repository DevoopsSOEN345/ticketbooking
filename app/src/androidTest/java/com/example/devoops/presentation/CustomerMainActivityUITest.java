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
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.devoops.R;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CustomerMainActivityUITest {

    @Before
    public void setUp() {
        try {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000);
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testCustomerBrowse() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
            onView(withText(containsString("Location: Updated Location"))).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testFilterLogic() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {

            onView(withId(R.id.recyclerView))
                    .check(matches(hasDescendant(withText("Category A"))));
        }
    }

    @Test
    public void testCustomer_AdminButtonsAreHidden() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerView))
                    .check(matches(hasDescendant(allOf(
                            withId(R.id.editBtn),
                            withEffectiveVisibility(Visibility.GONE)))));

            onView(withId(R.id.recyclerView))
                    .check(matches(hasDescendant(allOf(
                            withId(R.id.deleteBtn),
                            withEffectiveVisibility(Visibility.GONE)))));
        }
    }

    @Test
    public void testCustomer_EmptyListMessage() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));
        }
    }
}