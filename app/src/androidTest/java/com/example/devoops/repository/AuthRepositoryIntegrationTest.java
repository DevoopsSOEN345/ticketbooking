package com.example.devoops.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.example.devoops.models.User;
import com.example.devoops.models.UserRole;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class AuthRepositoryIntegrationTest {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private AuthRepository authRepo;
    private UserRepository userRepo;

    @Before
    public void setup() {
        auth = FirebaseAuth.getInstance();
        try {
            auth.useEmulator("10.0.2.2", 9099);
        } catch (IllegalStateException e) {
            // Emulator already configured
        }

        database = FirebaseDatabase.getInstance();
        try {
            database.useEmulator("10.0.2.2", 9000);
        } catch (IllegalStateException e) {
            // Emulator already configured
        }

        authRepo = new AuthRepository(auth);
        database.getReference().setValue(null);
        userRepo = new UserRepository(database.getReference());
    }

    @Test
    public void signupEmail_FullIntegration_AuthAndDatabase() throws InterruptedException {
        String email = "test_" + System.currentTimeMillis() + "@concordia.ca";
        String password = "password123";
        String name = "Jonathan Test";
        String phone = "514-000-0000";

        CountDownLatch authLatch = new CountDownLatch(1);
        final String[] resultUid = new String[1];

        // Create authetincation user
        authRepo.signupEmail(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                resultUid[0] = uid;
                authLatch.countDown();
            }

            @Override
            public void onError(String error) {
                fail("Auth Signup failed: " + error);
                authLatch.countDown();
            }
        });

        assertTrue("Auth timed out", authLatch.await(5, TimeUnit.SECONDS));
        String uid = resultUid[0];

        // Create user for the db
        userRepo.createUser(uid, email, phone, name);

        CountDownLatch verifyLatch = new CountDownLatch(1);
        final User[] savedUser = new User[1];

        database.getReference("users").child(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        savedUser[0] = snapshot.getValue(User.class);
                    }
                    verifyLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    fail("Database fetch failed: " + e.getMessage());
                    verifyLatch.countDown();
                });

        assertTrue("Verification timed out - Database might not have saved in time",
                verifyLatch.await(5, TimeUnit.SECONDS));

        assertNotNull("User object should not be null in DB", savedUser[0]);
        assertEquals("Name match", name, savedUser[0].getName());
        assertEquals("Email match", email, savedUser[0].getEmail());
        assertEquals("Role match", UserRole.CUSTOMER, savedUser[0].getRole());
    }

    @Test
    public void signupEmail_Integration_DuplicateUser() throws InterruptedException, ExecutionException {
        String uniqueEmail = "duplicate_" + System.currentTimeMillis() + "@test.com";
        String password = "password123";

        Tasks.await(auth.createUserWithEmailAndPassword(uniqueEmail, password));
        auth.signOut();

        CountDownLatch latch = new CountDownLatch(1);
        final String[] errorMsg = new String[1];

        authRepo.signupEmail(uniqueEmail, "differentPassword", new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                fail("Signup should have failed for duplicate email");
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                errorMsg[0] = error;
                latch.countDown();
            }
        });

        boolean completed = latch.await(5, TimeUnit.SECONDS);

        assertTrue("Test timed out waiting for Firebase response", completed);
        assertNotNull("Error message should not be null", errorMsg[0]);
        assertTrue("Error message should mention the email is in use. Actual: " + errorMsg[0],
                errorMsg[0].contains("already in use"));
    }

    @Test
    public void signOut_Integration_ClearsSession() throws InterruptedException, ExecutionException {
        // 1. Log in a user first
        Tasks.await(auth.signInAnonymously());
        assertNotNull("Should be logged in", auth.getCurrentUser());

        authRepo.signOut();

        assertNull("Firebase session should be null after signOut", auth.getCurrentUser());
    }

    @Test
    public void loginEmail_Integration_InvalidPassword() throws InterruptedException, ExecutionException {
        String email = "existing_user@concordia.ca";
        String wrongPassword = "wrongPassword";

        Tasks.await(auth.createUserWithEmailAndPassword(email, "correctPassword"));
        auth.signOut();

        CountDownLatch latch = new CountDownLatch(1);
        final String[] errorMsg = new String[1];

        authRepo.loginEmail(email, wrongPassword, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(String uid) {
                fail("Login should have failed with wrong password");
                latch.countDown();
            }

            @Override
            public void onError(String error) {
                errorMsg[0] = error;
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        assertNotNull("Error message should be returned", errorMsg[0]);
        assertTrue(errorMsg[0].contains("password"));
    }
}
