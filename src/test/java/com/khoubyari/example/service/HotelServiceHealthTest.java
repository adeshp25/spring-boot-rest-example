package com.khoubyari.example.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class HotelServiceHealthTest {

    @Mock
    private ServiceProperties serviceProperties;

    @InjectMocks
    private HotelServiceHealth hotelServiceHealth;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void health_whenServiceIsHealthy_shouldReturnUp() {
        when(serviceProperties.isHealthy()).thenReturn(true);
        Health health = hotelServiceHealth.health();
        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    public void health_whenServiceIsNotHealthy_shouldReturnDown() {
        when(serviceProperties.isHealthy()).thenReturn(false);
        when(serviceProperties.getName()).thenReturn("Test Hotel Service");
        Health health = hotelServiceHealth.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("Test Hotel Service is not healthy", health.getDetails().get("Error"));
    }

    @Test
    public void health_whenServiceIsNotHealthyAndNameIsNull_shouldReturnDownWithDefaultName() {
        when(serviceProperties.isHealthy()).thenReturn(false);
        when(serviceProperties.getName()).thenReturn(null); // Simulate null service name
        Health health = hotelServiceHealth.health();
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("HotelService is not healthy", health.getDetails().get("Error"));
    }
}
