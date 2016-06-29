package it.unibs.appwow.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Alessandro on 29/06/2016.
 */
public class AmountDetailsUtils {

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
}
