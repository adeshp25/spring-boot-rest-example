package com.khoubyari.example.service;

import com.khoubyari.example.domain.Hotel;
import org.junit.Test;
import static org.junit.Assert.*;

public class HotelServiceEventTest {

    @Test
    public void testHotelServiceEventCreationAndSourceAccess() {
        Hotel hotelSource = new Hotel();
        hotelSource.setId(1L);
        hotelSource.setName("Test Hotel Source");

        // Constructor expects a single Object source
        HotelServiceEvent event = new HotelServiceEvent(hotelSource);

        assertNotNull(event.getSource());
        assertTrue(event.getSource() instanceof Hotel);
        Hotel retrievedHotel = (Hotel) event.getSource();
        assertEquals(hotelSource.getId(), retrievedHotel.getId());
        assertEquals(hotelSource.getName(), retrievedHotel.getName());
    }

    @Test
    public void testHotelServiceEventCreationWithStringSource() {
        String messageSource = "Test Event Message";
        HotelServiceEvent event = new HotelServiceEvent(messageSource);

        assertNotNull(event.getSource());
        assertTrue(event.getSource() instanceof String);
        assertEquals(messageSource, event.getSource());
    }


    @Test
    public void testHotelServiceEventToString() {
        Hotel hotel = new Hotel(); // Source can be any object
        HotelServiceEvent event = new HotelServiceEvent(hotel);
        String expectedToString = "My HotelService Event"; // As defined in HotelServiceEvent.java
        assertEquals(expectedToString, event.toString());
    }
}
