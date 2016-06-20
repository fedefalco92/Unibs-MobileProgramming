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

    public static long dateStringToLong(String date){
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
}
