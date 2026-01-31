package com.eatclub.model.ec;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantsDTO {
    private List<RestaurantDTO> restaurants;
}
