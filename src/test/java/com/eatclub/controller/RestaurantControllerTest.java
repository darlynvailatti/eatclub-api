package com.eatclub.controller;

import com.eatclub.mapper.IRestaurantMapper;
import com.eatclub.model.Deal;
import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.Restaurant;
import com.eatclub.model.dtos.AvailableRestaurantsDTO;
import com.eatclub.model.dtos.ErrorDTO;
import com.eatclub.model.dtos.PeakTimeDTO;
import com.eatclub.service.IRestaurantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {

    @Mock
    private IRestaurantService restaurantService;

    @Mock
    private IRestaurantMapper restaurantMapper;

    @InjectMocks
    private RestaurantController restaurantController;

    @BeforeEach
    void setUp() {
        restaurantController = new RestaurantController(restaurantService, restaurantMapper);
    }

    @Test
    void testGetAvailableRestaurants_WithValidTime_ReturnsOk() {
        String timeOfDay = "14:30";
        LocalTime parsedTime = LocalTime.of(14, 30);

        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(10, 0), LocalTime.of(22, 0));
        Deal deal = new Deal("d1", "r1", 10.0f, true, false, 5);
        DealAtRestaurant dealAtRestaurant = new DealAtRestaurant(restaurant, deal);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant);

        AvailableRestaurantsDTO expectedDTO = new AvailableRestaurantsDTO();

        when(restaurantService.getAvailableRestaurantDealsByTime(parsedTime)).thenReturn(dealAtRestaurants);
        when(restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants)).thenReturn(expectedDTO);

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody());
        verify(restaurantService).getAvailableRestaurantDealsByTime(parsedTime);
        verify(restaurantMapper).toAvailableRestaurantsDTO(dealAtRestaurants);
    }

    @Test
    void testGetAvailableRestaurants_WithInvalidTimeFormat_ReturnsBadRequest() {
        String timeOfDay = "invalid-time";

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorDTO);
        ErrorDTO error = (ErrorDTO) response.getBody();
        assertNotNull(error.getMessage());
        assertEquals("INVALID_TIME_FORMAT", error.getError());
        assertTrue(error.getMessage().contains("Invalid time format"));
        verify(restaurantService, never()).getAvailableRestaurantDealsByTime(any());
        verify(restaurantMapper, never()).toAvailableRestaurantsDTO(any());
    }

    @Test
    void testGetAvailableRestaurants_WithInvalidTimeFormat_EmptyString_ReturnsBadRequest() {
        String timeOfDay = "";

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorDTO);
        ErrorDTO error = (ErrorDTO) response.getBody();
        assertEquals("INVALID_TIME_FORMAT", error.getError());
        verify(restaurantService, never()).getAvailableRestaurantDealsByTime(any());
    }

    @Test
    void testGetAvailableRestaurants_WithInvalidTimeFormat_WrongFormat_ReturnsBadRequest() {
        String timeOfDay = "2:30 PM";

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorDTO);
        ErrorDTO error = (ErrorDTO) response.getBody();
        assertEquals("INVALID_TIME_FORMAT", error.getError());
        verify(restaurantService, never()).getAvailableRestaurantDealsByTime(any());
    }

    @Test
    void testGetAvailableRestaurants_WithServiceException_ReturnsInternalServerError() {
        String timeOfDay = "14:30";
        LocalTime parsedTime = LocalTime.of(14, 30);

        when(restaurantService.getAvailableRestaurantDealsByTime(parsedTime))
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorDTO);
        ErrorDTO error = (ErrorDTO) response.getBody();
        assertNotNull(error.getMessage());
        assertEquals("INTERNAL_ERROR", error.getError());
        assertTrue(error.getMessage().contains("An unexpected error occurred"));
        verify(restaurantService).getAvailableRestaurantDealsByTime(parsedTime);
        verify(restaurantMapper, never()).toAvailableRestaurantsDTO(any());
    }

    @Test
    void testGetAvailableRestaurants_WithEmptyResult_ReturnsOk() {
        String timeOfDay = "02:00";
        LocalTime parsedTime = LocalTime.of(2, 0);

        List<DealAtRestaurant> emptyList = Arrays.asList();
        AvailableRestaurantsDTO expectedDTO = new AvailableRestaurantsDTO();

        when(restaurantService.getAvailableRestaurantDealsByTime(parsedTime)).thenReturn(emptyList);
        when(restaurantMapper.toAvailableRestaurantsDTO(emptyList)).thenReturn(expectedDTO);

        ResponseEntity<?> response = restaurantController.getAvailableRestaurants(timeOfDay);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody());
        verify(restaurantService).getAvailableRestaurantDealsByTime(parsedTime);
        verify(restaurantMapper).toAvailableRestaurantsDTO(emptyList);
    }

    @Test
    void testGetPeakTimeWindow_ReturnsOk() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.of(12, 0),
                LocalTime.of(15, 0));
        PeakTimeDTO expectedDTO = new PeakTimeDTO("12:00PM", "3:00PM");

        when(restaurantService.getPeakTimeWindow()).thenReturn(peakTimeWindow);
        when(restaurantMapper.toPeakTimeDTO(peakTimeWindow)).thenReturn(expectedDTO);

        ResponseEntity<PeakTimeDTO> response = restaurantController.getPeakTimeWindow();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody());
        verify(restaurantService).getPeakTimeWindow();
        verify(restaurantMapper).toPeakTimeDTO(peakTimeWindow);
    }

    @Test
    void testGetPeakTimeWindow_WithMinMaxTimes_ReturnsOk() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.MIN,
                LocalTime.MAX);
        PeakTimeDTO expectedDTO = new PeakTimeDTO("12:00AM", "11:59PM");

        when(restaurantService.getPeakTimeWindow()).thenReturn(peakTimeWindow);
        when(restaurantMapper.toPeakTimeDTO(peakTimeWindow)).thenReturn(expectedDTO);

        ResponseEntity<PeakTimeDTO> response = restaurantController.getPeakTimeWindow();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedDTO, response.getBody());
        verify(restaurantService).getPeakTimeWindow();
        verify(restaurantMapper).toPeakTimeDTO(peakTimeWindow);
    }
}
