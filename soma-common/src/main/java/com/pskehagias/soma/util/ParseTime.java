package com.pskehagias.soma.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by pkcyr on 8/9/2016.
 */
public class ParseTime {
    static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

    public static long convertTimeToMillis(String time) {
        Calendar now = Calendar.getInstance();
        try {
            Date playtime = df.parse(time);
            if(now.get(Calendar.HOUR_OF_DAY) < playtime.getHours()) {
                now.set(Calendar.DATE, now.get(Calendar.DATE)-1);
            }
            now.set(Calendar.HOUR_OF_DAY, playtime.getHours());
            now.set(Calendar.MINUTE, playtime.getMinutes());
            now.set(Calendar.SECOND, playtime.getSeconds());
            now.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return now.getTimeInMillis();
    }
}
