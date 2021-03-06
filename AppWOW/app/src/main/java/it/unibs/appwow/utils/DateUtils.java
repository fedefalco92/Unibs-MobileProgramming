package it.unibs.appwow.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import it.unibs.appwow.MyApplication;

/**
 * Created by Alessandro on 03/06/2016.
 */
public class DateUtils {

    private static final String TAG_LOG = DateUtils.class.getSimpleName();

    public static final String DATE_FORMAT ="yyyy-MM-dd HH:mm:ss";
    public static final String DATE_HOUR_FORMAT ="dd/MM/yyyy HH:mm";
    public static final String DATE_HOUR_FORMAT_ENG ="MM/dd/yyyy HH:mm";
    public static final String SIMPLE_DATE_FORMAT = "dd/MM/yyyy";
    public static final String SIMPLE_DATE_FORMAT_ENG = "MM/dd/yyyy";
    public static final String SIMPLE_TIME_FORMAT = "HH:mm";

    public static final String DATE_READABLE_FORMAT = "MMM dd";
    public static final String DATE_READABLE_FORMAT_IT = "dd MMM";

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
        SimpleDateFormat dateformat = (SimpleDateFormat) android.text.format.DateFormat.getDateFormat(MyApplication.getAppContext());
        SimpleDateFormat timeformat = (SimpleDateFormat) android.text.format.DateFormat.getTimeFormat(MyApplication.getAppContext());
        //return new SimpleDateFormat(DATE_HOUR_FORMAT).format(date);
        return  dateformat.format(date) + " " + timeformat.format(date);
    }

    public static String dateReadableLongToString(Long date){
        if(Locale.getDefault().getLanguage().equalsIgnoreCase("it")){
            return new SimpleDateFormat(DATE_READABLE_FORMAT_IT).format(date);
        }
        return new SimpleDateFormat(DATE_READABLE_FORMAT).format(date);
    }

    public static String longToSimpleDateString(Long date){
        return new SimpleDateFormat(SIMPLE_DATE_FORMAT).format(date);
    }

    public static String longToSimpleTimeString(Long date){
        return new SimpleDateFormat(SIMPLE_TIME_FORMAT).format(date);
    }

    public static String formatSimpleDate(int year, int month, int day){
        return padWithZeros(day) + "/" + padWithZeros(month +1) + "/" + year;
    }

    public static String formatSimpleTime(int hour, int minutes){
        return padWithZeros(hour) + ":" + padWithZeros(minutes);
    }

    private static String padWithZeros(int num){
        return String.format("%02d", num);
    }


    public static long buildDateLong(String date_string, String time_string) {
        long date = 0L;
        try {
            String[] date_splitted = date_string.split("/");
            String[] time_splitted = time_string.split(":");

            int day = Integer.parseInt(date_splitted[0]);
            int month = Integer.parseInt(date_splitted[1]);
            int year = Integer.parseInt(date_splitted[2]);
            int hourOfDay = Integer.parseInt(time_splitted[0]);
            int minOfDay = Integer.parseInt(time_splitted[1]);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR,year);
            cal.set(Calendar.MONTH, month-1); //MONTH VA DA 0 A 11
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minOfDay);
            date =  cal.getTimeInMillis();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG_LOG, "error in building date");
        }
        Log.d(TAG_LOG, "BUILDED DATE: " + DateUtils.dateLongToString(date));
        return date;
    }
}
