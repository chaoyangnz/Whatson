package me.yangchao.whatson.util;

import me.yangchao.whatson.model.Stats;

/**
 * Created by richard on 5/7/17.
 */

public class MessageFormaterUtil {

    public static String distance(int distance) {
        return distance + " metres";
    }

    public static String walkTime(int distance) {
        int minutes = distance/(90);
        String format = "%d minutes walking estimated";
        if(minutes == 0) format = "less than 1 minute walking estimated";

        return String.format(format, minutes);
    }

    public static String timeRange(String utcStartTime, String utcEndTime) {
        String startTime = DateUtil.formatTime(utcStartTime);
        String endTime = DateUtil.formatTime(utcEndTime);
        endTime = endTime.replace(startTime.substring(0, 11), "");
        return startTime + " ~ " + endTime;
    }

    public static String stats(Stats stats) {
        int attending = stats.getAttending();
        int maybe = stats.getMaybe();
        int invited = stats.getAttending()+stats.getDeclined()+stats.getMaybe()+stats.getNoreply();
        return String.format("%d attending • %d maybe • %d invited", attending, maybe, invited);
    }
}
