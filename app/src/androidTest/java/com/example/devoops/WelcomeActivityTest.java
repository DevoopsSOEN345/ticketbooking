package com.example.devoops.presentation;

import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.devoops.R;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation tests for WelcomeActivity
 */
public class WelcomeActivityTest {

    @Rule
    public ActivityScenarioRule<WelcomeActivity> activityRule =
            new ActivityScenarioRule<>(WelcomeActivity.class);

    @Test
    public void testActivityLaunches() {
        activityRule.getScenario().onActivity(activity -> {
            assertNotNull(activity);
        });
    }

    @Test
    public void testActivityIsNotNull() {
        assertNotNull(activityRule.getScenario());
    }
}

