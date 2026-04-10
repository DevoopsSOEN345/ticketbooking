package com.example.devoops.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ReservationTest {

    
    // Empty constructor
    

    @Test
    void emptyConstructor_allFieldsNull() {
        Reservation r = new Reservation();
        assertNull(r.getReservationId());
        assertNull(r.getUserId());
        assertNull(r.getEventId());
        assertEquals(0, r.getTimestamp());
    }

    
    // Parameterized constructor
    

    @Test
    void paramConstructor_setsAllFields() {
        Reservation r = new Reservation("r1", "u1", "e1", 1000L);
        assertEquals("r1", r.getReservationId());
        assertEquals("u1", r.getUserId());
        assertEquals("e1", r.getEventId());
        assertEquals(1000L, r.getTimestamp());
    }

    
    //  Setters
    

    @Test
    void setReservationId_updatesValue() {
        Reservation r = new Reservation();
        r.setReservationId("r99");
        assertEquals("r99", r.getReservationId());
    }

    @Test
    void setUserId_updatesValue() {
        Reservation r = new Reservation();
        r.setUserId("u99");
        assertEquals("u99", r.getUserId());
    }

    @Test
    void setEventId_updatesValue() {
        Reservation r = new Reservation();
        r.setEventId("e99");
        assertEquals("e99", r.getEventId());
    }

    @Test
    void setTimestamp_updatesValue() {
        Reservation r = new Reservation();
        r.setTimestamp(5000L);
        assertEquals(5000L, r.getTimestamp());
    }

    
    // Setter overrides constructor value
    

    @Test
    void setter_overridesConstructorValue() {
        Reservation r = new Reservation("r1", "u1", "e1", 1000L);
        r.setReservationId("r2");
        r.setUserId("u2");
        r.setEventId("e2");
        r.setTimestamp(2000L);

        assertEquals("r2", r.getReservationId());
        assertEquals("u2", r.getUserId());
        assertEquals("e2", r.getEventId());
        assertEquals(2000L, r.getTimestamp());
    }

    
    // Prime Path: Null values
    

    @Test
    void setNullValues_acceptsNull() {
        Reservation r = new Reservation("r1", "u1", "e1", 1000L);
        r.setReservationId(null);
        r.setUserId(null);
        r.setEventId(null);

        assertNull(r.getReservationId());
        assertNull(r.getUserId());
        assertNull(r.getEventId());
    }
}
