package com.eatclub.service;

import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;

import java.time.LocalTime;
import java.util.List;

public interface IRestaurantService {
    List<DealAtRestaurant> getAvailableRestaurantDealsByTime(LocalTime time);
    PeakTimeWindow getPeakTimeWindow();
}
