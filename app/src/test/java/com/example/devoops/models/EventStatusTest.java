package com.example.devoops.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventStatusTest {

    @Test
    public void givenEnumValues_whenFetched_thenContainsActiveAndCancelled_primePath1() {
        EventStatus[] statuses = EventStatus.values();
        assertEquals(2, statuses.length);
    }

    @Test
    public void givenStatusName_whenValueOfCalled_thenReturnsCorrectEnum_primePath2() {
        assertEquals(EventStatus.ACTIVE, EventStatus.valueOf("ACTIVE"));
        assertEquals(EventStatus.CANCELLED, EventStatus.valueOf("CANCELLED"));
    }
}