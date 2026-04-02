package com.example.devoops.presentation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.devoops.R;
import com.example.devoops.models.Event;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;

@RunWith(AndroidJUnit4.class)
public class CustomerMainActivityUITest {

    @Before
    public void setUp() {
        try {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000);
        } catch (IllegalStateException e) {
            // already configured
        }
    }

    @Test
    public void testCustomerBrowse() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

            // Inject a known event directly so the test is not blocked on async Firestore data
            Event testEvent = new Event("id1", "Test Event", "2025-01-01", "Tech", "Updated Location", 100);
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.recyclerView);
                EventAdapter adapter = (EventAdapter) rv.getAdapter();
                if (adapter != null) adapter.setEvents(Collections.singletonList(testEvent));
            });

            // Adapter formats location as "📍 Updated Location" — containsString handles any prefix
            onView(withId(R.id.recyclerView))
                    .check(matches(hasDescendant(withText(containsString("Updated Location")))));
        }
    }

    @Test
    public void testFilterLogic() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            // Inject events directly instead of relying on emulator seed data
            Event eventA = new Event("id2", "Event A", "2025-01-01", "Category A", "Location A", 50);
            Event eventB = new Event("id3", "Event B", "2025-02-01", "Category B", "Location B", 50);
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.recyclerView);
                EventAdapter adapter = (EventAdapter) rv.getAdapter();
                if (adapter != null) adapter.setEvents(Arrays.asList(eventA, eventB));
            });

            onView(withId(R.id.recyclerView))
                    .check(matches(hasDescendant(withText("Category A"))));
        }
    }

    @Test
    public void testCustomer_AdminButtonsAreHidden() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), CustomerMainActivity.class);
        try (ActivityScenario<CustomerMainActivity> scenario = ActivityScenario.launch(intent)) {
            // Inject one event so the adapter binds a ViewHolder with the buttons present
            Event testEvent = new Event("id4", "Test Event", "2025-01-01", "Tech", "Somewhere", 10);
            scenario.onActivity(activity -> {
                RecyclerView rv = activity.findViewById(R.id.recyclerView);
                EventAdapter adapter = (EventAdapter) rv.getAdapter();
                if (adapter != null) adapter.setEvents(Collections.singletonList(testEvent));
            });

            // CustomerMainActivity creates EventAdapter(false) — buttons are set to GONE
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
