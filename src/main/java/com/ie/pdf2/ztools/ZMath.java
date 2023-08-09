package com.ie.pdf2.ztools;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZMath {

    // 判断字符串能否转数字
    public static boolean isDigital(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }

    // long to String
    public static String longToString(long _long) {
        return Long.toString(_long);
    }

    // String to long
    public static long stringToLong(String _str) {
        return Long.parseLong(_str);
    }

    // int to String
    public static String intToString(int _int) {
        return String.valueOf(_int);
    }

    // String to int
    public static int stringToInt(String _str) {
        return Integer.parseInt(_str);
    }

    // Float to String
    public static String floatToString(Float _float) {
        return String.valueOf(_float);
    }

    // String to Float
    public static Float stringToFloat(String _str) {
        return Float.parseFloat(_str);
    }

    // list to Array
    public static <T> String[] listToArray(List<T> list) {
        String[] array = new String[list.size()];
        for (int i = 0; i < array.length; i++) array[i] = list.get(i).toString();
        return array;
    }

    // 判断字符串，是否是数字
    public static boolean isNumber(String str) {
        if (str == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    // array拼接 String；
    public static String arrayToString(long[] arr, String separator) {

        if (arr == null || arr.length < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    // list拼接 String；
    public static String listToString(List list, String separator) {
        if (list == null || list.size() < 1) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    // 随机生成数字
    public static String random(int min, int max) {
        int ran = (int) (Math.random() * (max - min) + min);
        return intToString(ran);
    }

    // 随机生成数字
    public static int random2(int min, int max) {
        int ran = (int) (Math.random() * (max - min) + min);
        return ran;
    }

    public static long random2(long min, long max) {
        long ran = (long) (Math.random() * (max - min) + min);
        return ran;
    }

    // 时间字符转时间戳
    public static long strToDateLong(String strDate) {

        // 创建日期时间对象格式化器，日期格式类似： 2020-02-23 22:18:38
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 时间格式字符串
        // String sDate="2020-02-23 22:18:38";
        // 将时间格式字符串转化为LocalDateTime对象，需传入日期对象格式化器
        LocalDateTime parseDate = LocalDateTime.parse(strDate, formatter);
        long milliSecond = parseDate.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        return milliSecond;
    }

    // 获取某月的最后一天
    public static int getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        // 设置年份
        cal.set(Calendar.YEAR, year);
        // 设置月份
        cal.set(Calendar.MONTH, month - 1);
        // 获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // 设置日历中月份的最大天数
        cal.set(Calendar.DAY_OF_MONTH, lastDay);
        // 格式化日期
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String lastDayOfMonth = sdf.format(cal.getTime());

        return ZMath.stringToInt(lastDayOfMonth);
    }
}
