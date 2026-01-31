package com.eatclub.service;

import com.eatclub.model.Deal;
import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.Restaurant;
import com.eatclub.repository.LocalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private LocalRepository restaurantRepository;

    @InjectMocks
    private RestaurantServiceImpl restaurantService;

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantServiceImpl(restaurantRepository);
    }

    @Test
    void testGetAvailableRestaurantDealsByTime_WithAvailableRestaurantsAndDeals() {
        LocalTime queryTime = LocalTime.of(12, 0);
        
        Restaurant restaurant1 = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(10, 0), LocalTime.of(22, 0));
        Restaurant restaurant2 = new Restaurant("r2", "Restaurant 2", "456 Oak Ave", "City", 
            LocalTime.of(11, 0), LocalTime.of(23, 0));
        
        List<Restaurant> availableRestaurants = Arrays.asList(restaurant1, restaurant2);
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r1", 15.0f, false, true, 3);
        Deal deal3 = new Deal("d3", "r2", 20.0f, true, false, 10);
        
        when(restaurantRepository.findAvailableRestaurantsAt(queryTime)).thenReturn(availableRestaurants);
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(Arrays.asList(deal1, deal2));
        when(restaurantRepository.findDealsByRestaurantId("r2")).thenReturn(Arrays.asList(deal3));
        
        List<DealAtRestaurant> result = restaurantService.getAvailableRestaurantDealsByTime(queryTime);
        
        assertEquals(3, result.size());
        assertEquals(restaurant1, result.get(0).getRestaurant());
        assertEquals(deal1, result.get(0).getDeal());
        assertEquals(restaurant1, result.get(1).getRestaurant());
        assertEquals(deal2, result.get(1).getDeal());
        assertEquals(restaurant2, result.get(2).getRestaurant());
        assertEquals(deal3, result.get(2).getDeal());
        
        verify(restaurantRepository).findAvailableRestaurantsAt(queryTime);
        verify(restaurantRepository).findDealsByRestaurantId("r1");
        verify(restaurantRepository).findDealsByRestaurantId("r2");
    }

    @Test
    void testGetAvailableRestaurantDealsByTime_WithNoAvailableRestaurants() {
        LocalTime queryTime = LocalTime.of(2, 0);
        
        when(restaurantRepository.findAvailableRestaurantsAt(queryTime)).thenReturn(new ArrayList<>());
        
        List<DealAtRestaurant> result = restaurantService.getAvailableRestaurantDealsByTime(queryTime);
        
        assertTrue(result.isEmpty());
        verify(restaurantRepository).findAvailableRestaurantsAt(queryTime);
        verify(restaurantRepository, never()).findDealsByRestaurantId(anyString());
    }

    @Test
    void testGetAvailableRestaurantDealsByTime_WithRestaurantsButNoDeals() {
        LocalTime queryTime = LocalTime.of(14, 0);
        
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(10, 0), LocalTime.of(22, 0));
        
        List<Restaurant> availableRestaurants = Arrays.asList(restaurant);
        
        when(restaurantRepository.findAvailableRestaurantsAt(queryTime)).thenReturn(availableRestaurants);
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(new ArrayList<>());
        
        List<DealAtRestaurant> result = restaurantService.getAvailableRestaurantDealsByTime(queryTime);
        
        assertTrue(result.isEmpty());
        verify(restaurantRepository).findAvailableRestaurantsAt(queryTime);
        verify(restaurantRepository).findDealsByRestaurantId("r1");
    }

    @Test
    void testGetPeakTimeWindow_WithEmptyRestaurantList() {
        when(restaurantRepository.findAllRestaurants()).thenReturn(new ArrayList<>());
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.MIN, result.getPeakTimeStart());
        assertEquals(LocalTime.MAX, result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
    }

    @Test
    void testGetPeakTimeWindow_WithSingleRestaurant() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(10, 0), LocalTime.of(22, 0));
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r1", 15.0f, false, true, 3);
        
        when(restaurantRepository.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(Arrays.asList(deal1, deal2));
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.of(9, 0), result.getPeakTimeStart());
        assertEquals(LocalTime.of(12, 0), result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
        verify(restaurantRepository, atLeastOnce()).findDealsByRestaurantId("r1");
    }

    @Test
    void testGetPeakTimeWindow_WithMultipleRestaurants_FindsCorrectPeak() {
        Restaurant restaurant1 = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(10, 0), LocalTime.of(11, 0));
        Restaurant restaurant2 = new Restaurant("r2", "Restaurant 2", "456 Oak Ave", "City", 
            LocalTime.of(12, 30), LocalTime.of(14, 30));
        Restaurant restaurant3 = new Restaurant("r3", "Restaurant 3", "789 Pine St", "City", 
            LocalTime.of(13, 0), LocalTime.of(14, 0));
        Restaurant restaurant4 = new Restaurant("r4", "Restaurant 4", "321 Elm St", "City", 
            LocalTime.of(13, 30), LocalTime.of(14, 30));
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r2", 15.0f, false, true, 3);
        Deal deal3 = new Deal("d3", "r2", 20.0f, true, false, 10);
        Deal deal4 = new Deal("d4", "r3", 25.0f, true, false, 8);
        Deal deal5 = new Deal("d5", "r4", 30.0f, true, false, 12);
        
        when(restaurantRepository.findAllRestaurants()).thenReturn(
            Arrays.asList(restaurant1, restaurant2, restaurant3, restaurant4));
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(Arrays.asList(deal1));
        when(restaurantRepository.findDealsByRestaurantId("r2")).thenReturn(Arrays.asList(deal2, deal3));
        when(restaurantRepository.findDealsByRestaurantId("r3")).thenReturn(Arrays.asList(deal4));
        when(restaurantRepository.findDealsByRestaurantId("r4")).thenReturn(Arrays.asList(deal5));
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.of(12, 0), result.getPeakTimeStart());
        assertEquals(LocalTime.of(15, 0), result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
    }

    @Test
    void testGetPeakTimeWindow_WithRestaurantOpenAcrossMidnight() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(22, 0), LocalTime.of(2, 0));
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r1", 15.0f, false, true, 3);
        
        when(restaurantRepository.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(Arrays.asList(deal1, deal2));
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.of(0, 0), result.getPeakTimeStart());
        assertEquals(LocalTime.of(3, 0), result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
    }

    @Test
    void testGetPeakTimeWindow_WithMultipleTimeWindows_SelectsHighest() {
        Restaurant restaurant1 = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(9, 0), LocalTime.of(11, 0));
        Restaurant restaurant2 = new Restaurant("r2", "Restaurant 2", "456 Oak Ave", "City", 
            LocalTime.of(15, 30), LocalTime.of(18, 0));
        Restaurant restaurant3 = new Restaurant("r3", "Restaurant 3", "789 Pine St", "City", 
            LocalTime.of(15, 30), LocalTime.of(18, 0));
        Restaurant restaurant4 = new Restaurant("r4", "Restaurant 4", "321 Elm St", "City", 
            LocalTime.of(15, 30), LocalTime.of(18, 0));
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r2", 15.0f, false, true, 3);
        Deal deal3 = new Deal("d3", "r3", 20.0f, true, false, 10);
        Deal deal4 = new Deal("d4", "r4", 25.0f, true, false, 8);
        
        when(restaurantRepository.findAllRestaurants()).thenReturn(
            Arrays.asList(restaurant1, restaurant2, restaurant3, restaurant4));
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(Arrays.asList(deal1));
        when(restaurantRepository.findDealsByRestaurantId("r2")).thenReturn(Arrays.asList(deal2));
        when(restaurantRepository.findDealsByRestaurantId("r3")).thenReturn(Arrays.asList(deal3));
        when(restaurantRepository.findDealsByRestaurantId("r4")).thenReturn(Arrays.asList(deal4));
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.of(15, 0), result.getPeakTimeStart());
        assertEquals(LocalTime.of(18, 0), result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
    }

    @Test
    void testGetPeakTimeWindow_WithRestaurantOpenAllDay() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City", 
            LocalTime.of(0, 0), LocalTime.of(23, 59));
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r1", 15.0f, false, true, 3);
        Deal deal3 = new Deal("d3", "r1", 20.0f, true, false, 10);
        
        when(restaurantRepository.findAllRestaurants()).thenReturn(Arrays.asList(restaurant));
        when(restaurantRepository.findDealsByRestaurantId("r1")).thenReturn(
            Arrays.asList(deal1, deal2, deal3));
        
        PeakTimeWindow result = restaurantService.getPeakTimeWindow();
        
        assertNotNull(result);
        assertEquals(LocalTime.MIN, result.getPeakTimeStart());
        assertEquals(LocalTime.of(3, 0), result.getPeakTimeEnd());
        verify(restaurantRepository).findAllRestaurants();
    }
}
