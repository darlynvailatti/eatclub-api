package com.eatclub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealAtRestaurant {
    private Restaurant restaurant;
    private Deal deal;
}
