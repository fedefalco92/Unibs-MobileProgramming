package it.unibs.appwow.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Alessandro on 03/06/2016.
 */
public class DateUtils {
    public static final String DATE_FORMAT ="yyyy-MM-dd HH:mm:ss";

    public static long dateToLong(String date){
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
}
