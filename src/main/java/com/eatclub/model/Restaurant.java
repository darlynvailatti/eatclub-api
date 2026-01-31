package com.eatclub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    private String objectId;
    private String name;
    private String address1;
    private String suburb;
    private LocalTime openTime;
    private LocalTime closeTime;
}
