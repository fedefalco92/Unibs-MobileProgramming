package it.unibs.appwow.utils;

/**
 * Created by Alessandro on 23/06/2016.
 */
public class PositionUtils {
    public static String encodePositionId(String id){
        return "###" + id;
    }

    public static String decodePositionId(String codedId){
        return codedId.substring(3, codedId.length());
    }

    public static boolean isPositionId(String position){
        String start = position.substring(0,2);
        if (start.equalsIgnoreCase("###")){
            return true;
        } else {
            return false;
        }
    }
}
