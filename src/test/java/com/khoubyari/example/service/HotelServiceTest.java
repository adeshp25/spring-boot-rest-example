package com.khoubyari.example.service;

import com.khoubyari.example.dao.jpa.HotelRepository;
import com.khoubyari.example.domain.Hotel;
import com.khoubyari.example.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel hotel;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setName("Test Hotel");
        hotel.setCity("Test City");
        hotel.setDescription("Test Description");
        hotel.setRating(5);
    }

    @Test
    public void createHotel_shouldSaveAndReturnHotel() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);
        Hotel createdHotel = hotelService.createHotel(hotel);
        assertNotNull(createdHotel);
        assertEquals(hotel.getName(), createdHotel.getName());
        verify(hotelRepository, times(1)).save(hotel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createHotel_withNullHotel_shouldThrowException() {
        hotelService.createHotel(null);
    }

    @Test
    public void getHotel_whenHotelExists_shouldReturnHotel() {
        when(hotelRepository.findOne(1L)).thenReturn(hotel);
        Hotel foundHotel = hotelService.getHotel(1L);
        assertNotNull(foundHotel);
        assertEquals(hotel.getId(), foundHotel.getId());
        verify(hotelRepository, times(1)).findOne(1L);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getHotel_whenHotelNotExists_shouldThrowResourceNotFoundException() {
        when(hotelRepository.findOne(2L)).thenReturn(null);
        hotelService.getHotel(2L);
    }

    @Test
    public void getAllHotels_shouldReturnPagedHotels() {
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel);
        Pageable pageable = new PageRequest(0, 10);
        Page<Hotel> hotelPage = new PageImpl<>(hotels, pageable, 1);
        when(hotelRepository.findAll(any(Pageable.class))).thenReturn(hotelPage);

        Page<Hotel> result = hotelService.getAllHotels(0, 10);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(hotel.getName(), result.getContent().get(0).getName());
        verify(hotelRepository, times(1)).findAll(pageable);
    }

    @Test
    public void getAllHotels_whenNoHotels_shouldReturnEmptyPage() {
        Pageable pageable = new PageRequest(0, 10);
        Page<Hotel> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(hotelRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        Page<Hotel> result = hotelService.getAllHotels(0, 10);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(hotelRepository, times(1)).findAll(pageable);
    }

    @Test
    public void updateHotel_whenHotelExists_shouldUpdateAndReturnHotel() {
        Hotel existingHotel = new Hotel();
        existingHotel.setId(1L);
        existingHotel.setName("Old Name");

        Hotel updatedDetails = new Hotel();
        updatedDetails.setId(1L);
        updatedDetails.setName("New Name");
        updatedDetails.setCity("New City");

        when(hotelRepository.findOne(1L)).thenReturn(existingHotel);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(updatedDetails);

        hotelService.updateHotel(updatedDetails);

        verify(hotelRepository, times(1)).findOne(1L);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateHotel_whenHotelNotExists_shouldThrowResourceNotFoundException() {
        Hotel nonExistentHotel = new Hotel();
        nonExistentHotel.setId(99L);
        nonExistentHotel.setName("Non Existent");
        when(hotelRepository.findOne(99L)).thenReturn(null);
        hotelService.updateHotel(nonExistentHotel);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateHotel_withNullHotel_shouldThrowException() {
        hotelService.updateHotel(null);
    }

    @Test
    public void deleteHotel_whenHotelExists_shouldCallDelete() {
        when(hotelRepository.findOne(1L)).thenReturn(hotel);
        doNothing().when(hotelRepository).delete(1L);
        hotelService.deleteHotel(1L);
        verify(hotelRepository, times(1)).findOne(1L);
        verify(hotelRepository, times(1)).delete(1L);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteHotel_whenHotelNotExists_shouldThrowResourceNotFoundException() {
        when(hotelRepository.findOne(2L)).thenReturn(null);
        hotelService.deleteHotel(2L);
    }
}
