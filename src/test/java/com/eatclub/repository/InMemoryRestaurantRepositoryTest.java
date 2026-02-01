package com.eatclub.repository;

import com.eatclub.common.Constants;
import com.eatclub.model.Deal;
import com.eatclub.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InMemoryRestaurantRepositoryTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private InMemoryRestaurantRepository repository;
    private List<Restaurant> testRestaurants;
    private List<Deal> testDeals;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        setupMockRestTemplateResponse();
        repository = new InMemoryRestaurantRepository(restTemplate);
        testRestaurants = repository.findAllRestaurants();
        testDeals = new ArrayList<>();
        testDeals.addAll(repository.findDealsByRestaurantId("r1"));
        testDeals.addAll(repository.findDealsByRestaurantId("r2"));
        testDeals.addAll(repository.findDealsByRestaurantId("r3"));
    }

    private void setupMockRestTemplateResponse() {
        String jsonResponse = """
            {
              "restaurants": [
                {
                  "objectId": "r1",
                  "name": "Restaurant 1",
                  "address1": "123 Main St",
                  "suburb": "City",
                  "open": "10:00am",
                  "close": "10:00pm",
                  "deals": [
                    {"objectId": "d1", "discount": "10.0", "dineIn": "true", "lightning": "false", "qtyLeft": "5"},
                    {"objectId": "d2", "discount": "15.0", "dineIn": "false", "lightning": "true", "qtyLeft": "3"}
                  ]
                },
                {
                  "objectId": "r2",
                  "name": "Restaurant 2",
                  "address1": "456 Oak Ave",
                  "suburb": "Suburb",
                  "open": "11:30am",
                  "close": "11:30pm",
                  "deals": [
                    {"objectId": "d3", "discount": "20.0", "dineIn": "true", "lightning": "false", "qtyLeft": "10"}
                  ]
                },
                {
                  "objectId": "r3",
                  "name": "Restaurant 3",
                  "address1": "789 Pine St",
                  "suburb": "Town",
                  "open": "10:00pm",
                  "close": "2:00am",
                  "deals": [
                    {"objectId": "d4", "discount": "25.0", "dineIn": "true", "lightning": "false", "qtyLeft": "8"}
                  ]
                },
                {
                  "objectId": "r4",
                  "name": "Restaurant 4",
                  "address1": "321 Elm St",
                  "suburb": "Village",
                  "open": "9:00am",
                  "close": "5:00pm",
                  "deals": []
                }
              ]
            }
            """;

        mockServer.expect(requestTo(Constants.EC_API_CHALLENGE_ENDPOINT))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
    }

    @Test
    void testFindAllRestaurants_ReturnsAllRestaurants() {
        List<Restaurant> result = repository.findAllRestaurants();
        
        assertEquals(4, result.size());
        assertTrue(result.containsAll(testRestaurants));
    }

    @Test
    void testFindAllRestaurants_ReturnsNewList() {
        List<Restaurant> result1 = repository.findAllRestaurants();
        List<Restaurant> result2 = repository.findAllRestaurants();
        
        assertNotSame(result1, result2);
        assertEquals(result1, result2);
    }

    @Test
    void testFindAllRestaurants_WithEmptyList() throws Exception {
        RestTemplate emptyRestTemplate = new RestTemplate();
        MockRestServiceServer emptyMockServer = MockRestServiceServer.bindTo(emptyRestTemplate).build();
        
        String jsonResponse = """
            {
              "restaurants": []
            }
            """;
        
        emptyMockServer.expect(requestTo(Constants.EC_API_CHALLENGE_ENDPOINT))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
        
        InMemoryRestaurantRepository emptyRepo = new InMemoryRestaurantRepository(emptyRestTemplate);
        List<Restaurant> result = emptyRepo.findAllRestaurants();
        
        assertTrue(result.isEmpty());
        emptyMockServer.verify();
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNormalHours_TimeDuringOpenHours() {
        LocalTime queryTime = LocalTime.of(15, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r1")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r2")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r4")));
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNormalHours_TimeAtOpenTime() {
        LocalTime queryTime = LocalTime.of(10, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r1")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r4")));
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNormalHours_TimeAtCloseTime() {
        LocalTime queryTime = LocalTime.of(22, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r1")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r2")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r3")));
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNormalHours_TimeBeforeOpen() {
        LocalTime queryTime = LocalTime.of(9, 30);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(1, result.size());
        assertEquals("r4", result.get(0).getObjectId());
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNormalHours_TimeAfterClose() {
        LocalTime queryTime = LocalTime.of(17, 30);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r1")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r2")));
    }

    @Test
    void testFindAvailableRestaurantsAt_WithRestaurantSpanningMidnight_TimeBeforeMidnight() {
        LocalTime queryTime = LocalTime.of(23, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r2")));
        assertTrue(result.stream().anyMatch(r -> r.getObjectId().equals("r3")));
    }

    @Test
    void testFindAvailableRestaurantsAt_WithRestaurantSpanningMidnight_TimeAfterMidnight() {
        LocalTime queryTime = LocalTime.of(1, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(1, result.size());
        assertEquals("r3", result.get(0).getObjectId());
    }

    @Test
    void testFindAvailableRestaurantsAt_WithRestaurantSpanningMidnight_TimeAtMidnight() {
        LocalTime queryTime = LocalTime.of(0, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(1, result.size());
        assertEquals("r3", result.get(0).getObjectId());
    }

    @Test
    void testFindAvailableRestaurantsAt_WithRestaurantSpanningMidnight_TimeNotInRange() {
        LocalTime queryTime = LocalTime.of(5, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAvailableRestaurantsAt_WithNoAvailableRestaurants() {
        LocalTime queryTime = LocalTime.of(3, 0);
        
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindDealsByRestaurantId_WithExistingRestaurant_ReturnsDeals() {
        List<Deal> result = repository.findDealsByRestaurantId("r1");
        
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.getObjectId().equals("d1")));
        assertTrue(result.stream().anyMatch(d -> d.getObjectId().equals("d2")));
    }

    @Test
    void testFindDealsByRestaurantId_WithExistingRestaurant_SingleDeal() {
        List<Deal> result = repository.findDealsByRestaurantId("r2");
        
        assertEquals(1, result.size());
        assertEquals("d3", result.get(0).getObjectId());
    }

    @Test
    void testFindDealsByRestaurantId_WithExistingRestaurant_NoDeals() {
        List<Deal> result = repository.findDealsByRestaurantId("r4");
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindDealsByRestaurantId_WithNonExistentRestaurant_ReturnsEmptyList() {
        List<Deal> result = repository.findDealsByRestaurantId("non-existent");
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindDealsByRestaurantId_WithNullRestaurantId_ReturnsEmptyList() {
        List<Deal> result = repository.findDealsByRestaurantId(null);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindDealsByRestaurantId_ReturnsSameListReference() {
        List<Deal> result1 = repository.findDealsByRestaurantId("r1");
        List<Deal> result2 = repository.findDealsByRestaurantId("r1");
        
        assertSame(result1, result2);
        assertEquals(result1, result2);
    }

    @Test
    void testFindAvailableRestaurantsAt_WithAllRestaurantsOpen() throws Exception {
        RestTemplate allDayRestTemplate = new RestTemplate();
        MockRestServiceServer allDayMockServer = MockRestServiceServer.bindTo(allDayRestTemplate).build();
        
        String jsonResponse = """
            {
              "restaurants": [
                {
                  "objectId": "r5",
                  "name": "All Day Restaurant",
                  "address1": "999 Test St",
                  "suburb": "City",
                  "open": "12:00am",
                  "close": "11:59pm",
                  "deals": []
                }
              ]
            }
            """;
        
        allDayMockServer.expect(requestTo(Constants.EC_API_CHALLENGE_ENDPOINT))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));
        
        InMemoryRestaurantRepository allDayRepo = new InMemoryRestaurantRepository(allDayRestTemplate);
        LocalTime queryTime = LocalTime.of(12, 0);
        List<Restaurant> result = allDayRepo.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(1, result.size());
        assertEquals("r5", result.get(0).getObjectId());
        allDayMockServer.verify();
    }

    @Test
    void testFindAvailableRestaurantsAt_WithExactBoundaryTimes() {
        LocalTime queryTime1 = LocalTime.of(10, 0);
        LocalTime queryTime2 = LocalTime.of(22, 0);
        
        List<Restaurant> result1 = repository.findAvailableRestaurantsAt(queryTime1);
        List<Restaurant> result2 = repository.findAvailableRestaurantsAt(queryTime2);
        
        assertTrue(result1.stream().anyMatch(r -> r.getObjectId().equals("r1")));
        assertTrue(result2.stream().anyMatch(r -> r.getObjectId().equals("r1")));
    }
}
