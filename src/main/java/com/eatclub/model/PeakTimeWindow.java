package com.eatclub.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PeakTimeWindow {
    private LocalTime peakTimeStart;
    private LocalTime peakTimeEnd;
}
