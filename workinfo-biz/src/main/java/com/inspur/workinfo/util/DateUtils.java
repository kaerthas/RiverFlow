package com.inspur.workinfo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author : Jason
 * @date : 2020/7/14 13:37
 * @description :
 */
public class DateUtils {
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * @description:根据格式化字符串化格式化日期
     * @param formatStr
     *            格式化字符串 date 要格式化的时间
     * @return 经过格式化的date
     * @throws ParseException
     */
    public static Date formatDate(String formatStr, Date date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.parse(format.format(date));

    }
    /**
     * @description:根据格式化字符串化将字符串转换为日期
     * @param formatStr
     *            格式化字符串 date 要格式化的时间
     * @return date
     * @throws ParseException
     */
    public static Date formatDate(String formatStr, String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.parse(date);

    }
}

