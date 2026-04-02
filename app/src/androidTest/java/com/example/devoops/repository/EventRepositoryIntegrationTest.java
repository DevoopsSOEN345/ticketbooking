package com.example.devoops.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.devoops.models.Event;
import com.example.devoops.models.EventStatus;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class EventRepositoryIntegrationTest {

    private EventRepository repo;
    private FirebaseDatabase database;

    @Before
    public void setup() {
        database = FirebaseDatabase.getInstance();
        try {
            //Must match the port in firebase.json
            database.useEmulator("10.0.2.2", 9000);
        } catch (IllegalStateException e) {

        }
        repo = new EventRepository(database.getReference());
    }


      //Verifies that creating an event correctly persists data to Firebase
    @Test
    public void createEvent_validData_savesToDatabase() throws InterruptedException {
        String name = "CreateTest_" + System.currentTimeMillis();
        repo.createEvent(name, "2026-10-10", "Presentation Test", "MTL", 50);

        boolean saved = waitForEvent(name, e -> e != null);
        assertTrue("Event should be found in the database after creation", saved);
    }


     //Verifies that getEvents retrieves the live list from Firebase
    @Test
    public void getEvents_existingData_returnsPopulatedList() throws InterruptedException {
        String name = "BrowseTest_" + System.currentTimeMillis();
        repo.createEvent(name, "2026-11-11", "Browse Test", "MTL", 10);

        boolean retrieved = waitForEvent(name, e -> e != null);
        assertTrue("Repository should retrieve a list containing the newly created event", retrieved);
    }

     // Verifies that editing an event updates all relevant fields
    @Test
    public void editEvent_existingEvent_updatesFieldsCorrectly() throws InterruptedException {
        String name = "EditTest_" + System.currentTimeMillis();
        repo.createEvent(name, "2026-01-01", "Old Desc", "Old Loc", 10);

        Event event = getEventSync(name);
        repo.editEvent(event.getEventId(), "UpdatedName", "2026-02-02", "New Desc", "New Loc", 20);

        boolean updated = waitForEvent("UpdatedName", e -> e.getLocation().equals("New Loc"));
        assertTrue("Event fields in Firebase should match updated values", updated);
    }


     //Verifies that canceling an event properly transitions its status
    @Test
    public void cancelEvent_activeEvent_updatesStatusToCancelled() throws InterruptedException {
        String name = "CancelTest_" + System.currentTimeMillis();
        repo.createEvent(name, "2026-05-05", "Status Test", "Loc", 100);

        Event event = getEventSync(name);
        repo.cancelEvent(event.getEventId());

        boolean cancelled = waitForEvent(name, e -> e.getEventStatus() == EventStatus.CANCELLED);
        assertTrue("Event status should be CANCELLED in the database", cancelled);
    }


     //Tests idempotency by canceling an event twice
    @Test
    public void cancelEvent_alreadyCancelled_remainsCancelled() throws InterruptedException {
        String name = "DoubleCancel_" + System.currentTimeMillis();
        repo.createEvent(name, "2026", "Double Tap Test", "Loc", 10);

        Event event = getEventSync(name);
        repo.cancelEvent(event.getEventId());
        repo.cancelEvent(event.getEventId());

        boolean stillCancelled = waitForEvent(name, e -> e.getEventStatus() == EventStatus.CANCELLED);
        assertTrue("Event status should remain CANCELLED after multiple calls", stillCancelled);
    }


     //Verifies that the repo handles invalid IDs without crashing
    @Test
    public void editEvent_nonExistentId_handlesGracefully() throws InterruptedException {
        repo.editEvent("invalid-uuid-123", "Fake Event", "2026", "None", "None", 0);

        Thread.sleep(1000);
        assertTrue("Repository should handle non-existent IDs without throwing exceptions", true);
    }

    private boolean waitForEvent(String name, EventCondition condition) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final androidx.lifecycle.Observer<java.util.List<Event>> observer = new androidx.lifecycle.Observer<java.util.List<Event>>() {
            @Override
            public void onChanged(java.util.List<Event> list) {
                if (list != null) {
                    for (Event e : list) {
                        if (e.getName().equals(name) && condition.check(e)) {
                            latch.countDown();
                            repo.getEvents().removeObserver(this);
                            break;
                        }
                    }
                }
            }
        };

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            repo.getEvents().observeForever(observer);
        });

        return latch.await(8, TimeUnit.SECONDS);
    }

    private Event getEventSync(String name) throws InterruptedException {
        AtomicReference<Event> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        final androidx.lifecycle.Observer<java.util.List<Event>> observer = new androidx.lifecycle.Observer<java.util.List<Event>>() {
            @Override
            public void onChanged(java.util.List<Event> list) {
                if (list != null) {
                    for (Event e : list) {
                        if (e.getName().equals(name)) {
                            ref.set(e);
                            latch.countDown();
                            repo.getEvents().removeObserver(this);
                            break;
                        }
                    }
                }
            }
        };

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            repo.getEvents().observeForever(observer);
        });

        latch.await(5, TimeUnit.SECONDS);
        return ref.get();
    }

    interface EventCondition { boolean check(Event e); }
}