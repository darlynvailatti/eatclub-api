package com.eatclub.mapper;

import com.eatclub.common.Constants;
import com.eatclub.model.Deal;
import com.eatclub.model.DealAtRestaurant;
import com.eatclub.model.PeakTimeWindow;
import com.eatclub.model.Restaurant;
import com.eatclub.model.dtos.AvailableRestaurantsDTO;
import com.eatclub.model.dtos.DealDTO;
import com.eatclub.model.dtos.PeakTimeDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestaurantMapper {

    public AvailableRestaurantsDTO toAvailableRestaurantsDTO(List<DealAtRestaurant> dealAtRestaurants) {
        List<DealDTO> dealDTOs = dealAtRestaurants.stream()
                .map(dealAtRestaurant -> {
                    DealDTO dto = new DealDTO();
                    Restaurant restaurant = dealAtRestaurant.getRestaurant();
                    Deal deal = dealAtRestaurant.getDeal();
                    dto.setRestaurantObjectId(restaurant.getObjectId());
                    dto.setRestaurantName(restaurant.getName());
                    dto.setRestaurantAddress1(restaurant.getAddress1());
                    dto.setRestarantSuburb(restaurant.getSuburb());
                    dto.setRestaurantOpen(restaurant.getOpenTime().format(Constants.H_MM_A_TIME_FORMATTER));
                    dto.setRestaurantClose(restaurant.getCloseTime().format(Constants.H_MM_A_TIME_FORMATTER));
                    dto.setObjectId(deal.getObjectId());
                    dto.setDiscount(deal.getDiscount().toString());
                    dto.setDineIn(deal.getDineIn().toString());
                    dto.setLightning(deal.getLightning().toString());
                    dto.setOpen(restaurant.getOpenTime().format(Constants.H_MM_A_TIME_FORMATTER));
                    dto.setClose(restaurant.getCloseTime().format(Constants.H_MM_A_TIME_FORMATTER));
                    dto.setQtyLeft(deal.getQtyLeft().toString());
                    return dto;
                })
                .collect(Collectors.toList());

        return new AvailableRestaurantsDTO(dealDTOs);
    }

    public PeakTimeDTO toPeakTimeDTO(PeakTimeWindow peakTimeWindow) {
        return new PeakTimeDTO(
                peakTimeWindow.getPeakTimeStart().format(Constants.H_MM_A_TIME_FORMATTER),
                peakTimeWindow.getPeakTimeEnd().format(Constants.H_MM_A_TIME_FORMATTER));
    }

}
