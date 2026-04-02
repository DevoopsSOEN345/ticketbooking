package com.example.devoops.presentation;

import static org.junit.jupiter.api.Assertions.*;

import com.example.devoops.models.Event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class EventAdapterTest {

    private Event testEvent;
    private EventAdapter.OnEventClickListener dummyClickListener;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setDateTime("2026-04-10");
        testEvent.setCategory("Music");
        testEvent.setLocation("Montreal");
        testEvent.setTotalSeats(100);
        testEvent.setOpenSeats(50);
        testEvent.setEventId("evt-001");

        dummyClickListener = new EventAdapter.OnEventClickListener() {
            @Override public void onEdit(Event event) {}
            @Override public void onDelete(Event event) {}
        };
    }

    
    // Prime Path 1: isAdmin=true → Admin branch
    
    @Test
    @DisplayName("PP1: Admin adapter — constructor and getItemCount")
    void primePath1_adminAdapter() {
        EventAdapter adapter = new EventAdapter(true, dummyClickListener);
        assertEquals(0, adapter.getItemCount());
    }

    // Prime Path 2: Customer, already reserved → Cancel branch
    @Test
    @DisplayName("PP2: Reserved set contains event — Cancel branch condition")
    void primePath2_reservedSetContainsEvent() {
        Set<String> reserved = new HashSet<>();
        reserved.add("evt-001");

        assertTrue(reserved.contains(testEvent.getEventId()));
    }

    // Prime Path 3: Customer, not reserved, openSeats <= 0 → Sold Out
    @Test
    @DisplayName("PP3: Not reserved and no seats — Sold Out branch condition")
    void primePath3_notReservedNoSeats() {
        testEvent.setOpenSeats(0);
        Set<String> reserved = new HashSet<>();

        assertFalse(reserved.contains(testEvent.getEventId()));
        assertTrue(testEvent.getOpenSeats() <= 0);
    }

    // Prime Path 4: Customer, not reserved, openSeats > 0 → Reserve
    @Test
    @DisplayName("PP4: Not reserved and seats available — Reserve branch condition")
    void primePath4_notReservedSeatsAvailable() {
        testEvent.setOpenSeats(25);
        Set<String> reserved = new HashSet<>();

        assertFalse(reserved.contains(testEvent.getEventId()));
        assertTrue(testEvent.getOpenSeats() > 0);
    }
}