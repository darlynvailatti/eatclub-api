package com.eatclub.service;

import com.eatclub.model.Deal;
import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.Restaurant;
import com.eatclub.repository.LocalRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final LocalRepository restaurantRepository;

    public RestaurantServiceImpl(LocalRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Override
    public List<DealAtRestaurant> getAvailableRestaurantDealsByTime(LocalTime time) {
        /*
         * This is the implementation of the available restaurant deals by time algorithm.
         * Finds all the restaurants that are open at the given time and returns the deals for those restaurants.
        */
        List<Restaurant> availableRestaurants = restaurantRepository.findAvailableRestaurantsAt(time);
        List<DealAtRestaurant> dealAtRestaurants = new ArrayList<>();
        for (Restaurant restaurant : availableRestaurants) {
            List<Deal> deals = restaurantRepository.findDealsByRestaurantId(restaurant.getObjectId());
            for (Deal deal : deals) {
                dealAtRestaurants.add(new DealAtRestaurant(restaurant, deal));
            }
        }
        return dealAtRestaurants;
    }

    @Override
    public PeakTimeWindow getPeakTimeWindow() {
        /*
         * This is the implementation of the peak time window algorithm.
         * Iterates over all the restaurants and count the number of available deals in a time window.
         * Uses a fixed bucket size of 3 hours.
         * Returns the time window with the most available deals.
         */
        List<Restaurant> allRestaurants = restaurantRepository.findAllRestaurants();
        
        if (allRestaurants.isEmpty()) {
            return new PeakTimeWindow(LocalTime.MIN, LocalTime.MAX);
        }

        int timeWindowFixedBucketSize = 3;
        int maxAvailableDeals = 0;
        LocalTime peakStart = LocalTime.MIN;
        LocalTime peakEnd = LocalTime.of(timeWindowFixedBucketSize, 0);

        for (int hour = 0; hour < 24; hour += timeWindowFixedBucketSize) {
            LocalTime bucketStart = LocalTime.of(hour, 0);
            LocalTime bucketEnd = (hour + timeWindowFixedBucketSize == 24) ? LocalTime.MAX : LocalTime.of(hour + timeWindowFixedBucketSize, 0);
            
            int availableDealsCount = countAvailableDealsInTimeWindow(allRestaurants, bucketStart, bucketEnd);
            
            if (availableDealsCount > maxAvailableDeals) {
                maxAvailableDeals = availableDealsCount;
                peakStart = bucketStart;
                peakEnd = bucketEnd;
            }
        }

        return new PeakTimeWindow(peakStart, peakEnd);
    }

    private int countAvailableDealsInTimeWindow(List<Restaurant> restaurants, LocalTime timeWindowStart, LocalTime timeWindowEnd) {
        int totalDeals = 0;
        for (Restaurant restaurant : restaurants) {
            if (isRestaurantOpenDuringTimeWindow(restaurant, timeWindowStart, timeWindowEnd)) {
                List<Deal> deals = restaurantRepository.findDealsByRestaurantId(restaurant.getObjectId());
                totalDeals += deals.size();
            }
        }
        return totalDeals;
    }

    private boolean isRestaurantOpenDuringTimeWindow(Restaurant restaurant, LocalTime timeWindowStart, LocalTime timeWindowEnd) {
        LocalTime openTime = restaurant.getOpenTime();
        LocalTime closeTime = restaurant.getCloseTime();
        
        if (openTime.isBefore(closeTime)) {
            return !timeWindowEnd.isBefore(openTime) && !timeWindowStart.isAfter(closeTime);
        } else {
            return !timeWindowEnd.isBefore(openTime) || !timeWindowStart.isAfter(closeTime);
        }
    }
}
