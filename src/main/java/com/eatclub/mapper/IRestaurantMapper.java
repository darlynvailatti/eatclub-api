package com.eatclub.mapper;

import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.dtos.AvailableRestaurantsDTO;
import com.eatclub.model.dtos.PeakTimeDTO;

import java.util.List;

public interface IRestaurantMapper {
    AvailableRestaurantsDTO toAvailableRestaurantsDTO(List<DealAtRestaurant> dealAtRestaurants);
    PeakTimeDTO toPeakTimeDTO(PeakTimeWindow peakTimeWindow);
}
