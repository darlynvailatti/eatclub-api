package com.eatclub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deal {
    private String restaurantId;
    private Float discount;
    private Boolean dineIn;
    private Boolean lightning;
    private Integer qtyLeft;
}
