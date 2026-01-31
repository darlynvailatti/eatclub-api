package com.eatclub.repository;

import com.eatclub.model.Deal;
import com.eatclub.model.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryRestaurantRepositoryTest {

    private InMemoryRestaurantRepository repository;
    private List<Restaurant> testRestaurants;
    private Map<String, List<Deal>> testDealsByRestaurantId;

    @BeforeEach
    void setUp() throws Exception {
        repository = createTestRepository();
    }

    private InMemoryRestaurantRepository createTestRepository() throws Exception {
        InMemoryRestaurantRepository repo;
        try {
            repo = new InMemoryRestaurantRepository();
        } catch (Exception e) {
            repo = createRepositoryWithoutApiCall();
        }
        
        testRestaurants = new ArrayList<>();
        testDealsByRestaurantId = new HashMap<>();
        
        Restaurant restaurant1 = new Restaurant("r1", "Restaurant 1", "123 Main St", "City",
                LocalTime.of(10, 0), LocalTime.of(22, 0));
        Restaurant restaurant2 = new Restaurant("r2", "Restaurant 2", "456 Oak Ave", "Suburb",
                LocalTime.of(11, 30), LocalTime.of(23, 30));
        Restaurant restaurant3 = new Restaurant("r3", "Restaurant 3", "789 Pine St", "Town",
                LocalTime.of(22, 0), LocalTime.of(2, 0));
        Restaurant restaurant4 = new Restaurant("r4", "Restaurant 4", "321 Elm St", "Village",
                LocalTime.of(9, 0), LocalTime.of(17, 0));
        
        testRestaurants.add(restaurant1);
        testRestaurants.add(restaurant2);
        testRestaurants.add(restaurant3);
        testRestaurants.add(restaurant4);
        
        Deal deal1 = new Deal("d1", "r1", 10.0f, true, false, 5);
        Deal deal2 = new Deal("d2", "r1", 15.0f, false, true, 3);
        Deal deal3 = new Deal("d3", "r2", 20.0f, true, false, 10);
        Deal deal4 = new Deal("d4", "r3", 25.0f, true, false, 8);
        
        testDealsByRestaurantId.put("r1", Arrays.asList(deal1, deal2));
        testDealsByRestaurantId.put("r2", Arrays.asList(deal3));
        testDealsByRestaurantId.put("r3", Arrays.asList(deal4));
        testDealsByRestaurantId.put("r4", new ArrayList<>());
        
        setPrivateField(repo, "restaurants", testRestaurants);
        setPrivateField(repo, "dealsByRestaurantId", testDealsByRestaurantId);
        
        return repo;
    }

    private InMemoryRestaurantRepository createRepositoryWithoutApiCall() throws Exception {
        try {
            java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            
            InMemoryRestaurantRepository repo = (InMemoryRestaurantRepository) 
                unsafe.allocateInstance(InMemoryRestaurantRepository.class);
            
            setPrivateField(repo, "restaurants", new ArrayList<>());
            setPrivateField(repo, "dealsByRestaurantId", new HashMap<>());
            return repo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create test repository", e);
        }
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
        setPrivateField(repository, "restaurants", new ArrayList<>());
        
        List<Restaurant> result = repository.findAllRestaurants();
        
        assertTrue(result.isEmpty());
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
        Restaurant allDayRestaurant = new Restaurant("r5", "All Day Restaurant", "999 Test St", "City",
                LocalTime.of(0, 0), LocalTime.of(23, 59));
        List<Restaurant> allOpenRestaurants = Arrays.asList(allDayRestaurant);
        setPrivateField(repository, "restaurants", allOpenRestaurants);
        
        LocalTime queryTime = LocalTime.of(12, 0);
        List<Restaurant> result = repository.findAvailableRestaurantsAt(queryTime);
        
        assertEquals(1, result.size());
        assertEquals("r5", result.get(0).getObjectId());
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
