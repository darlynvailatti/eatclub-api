package com.eatclub.common;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

public class Constants {
    public static final DateTimeFormatter H_MM_A_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("h:mma")
        .toFormatter(Locale.ENGLISH);
    public static final DateTimeFormatter HH_MM_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
}
