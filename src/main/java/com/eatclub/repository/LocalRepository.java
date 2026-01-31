package com.eatclub.repository;

import com.eatclub.model.Deal;
import com.eatclub.model.Restaurant;
import java.time.LocalTime;
import java.util.List;

public interface LocalRepository {
    List<Restaurant> findAllRestaurants();
    List<Restaurant> findAvailableRestaurantsAt(LocalTime time);
    List<Deal> findDealsByRestaurantId(String restaurantId);
}
