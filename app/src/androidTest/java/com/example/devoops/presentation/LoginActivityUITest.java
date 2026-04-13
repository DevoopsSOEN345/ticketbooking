package com.example.devoops.presentation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertTrue;


import android.util.Log;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import com.example.devoops.R;
import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class LoginActivityUITest {

    private static FirebaseAuth auth;
    private static FirebaseDatabase database;
    private static boolean isSeeded = false;

//    @BeforeClass // Runs ONCE before all tests
//    public static void setupClass() throws ExecutionException, InterruptedException {
//        auth = FirebaseAuth.getInstance();
//        database = FirebaseDatabase.getInstance();
//
//        try {
//            auth.useEmulator("10.0.2.2", 9099);
//            database.useEmulator("10.0.2.2", 9000);
//        } catch (IllegalStateException e) {
//            // Already configured
//        }
//
//        // Only seed if we haven't already
//        if (!isSeeded) {
//            seedAdminUser("jonny@gmail.com", "password");
//            isSeeded = true;
//        }
//    }

    @Test
    public void testE2E_WelcomeToSignupFlow() {
        ActivityScenario.launch(WelcomeActivity.class);

        onView(withId(R.id.btnGoToSignup)).perform(click());

        onView(withId(R.id.etName)).check(matches(isDisplayed()));

        String email = "e2e_welcome_" + System.currentTimeMillis() + "@test.com";
        onView(withId(R.id.etName)).perform(typeText("Jonathan E2E"), closeSoftKeyboard());
        onView(withId(R.id.etSignupEmail)).perform(typeText(email), closeSoftKeyboard());
        onView(withId(R.id.etSignupPassword)).perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btnSignupEmail)).perform(click());

        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        onView(withId(R.id.btnGoToSignup)).check(matches(isDisplayed()));
    }

    @Test
    public void testE2E_WelcomeToLogin_AdminPath() {
        ActivityScenario.launch(WelcomeActivity.class);
        onView(withId(R.id.btnGoToLogin)).perform(click());

        // Enter Admin Credentials
        onView(withId(R.id.etEmail)).perform(typeText("jonny@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("password"), closeSoftKeyboard());

        // Click Login
        onView(withId(R.id.btnLoginEmail)).perform(click());
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // WAIT for the transition to complete (up to 5 seconds)
        waitForView(withId(R.id.addBtn), 5000);

        // Now this will pass
        onView(withId(R.id.addBtn)).check(matches(isDisplayed()));
    }
    private static void seedAdminUser(String email, String password) throws ExecutionException, InterruptedException {
        AuthResult result = Tasks.await(auth.createUserWithEmailAndPassword(email, password));
        String uid = result.getUser().getUid();
        DatabaseReference dbRef = database.getReference("users").child(uid);

        User adminUser = new User(uid, "Admin Jonathan", email, "514-000-0000", UserRole.ADMIN);

        Tasks.await(dbRef.setValue(adminUser));
        auth.signOut();
    }

    public void waitForView(final org.hamcrest.Matcher<View> viewMatcher, final long timeout) {
        final long endTime = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < endTime) {
            try {
                onView(viewMatcher).check(matches(isDisplayed()));
                return; // Found it!
            } catch (Exception e) {
                // Not found yet, keep waiting
            }
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        }
        // Final check which will throw the actual error if it still fails
        onView(viewMatcher).check(matches(isDisplayed()));
    }

}
