package com.example.devoops;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.devoops.models.Event;
import com.example.devoops.presentation.CustomerMainActivity;
import com.example.devoops.presentation.EventAdapter;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class CustomerMainActivityUITest {

    @Rule
    public ActivityScenarioRule<CustomerMainActivity> activityRule =
            new ActivityScenarioRule<>(CustomerMainActivity.class);

    private void injectTestEvent() {
        Event testEvent = new Event("id1", "Test Event", "2025-01-01", "Tech", "Updated Location", 100);
        activityRule.getScenario().onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerView);
            EventAdapter adapter = (EventAdapter) recyclerView.getAdapter();
            if (adapter != null) {
                adapter.setEvents(Collections.singletonList(testEvent));
            }
        });
    }

    @Test
    public void testCustomerBrowse() {
        injectTestEvent();
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText(containsString("Updated Location")))));
    }

    @Test
    public void testCustomer_AdminButtonsAreHidden() {
        injectTestEvent();
        onView(withId(R.id.editBtn))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
        onView(withId(R.id.deleteBtn))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));
    }
}
