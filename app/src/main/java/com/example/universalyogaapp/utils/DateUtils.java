package com.example.universalyogaapp.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DateUtils {

    private static int getDayOfWeekAsInt(String day) {
        switch (day.toLowerCase().trim()) {
            case "sunday": return Calendar.SUNDAY;
            case "monday": return Calendar.MONDAY;
            case "tuesday": return Calendar.TUESDAY;
            case "wednesday": return Calendar.WEDNESDAY;
            case "thursday": return Calendar.THURSDAY;
            case "friday": return Calendar.FRIDAY;
            case "saturday": return Calendar.SATURDAY;
            default: return -1;
        }
    }

    public static String getNextUpcomingDate(String schedule) {
        if (schedule == null || schedule.trim().isEmpty()) {
            return "Not scheduled";
        }

        List<String> scheduledDays = Arrays.asList(schedule.split(","));
        List<Calendar> upcomingDates = new ArrayList<>();
        Calendar today = Calendar.getInstance();

        for (String dayName : scheduledDays) {
            int targetDay = getDayOfWeekAsInt(dayName);
            if (targetDay == -1) continue;

            Calendar nextDate = (Calendar) today.clone();
            int currentDayOfWeek = nextDate.get(Calendar.DAY_OF_WEEK);

            int daysToAdd = targetDay - currentDayOfWeek;
            if (daysToAdd <= 0) {
                daysToAdd += 7;
            }
            nextDate.add(Calendar.DAY_OF_YEAR, daysToAdd);
            upcomingDates.add(nextDate);
        }

        if (upcomingDates.isEmpty()) {
            return "Invalid schedule";
        }

        Collections.sort(upcomingDates);
        Calendar nextUpcomingDate = upcomingDates.get(0);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return sdf.format(nextUpcomingDate.getTime());
    }
} 