package com.eatclub.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealDTO {

    /*
     * TODO: This would be better as a RestaurantDTO instead,
     * but the API contract is required to have these fields.
     */
    private String restaurantObjectId;
    private String restaurantName;
    private String restaurantAddress1;
    private String restarantSuburb;
    private String restaurantOpen;
    private String restaurantClose;

    private String objectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String open;
    private String close;
    private String qtyLeft;
}
