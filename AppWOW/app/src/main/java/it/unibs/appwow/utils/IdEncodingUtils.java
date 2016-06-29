package it.unibs.appwow.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import it.unibs.appwow.models.UserModel;

/**
 * Created by Alessandro on 29/06/2016.
 */
public class IdEncodingUtils {

    public static String encodeAmountDetails(HashMap<Integer,Double> amount_details){
        StringBuffer res = new StringBuffer();
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        Set<Integer> ids = amount_details.keySet();
        Iterator it = ids.iterator();
        while(it.hasNext()){
            int id = (int) it.next();
            double amount = amount_details.get(id);
            res.append(id);
            res.append("=");
            res.append(numberFormat.format(amount));
            if(it.hasNext()){
                res.append("&");
            }
        }
        return res.toString();
    }

    public static String encodeIds(List<UserModel> users){
        StringBuffer b = new StringBuffer();
        for(UserModel u:users){
            b.append(u.getId());
            b.append("&");
        }
        String res = b.toString();
        int l = res.length();
        return res.substring(0,l-1);
    }
}
