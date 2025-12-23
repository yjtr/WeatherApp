package com.example.weatherapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 * 提供日期格式化和解析功能
 */
public class DateUtils {

    /**
     * 获取周几（中文）
     */
    public static String getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        String[] weekDays = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        return weekDays[dayOfWeek];
    }

    /**
     * 格式化日期为 MM/dd 格式
     */
    public static String formatDateMMdd(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * 获取当前日期
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * 格式化分钟级时间（从"2025-12-22T03:00+08:00"格式提取时间）
     * @param fxTime API返回的时间字符串
     * @return "HH:mm" 格式的时间字符串，失败返回空字符串
     */
    public static String formatMinutelyTime(String fxTime) {
        if (fxTime == null || fxTime.isEmpty()) {
            return "";
        }
        try {
            int hourIndex = fxTime.indexOf('T');
            if (hourIndex > 0 && hourIndex + 5 < fxTime.length()) {
                return fxTime.substring(hourIndex + 1, hourIndex + 6); // 返回 "HH:mm" 格式
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "";
    }

    /**
     * 格式化小时时间（从"2025-12-22T03:00+08:00"格式提取小时）
     * @param fxTime API返回的时间字符串
     * @return 12小时制时间格式（如"3am", "12pm"），失败返回"--"
     */
    public static String formatHourlyTime(String fxTime) {
        if (fxTime == null || fxTime.isEmpty()) {
            return "--";
        }
        try {
            int hourIndex = fxTime.indexOf('T');
            if (hourIndex > 0 && hourIndex + 3 < fxTime.length()) {
                String hourStr = fxTime.substring(hourIndex + 1, hourIndex + 3);
                int hour = Integer.parseInt(hourStr);
                if (hour == 0) {
                    return "12am";
                } else if (hour < 12) {
                    return hour + "am";
                } else if (hour == 12) {
                    return "12pm";
                } else {
                    return (hour - 12) + "pm";
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "--";
    }

    /**
     * 格式化日期为 yyyyMMdd 格式
     * @param date 日期对象
     * @return 格式化的日期字符串
     */
    public static String formatDateYYYYMMDD(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return String.format("%04d%02d%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }
}
