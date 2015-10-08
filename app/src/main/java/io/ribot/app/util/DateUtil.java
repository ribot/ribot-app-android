package io.ribot.app.util;

import java.util.Calendar;

public class DateUtil {

    public static boolean isToday(long timeMilliseconds) {
        Calendar calendar = Calendar.getInstance();
        int todayYear = calendar.get(Calendar.YEAR);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendar.setTimeInMillis(timeMilliseconds);
        return calendar.get(Calendar.YEAR) == todayYear &&
                calendar.get(Calendar.MONTH) == todayMonth &&
                calendar.get(Calendar.DAY_OF_MONTH) == todayDay;
    }
}
