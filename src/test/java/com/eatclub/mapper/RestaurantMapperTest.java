package com.eatclub.mapper;

import com.eatclub.model.Deal;
import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.Restaurant;
import com.eatclub.model.dtos.AvailableRestaurantsDTO;
import com.eatclub.model.dtos.DealDTO;
import com.eatclub.model.dtos.PeakTimeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantMapperTest {

    private RestaurantMapper restaurantMapper;

    @BeforeEach
    void setUp() {
        restaurantMapper = new RestaurantMapper();
    }

    @Test
    void testToAvailableRestaurantsDTO_WithSingleDealAtRestaurant() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(10, 0), LocalTime.of(22, 0));
        Deal deal = new Deal("d1", "r1", 10.5f, true, false, 5);
        DealAtRestaurant dealAtRestaurant = new DealAtRestaurant(restaurant, deal);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant);

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants);

        assertNotNull(result);
        assertNotNull(result.getDeals());
        assertEquals(1, result.getDeals().size());

        DealDTO dealDTO = result.getDeals().get(0);
        assertEquals("r1", dealDTO.getRestaurantObjectId());
        assertEquals("Restaurant 1", dealDTO.getRestaurantName());
        assertEquals("123 Main St", dealDTO.getRestaurantAddress1());
        assertEquals("City", dealDTO.getRestarantSuburb());
        assertEquals("10:00AM", dealDTO.getRestaurantOpen());
        assertEquals("10:00PM", dealDTO.getRestaurantClose());
        assertEquals("d1", dealDTO.getObjectId());
        assertEquals("10.5", dealDTO.getDiscount());
        assertEquals("true", dealDTO.getDineIn());
        assertEquals("false", dealDTO.getLightning());
        assertEquals("10:00AM", dealDTO.getOpen());
        assertEquals("10:00PM", dealDTO.getClose());
        assertEquals("5", dealDTO.getQtyLeft());
    }

    @Test
    void testToAvailableRestaurantsDTO_WithMultipleDealsAtRestaurants() {
        Restaurant restaurant1 = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(9, 0), LocalTime.of(21, 0));
        Restaurant restaurant2 = new Restaurant("r2", "Restaurant 2", "456 Oak Ave", "Suburb",
                LocalTime.of(11, 30), LocalTime.of(23, 30));

        Deal deal1 = new Deal("d1", "r1", 15.0f, true, true, 10);
        Deal deal2 = new Deal("d2", "r2", 20.0f, false, false, 3);

        DealAtRestaurant dealAtRestaurant1 = new DealAtRestaurant(restaurant1, deal1);
        DealAtRestaurant dealAtRestaurant2 = new DealAtRestaurant(restaurant2, deal2);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant1, dealAtRestaurant2);

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants);

        assertNotNull(result);
        assertEquals(2, result.getDeals().size());

        DealDTO dealDTO1 = result.getDeals().get(0);
        assertEquals("r1", dealDTO1.getRestaurantObjectId());
        assertEquals("Restaurant 1", dealDTO1.getRestaurantName());
        assertEquals("15.0", dealDTO1.getDiscount());
        assertEquals("true", dealDTO1.getDineIn());
        assertEquals("true", dealDTO1.getLightning());
        assertEquals("10", dealDTO1.getQtyLeft());

        DealDTO dealDTO2 = result.getDeals().get(1);
        assertEquals("r2", dealDTO2.getRestaurantObjectId());
        assertEquals("Restaurant 2", dealDTO2.getRestaurantName());
        assertEquals("20.0", dealDTO2.getDiscount());
        assertEquals("false", dealDTO2.getDineIn());
        assertEquals("false", dealDTO2.getLightning());
        assertEquals("3", dealDTO2.getQtyLeft());
    }

    @Test
    void testToAvailableRestaurantsDTO_WithEmptyList() {
        List<DealAtRestaurant> emptyList = Arrays.asList();

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(emptyList);

        assertNotNull(result);
        assertNotNull(result.getDeals());
        assertTrue(result.getDeals().isEmpty());
    }

    @Test
    void testToAvailableRestaurantsDTO_WithRestaurantOpenAtMidnight() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(0, 0), LocalTime.of(23, 59));
        Deal deal = new Deal("d1", "r1", 10.0f, true, false, 5);
        DealAtRestaurant dealAtRestaurant = new DealAtRestaurant(restaurant, deal);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant);

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants);

        DealDTO dealDTO = result.getDeals().get(0);
        assertEquals("12:00AM", dealDTO.getRestaurantOpen());
        assertEquals("11:59PM", dealDTO.getRestaurantClose());
    }

    @Test
    void testToAvailableRestaurantsDTO_WithRestaurantOpenAtNoon() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(12, 0), LocalTime.of(13, 0));
        Deal deal = new Deal("d1", "r1", 10.0f, true, false, 5);
        DealAtRestaurant dealAtRestaurant = new DealAtRestaurant(restaurant, deal);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant);

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants);

        DealDTO dealDTO = result.getDeals().get(0);
        assertEquals("12:00PM", dealDTO.getRestaurantOpen());
        assertEquals("1:00PM", dealDTO.getRestaurantClose());
    }

    @Test
    void testToAvailableRestaurantsDTO_WithZeroDiscount() {
        Restaurant restaurant = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(10, 0), LocalTime.of(22, 0));
        Deal deal = new Deal("d1", "r1", 0.0f, false, false, 0);
        DealAtRestaurant dealAtRestaurant = new DealAtRestaurant(restaurant, deal);
        List<DealAtRestaurant> dealAtRestaurants = Arrays.asList(dealAtRestaurant);

        AvailableRestaurantsDTO result = restaurantMapper.toAvailableRestaurantsDTO(dealAtRestaurants);

        DealDTO dealDTO = result.getDeals().get(0);
        assertEquals("0.0", dealDTO.getDiscount());
        assertEquals("false", dealDTO.getDineIn());
        assertEquals("false", dealDTO.getLightning());
        assertEquals("0", dealDTO.getQtyLeft());
    }

    @Test
    void testToPeakTimeDTO_WithStandardTimes() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.of(12, 0),
                LocalTime.of(15, 0));

        PeakTimeDTO result = restaurantMapper.toPeakTimeDTO(peakTimeWindow);

        assertNotNull(result);
        assertEquals("12:00PM", result.getPeakTimeStart());
        assertEquals("3:00PM", result.getPeakTimeEnd());
    }

    @Test
    void testToPeakTimeDTO_WithMidnightTimes() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.of(0, 0),
                LocalTime.of(1, 0));

        PeakTimeDTO result = restaurantMapper.toPeakTimeDTO(peakTimeWindow);

        assertNotNull(result);
        assertEquals("12:00AM", result.getPeakTimeStart());
        assertEquals("1:00AM", result.getPeakTimeEnd());
    }

    @Test
    void testToPeakTimeDTO_WithNoonTime() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.of(12, 0),
                LocalTime.of(12, 30));

        PeakTimeDTO result = restaurantMapper.toPeakTimeDTO(peakTimeWindow);

        assertNotNull(result);
        assertEquals("12:00PM", result.getPeakTimeStart());
        assertEquals("12:30PM", result.getPeakTimeEnd());
    }

    @Test
    void testToPeakTimeDTO_WithMinMaxTimes() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.MIN,
                LocalTime.MAX);

        PeakTimeDTO result = restaurantMapper.toPeakTimeDTO(peakTimeWindow);

        assertNotNull(result);
        assertEquals("12:00AM", result.getPeakTimeStart());
        assertEquals("11:59PM", result.getPeakTimeEnd());
    }

    @Test
    void testToPeakTimeDTO_WithEveningTimes() {
        PeakTimeWindow peakTimeWindow = new PeakTimeWindow(
                LocalTime.of(18, 0),
                LocalTime.of(22, 30));

        PeakTimeDTO result = restaurantMapper.toPeakTimeDTO(peakTimeWindow);

        assertNotNull(result);
        assertEquals("6:00PM", result.getPeakTimeStart());
        assertEquals("10:30PM", result.getPeakTimeEnd());
    }
}
