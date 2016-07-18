package it.unibs.appwow.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alessandro on 03/06/2016.
 */
public class DateUtils {

    private static final String TAG_LOG = DateUtils.class.getSimpleName();

    public static final String DATE_FORMAT ="yyyy-MM-dd HH:mm:ss";
    public static final String DATE_HOUR_FORMAT ="dd/MM/yyyy HH:mm";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String SIMPLE_TIME_FORMAT = "HH:mm";

    public static final String DATE_READABLE_FORMAT = "MMM, dd";

    public static long dateStringToLong(String date){
        if(date.equalsIgnoreCase("null")) return 0L;
        SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT);
        Date d = null;
        try {
            d = f.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
        return d.getTime(); //ritorna i millisecondi
    }

    public static String dateLongToString(Long date){
        return new SimpleDateFormat(DATE_HOUR_FORMAT).format(date);
    }

    public static String dateReadableLongToString(Long date){
        return new SimpleDateFormat(DATE_READABLE_FORMAT).format(date);
    }

    public static String longToSimpleDateString(Long date){
        return new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(date);
    }

    public static String longToSimpleTimeString(Long date){
        return new SimpleDateFormat(SIMPLE_TIME_FORMAT).format(date);
    }

    public static String formatSimpleDate(int year, int month, int day){
        return padWithZeros(day) + "/" + padWithZeros(month) + "/" + year;
    }

    public static String formatSimpleTime(int hour, int minutes){
        return padWithZeros(hour) + ":" + padWithZeros(minutes);
    }

    private static String padWithZeros(int num){
        return String.format("%02d", num);
    }
}
