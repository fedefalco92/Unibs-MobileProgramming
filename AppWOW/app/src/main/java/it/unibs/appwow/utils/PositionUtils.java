package it.unibs.appwow.utils;

import android.util.Log;

/**
 * Created by Alessandro on 23/06/2016.
 */
public class PositionUtils {
    /**
     * la stringa mPosition è una posizione fittizia tipo "casa mia" oppure un ID di google places preceduto da "###"
     */
    public static String encodePositionId(String id){
        return "###" + id;
    }

    public static String decodePositionId(String codedId){
        String decodedId = codedId.substring(3, codedId.length());
        Log.d("PLACE_ID_DECODING", codedId);
        Log.d("PLACE_ID_DECODED", decodedId);
        return decodedId;
    }

    public static boolean isPositionId(String position){
        if(position==null ||position.isEmpty()) return false;

        String start = position.substring(0,3);
        if (start.equalsIgnoreCase("###")){
            return true;
        } else {
            return false;
        }
    }
}
