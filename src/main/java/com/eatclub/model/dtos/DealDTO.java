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

    /*
     * TODO: The API contract specified on the challenge description uses String
     * values. This is not ideal, as it would be better to have the deal fields as
     * primitive types. It would be important to check with the API contract owner
     * to see if this can be changed.
     * e.g:
     * private String objectId;
     * private Float discount;
     * private Boolean dineIn;
     * private Boolean lightning;
     * private Integer qtyLeft;
     */
    private String objectId;
    private String discount;
    private String dineIn;
    private String lightning;
    private String open;
    private String close;
    private String qtyLeft;
}
