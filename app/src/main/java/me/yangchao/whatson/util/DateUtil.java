package me.yangchao.whatson.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by richard on 4/11/17.
 */

public class DateUtil {

    public static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String formatTime(String utcTime) {
        if(utcTime == null) return "";
        SimpleDateFormat incomingFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date = null;
        try {
            date = incomingFormat.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
    }

    public static String formatDuration(long milliseconds) {
        long total_seconds = milliseconds / 1000;
        long minutes = total_seconds/60;
        long seconds = total_seconds % 60;

        long hours = 0L;
        if(minutes >= 60) {
            hours = minutes / 60;
            minutes = minutes % 60;
        }

        return (hours > 0 ? hours + ":" : "" ) + minutes + ":" + seconds;
    }
}
