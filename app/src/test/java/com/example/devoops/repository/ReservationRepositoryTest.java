package com.example.devoops.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ReservationRepositoryTest {

    
    // Constructor with mocked DB
    

    @Test
    void constructor_withMockedDb_setsDbField() throws Exception {
        // Use null as the mocked DB reference (avoids Firebase dependency)
        ReservationRepository repo = new ReservationRepository(null);

        Field dbField = ReservationRepository.class.getDeclaredField("db");
        dbField.setAccessible(true);

        // db should be null
        assertNull(dbField.get(repo));
    }

    
    //Class structure


    @Test
    void classStructure_hasCreateReservationMethod() throws Exception {
        Method m = ReservationRepository.class.getDeclaredMethod(
                "createReservation", String.class, String.class,
                ReservationRepository.ReservationCallback.class);
        assertNotNull(m);
    }

    @Test
    void classStructure_hasCancelReservationMethod() throws Exception {
        Method m = ReservationRepository.class.getDeclaredMethod(
                "cancelReservation", String.class, String.class,
                ReservationRepository.ReservationCallback.class);
        assertNotNull(m);
    }

    @Test
    void classStructure_hasGetReservationsForUserMethod() throws Exception {
        Method m = ReservationRepository.class.getDeclaredMethod(
                "getReservationsForUser", String.class);
        assertNotNull(m);
    }

    @Test
    void classStructure_hasGetEventByIdMethod() throws Exception {
        Method m = ReservationRepository.class.getDeclaredMethod(
                "getEventById", String.class, ReservationRepository.EventCallback.class);
        assertNotNull(m);
    }

    @Test
    void classStructure_hasDbField() throws Exception {
        Field f = ReservationRepository.class.getDeclaredField("db");
        assertNotNull(f);
    }

    @Test
    void classStructure_hasTagField() throws Exception {
        Field f = ReservationRepository.class.getDeclaredField("TAG");
        f.setAccessible(true);
        assertEquals("ReservationRepo", f.get(null));
    }

    
    // Constructors exist
    

    @Test
    void classStructure_hasDefaultConstructor() throws Exception {
        Constructor<?> c = ReservationRepository.class.getDeclaredConstructor();
        assertNotNull(c);
    }

    @Test
    void classStructure_hasMockedDbConstructor() throws Exception {
        Constructor<?> c = ReservationRepository.class.getDeclaredConstructor(
                com.google.firebase.database.DatabaseReference.class);
        assertNotNull(c);
    }

    // Callback interfaces
    

    @Test
    void reservationCallback_interfaceExists() {
        assertNotNull(ReservationRepository.ReservationCallback.class);
    }

    @Test
    void reservationCallback_hasOnSuccessMethod() throws Exception {
        Method m = ReservationRepository.ReservationCallback.class.getDeclaredMethod(
                "onSuccess", String.class);
        assertNotNull(m);
    }

    @Test
    void reservationCallback_hasOnErrorMethod() throws Exception {
        Method m = ReservationRepository.ReservationCallback.class.getDeclaredMethod(
                "onError", String.class);
        assertNotNull(m);
    }

    @Test
    void eventCallback_interfaceExists() {
        assertNotNull(ReservationRepository.EventCallback.class);
    }

    @Test
    void eventCallback_hasOnSuccessMethod() throws Exception {
        Method m = ReservationRepository.EventCallback.class.getDeclaredMethod(
                "onSuccess", com.example.devoops.models.Event.class);
        assertNotNull(m);
    }

    @Test
    void eventCallback_hasOnErrorMethod() throws Exception {
        Method m = ReservationRepository.EventCallback.class.getDeclaredMethod(
                "onError", String.class);
        assertNotNull(m);
    }

    
    // Callback implementations
    

    @Test
    void reservationCallback_onSuccess_receivesReservationId() {
        final String[] captured = {null};

        ReservationRepository.ReservationCallback callback =
                new ReservationRepository.ReservationCallback() {
                    @Override
                    public void onSuccess(String reservationId) {
                        captured[0] = reservationId;
                    }

                    @Override
                    public void onError(String error) { }
                };

        callback.onSuccess("r123");
        assertEquals("r123", captured[0]);
    }

    @Test
    void reservationCallback_onError_receivesErrorMessage() {
        final String[] captured = {null};

        ReservationRepository.ReservationCallback callback =
                new ReservationRepository.ReservationCallback() {
                    @Override
                    public void onSuccess(String reservationId) { }

                    @Override
                    public void onError(String error) {
                        captured[0] = error;
                    }
                };

        callback.onError("No seats available");
        assertEquals("No seats available", captured[0]);
    }

    @Test
    void eventCallback_onSuccess_receivesEvent() {
        final com.example.devoops.models.Event[] captured = {null};

        ReservationRepository.EventCallback callback =
                new ReservationRepository.EventCallback() {
                    @Override
                    public void onSuccess(com.example.devoops.models.Event event) {
                        captured[0] = event;
                    }

                    @Override
                    public void onError(String error) { }
                };

        com.example.devoops.models.Event event =
                new com.example.devoops.models.Event("e1", "Test", "2026", "Cat", "Loc", 10);
        callback.onSuccess(event);
        assertSame(event, captured[0]);
    }

    @Test
    void eventCallback_onError_receivesErrorMessage() {
        final String[] captured = {null};

        ReservationRepository.EventCallback callback =
                new ReservationRepository.EventCallback() {
                    @Override
                    public void onSuccess(com.example.devoops.models.Event event) { }

                    @Override
                    public void onError(String error) {
                        captured[0] = error;
                    }
                };

        callback.onError("Event not found");
        assertEquals("Event not found", captured[0]);
    }
}
