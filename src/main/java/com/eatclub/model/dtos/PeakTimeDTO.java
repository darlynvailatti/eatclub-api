package com.eatclub.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeakTimeDTO {
    private String peakTimeStart;
    private String peakTimeEnd;
}
