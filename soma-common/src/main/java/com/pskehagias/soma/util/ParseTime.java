package com.pskehagias.soma.util;

import java.time.*;

/**
 * Created by pkcyr on 8/9/2016.
 * Utilities for interpreting the play times of scraped playlist data as UTC timestamps.
 */
public class ParseTime {

    /**
     * Parses the time, given in standard "HH:mm:ss" format, to a UTC timestamp in milliseconds past the epoch.
     * This method is a convenience for somascraper which makes the assumption that incoming time strings are PST/PDT
     * and represent a time within the last 24 hours.
     * @param time A string representing a time of day in "HH:mm:ss" format.
     * @return A representation of time in milliseconds past the epoch in UTC
     */
    public static long convertTimeToMillis(String time) {
        return parseFromOffsetToUTCAsToday(time, ZoneId.SHORT_IDS.get("PST")).toInstant().toEpochMilli();
    }

    /**
     * Parses the time, given in standard "HH:mm:ss" format, to an OffsetDateTime representing a day in the last
     * 24 hours and the given timezone.
     * @param time A string representing a time of day in "HH:mm:ss" format.
     * @param zoneId The ZoneId of the timezone to set the result to.
     * @return An OffsetDateTime object representing the requested time string.
     */
    public static OffsetDateTime parseFromOffsetToUTCAsToday(String time, String zoneId){
        LocalTime parsed = LocalTime.parse(time);
        OffsetTime now = OffsetTime.now(ZoneId.of(zoneId));
        OffsetTime offsetTime = parsed.atOffset(now.getOffset());
        OffsetDateTime offsetDateTime = offsetTime.atDate(LocalDate.now());
        if(now.isBefore(offsetTime)){
            offsetDateTime.minusDays(1);
        }
        return offsetDateTime;
    }
}
