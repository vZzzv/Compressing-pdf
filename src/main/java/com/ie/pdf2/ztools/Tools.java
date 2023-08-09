package com.ie.pdf2.ztools;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Tools {

    /*
     * 获取当天的零点时间戳
     */
    public static long getTodayStartTime() {
        //设置时区
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long time = calendar.getTimeInMillis();
        time = time - time % 1000;
        return time;
    }

    // 获取当天 是一周第几天 （ 1，2，3，4，5，6，7）
    public static int getDayWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int week = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        if (week == 0) {
            week = 7;
        }
        return week;
    }

    /*
     * 获取当周的第一天的零点时间戳
     */
    public static long getWeekStartTime() {
        //设置时区  周天是一周第一天，，在中国这个会有点问题
//        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        long time = calendar.getTimeInMillis();
//        time = time -  time % 1000;
//        return time;


        long time = getTodayStartTime();
        int dayweek = getDayWeek();

        long l = time - (1000 * 3600 * 24 * (dayweek - 1));
//        System.out.println(l);
        return l;

    }

    /*
     * 获取当月1号0时时间戳
     */
    public static long getMonthStartTime() {
        //设置时区
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long time = calendar.getTimeInMillis();
        time = time - time % 1000;
        return time;
    }

    /*
     * 获取当年1号0时时间戳
     */
    public static long getYearStartTime() {
        //设置时区
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long time = calendar.getTimeInMillis();
        time = time - time % 1000;
        return time;
    }


}
