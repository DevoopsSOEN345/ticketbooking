package com.example.devoops.models;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    private Event event;

    @Before
    public void setUp() {
        event = new Event("e1", "Concert", "2026-04-10", "Music", "Montreal", 100);
    }

    @Test
    public void givenValidConstructor_whenEventCreated_thenFieldsAreSet_primePath1() {
        assertNotNull(event);
        assertEquals("e1", event.getEventId());
        assertEquals("Concert", event.getName());
        assertEquals(100, event.getTotalSeats());
        assertEquals(100, event.getOpenSeats());
        assertEquals(EventStatus.ACTIVE, event.getEventStatus());
    }

    @Test
    public void givenEmptyConstructor_whenEventCreated_thenNotNull_primePath2() {
        Event empty = new Event();
        assertNotNull(empty);
    }

    @Test
    public void givenSetters_whenValuesChanged_thenGettersReturnUpdatedValues() {
        event.setName("Updated Event");
        assertEquals("Updated Event", event.getName());
    }

    @Test
    public void givenEnoughSeats_whenCheckingAvailability_thenReturnsTrue_primePath3() {
        assertTrue(event.hasEnoughSeats(10));
    }

    @Test
    public void givenNotEnoughSeats_whenCheckingAvailability_thenReturnsFalse_primePath4() {
        assertFalse(event.hasEnoughSeats(200));
    }

    @Test
    public void givenReserveSeats_whenCalled_thenOpenSeatsDecrease_primePath5() {
        event.reserveSeats(10);
        assertEquals(90, event.getOpenSeats());
    }

    @Test
    public void givenCancelSeats_whenWithinLimit_thenSeatsIncrease_primePath6() {
        event.reserveSeats(20); // now 80
        event.cancelSeats(10);  // should be 90
        assertEquals(90, event.getOpenSeats());
    }

    @Test
    public void givenCancelSeats_whenExceedTotal_thenDoesNotOverflow_primePath7() {
        event.cancelSeats(50); //should stay capped at totalSeats (100)
        assertEquals(100, event.getOpenSeats());
    }

    @Test
    public void givenCancelEvent_whenCalled_thenStatusIsCancelled_primePath8() {
        event.cancelEvent();
        assertEquals(EventStatus.CANCELLED, event.getEventStatus());
    }

    @Test
    public void givenActiveEvent_whenChecked_thenIsActiveReturnsTrue_primePath9() {
        assertTrue(event.isActive());
    }

    @Test
    public void givenCancelledEvent_whenChecked_thenIsActiveReturnsFalse_primePath10() {
        event.cancelEvent();
        assertFalse(event.isActive());
    }
}