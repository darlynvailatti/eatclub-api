package com.eatclub.controller;

import com.eatclub.common.Constants;
import com.eatclub.mapper.IRestaurantMapper;
import com.eatclub.model.dtos.AvailableRestaurantsDTO;
import com.eatclub.model.dtos.ErrorDTO;
import com.eatclub.model.dtos.PeakTimeDTO;
import com.eatclub.service.IRestaurantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private final IRestaurantService restaurantService;
    private final IRestaurantMapper restaurantMapper;

    public RestaurantController(IRestaurantService restaurantService, IRestaurantMapper restaurantMapper) {
        this.restaurantService = restaurantService;
        this.restaurantMapper = restaurantMapper;
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRestaurants(@RequestParam String timeOfDay) {
        try {
            LocalTime parsedTime = LocalTime.parse(timeOfDay, Constants.HH_MM_TIME_FORMATTER);
            var restaurantDeals = restaurantService.getAvailableRestaurantDealsByTime(parsedTime);
            AvailableRestaurantsDTO availableRestaurants = restaurantMapper.toAvailableRestaurantsDTO(restaurantDeals);
            return ResponseEntity.ok(availableRestaurants);
        } catch (DateTimeParseException e) {
            String errorMessage = String.format(
                    "Invalid time format: '%s'. Expected format is HH:mm (e.g., '14:30' or '09:00'). Please provide a valid time in 24-hour format.",
                    timeOfDay);
            ErrorDTO error = new ErrorDTO(errorMessage, "INVALID_TIME_FORMAT");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            ErrorDTO error = new ErrorDTO("An unexpected error occurred: " + e.getMessage(), "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/peak-time")
    public ResponseEntity<PeakTimeDTO> getPeakTimeWindow() {
        var peakTimeWindow = restaurantService.getPeakTimeWindow();
        PeakTimeDTO peakTime = restaurantMapper.toPeakTimeDTO(peakTimeWindow);
        return ResponseEntity.ok(peakTime);
    }
}
