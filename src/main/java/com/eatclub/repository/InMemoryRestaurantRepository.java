package com.eatclub.repository;

import com.eatclub.common.Constants;
import com.eatclub.model.Deal;
import com.eatclub.model.Restaurant;
import com.eatclub.model.ec.DealDTO;
import com.eatclub.model.ec.RestaurantsDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryRestaurantRepository implements ILocalRepository {

    private final RestTemplate restTemplate;
    private List<Restaurant> restaurants;
    private Map<String, List<Deal>> dealsByRestaurantId;

    public InMemoryRestaurantRepository(RestTemplate restTemplate) throws Exception {
        this.restTemplate = restTemplate;
        /*
         * TODO: Currently the data is being fetched eagerly. This could be changed to a
         * lazy fetch. Also, to avoid fetching the data multiple times, we do this just
         * once, so we might have stale data. It would be important to have a periodic
         * refresh.
         */
        this.fetchDataFromAPI();
    }

    private void fetchDataFromAPI() throws Exception {
        ResponseEntity<RestaurantsDTO> response = null;
        try {
            response = restTemplate
                    .getForEntity(Constants.EC_API_CHALLENGE_ENDPOINT, RestaurantsDTO.class);
        } catch (Exception e) {
            throw new Exception("Failed to fetch data from API", e);
        }

        if (response == null || response.getBody() == null) {
            throw new Exception("Failed to fetch data from API: Response is null");
        }

        Map<String, List<Deal>> dealsMap = new HashMap<>();

        // Parse restaurants from response body
        List<Restaurant> restaurants = response.getBody().getRestaurants().stream()
                .map(restaurantDTO -> {

                    List<Deal> deals = new ArrayList<>();
                    for (DealDTO dealDTO : restaurantDTO.getDeals()) {
                        Deal deal = new Deal(
                                dealDTO.getObjectId(),
                                restaurantDTO.getObjectId(),
                                Float.parseFloat(dealDTO.getDiscount()),
                                Boolean.parseBoolean(dealDTO.getDineIn()),
                                Boolean.parseBoolean(dealDTO.getLightning()),
                                Integer.parseInt(dealDTO.getQtyLeft()));
                        deals.add(deal);
                    }
                    dealsMap.put(restaurantDTO.getObjectId(), deals);

                    LocalTime openTime;
                    LocalTime closeTime;
                    try {
                        String openStr = restaurantDTO.getOpen().trim().toLowerCase();
                        String closeStr = restaurantDTO.getClose().trim().toLowerCase();
                        openTime = LocalTime.parse(openStr, Constants.H_MM_A_TIME_FORMATTER);
                        closeTime = LocalTime.parse(closeStr, Constants.H_MM_A_TIME_FORMATTER);
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Failed to parse time for restaurant " + restaurantDTO.getObjectId() +
                                        ": open='" + restaurantDTO.getOpen() + "', close='" + restaurantDTO.getClose()
                                        + "'",
                                e);
                    }

                    return new Restaurant(
                            restaurantDTO.getObjectId(),
                            restaurantDTO.getName(),
                            restaurantDTO.getAddress1(),
                            restaurantDTO.getSuburb(),
                            openTime,
                            closeTime);
                })
                .collect(Collectors.toList());

        this.dealsByRestaurantId = dealsMap;
        this.restaurants = restaurants;
    }

    @Override
    public List<Restaurant> findAllRestaurants() {
        return new ArrayList<>(restaurants);
    }

    @Override
    public List<Restaurant> findAvailableRestaurantsAt(LocalTime time) {
        return restaurants.stream()
                .filter(restaurant -> isAvailableAt(restaurant, time))
                .toList();
    }

    @Override
    public List<Deal> findDealsByRestaurantId(String restaurantId) {
        return dealsByRestaurantId.getOrDefault(restaurantId, new ArrayList<>());
    }

    private boolean isAvailableAt(Restaurant restaurant, LocalTime time) {
        LocalTime openTime = restaurant.getOpenTime();
        LocalTime closeTime = restaurant.getCloseTime();

        if (openTime.isBefore(closeTime)) {
            return !time.isBefore(openTime) && !time.isAfter(closeTime);
        } else {
            return !time.isBefore(openTime) || !time.isAfter(closeTime);
        }
    }

}
