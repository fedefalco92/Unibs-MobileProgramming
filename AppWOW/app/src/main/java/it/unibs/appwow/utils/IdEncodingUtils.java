package it.unibs.appwow.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unibs.appwow.database.UserDAO;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.UserModel;

/**
 * Created by Alessandro on 29/06/2016.
 */
public class IdEncodingUtils {

    private static final String AMOUNT_SEPARATOR = "&";
    private static final String INNER_SEPARATOR = "=";
    public static String encodeAmountDetails(HashMap<Integer,Double> amount_details){
        List<Map.Entry<Integer,Double>> entries = new ArrayList<Map.Entry<Integer,Double>>(
                amount_details.entrySet()
        );

        Collections.sort(
                entries
                ,   new Comparator<Map.Entry<Integer,Double>>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    public int compare(Map.Entry<Integer,Double> a, Map.Entry<Integer,Double> b) {
                        return Integer.compare(b.getKey(), a.getKey());
                    }
                }
        );

        StringBuffer sb = new StringBuffer();
        DecimalFormat numberFormat = new DecimalFormat("#.00");

        for (Map.Entry<Integer,Double> e : entries) {

            sb.append(e.getKey() + "=" + numberFormat.format(e.getValue()) + AMOUNT_SEPARATOR );
        }

        String res = sb.toString();

        /*
        Set<Integer> ids = amount_details.keySet();
        Iterator it = ids.iterator();
        while(it.hasNext()){
            int id = (int) it.next();
            double amount = amount_details.get(id);
            res.append(id);
            res.append("=");
            res.append(numberFormat.format(amount));
            if(it.hasNext()){
                res.append(AMOUNT_SEPARATOR);
            }
        }*/
        if(!res.isEmpty()){
            res = res.substring(0, res.length()-1);
            res = res.replace(',','.');
            Log.d("IDENCODINGUTILS", "AMOUNT DETAILS: " + res);
        }

        return res;
    }

    private static HashMap<Integer,Double> decodeAmountDetailsFromString(String ad){
        HashMap<Integer,Double> res = new HashMap<Integer, Double>();
        String[] amountArray = ad.split(AMOUNT_SEPARATOR);
        for(String s: amountArray){
            String [] singleAmount = s.split(INNER_SEPARATOR);
            if(singleAmount.length == 2){
                int id = new Integer(singleAmount[0]);
                double amount = new Double(singleAmount[1]);
                res.put(id, amount);
            }
        }
        return res;
    }

    /**
     * ritorna una lista di Amount in cui il singolo amount è la spesa effettuata dal singolo user
     * @param ad
     * @return
     */
    public static List<Amount> decodeAmountDetails(String ad, int idPagante, double totalAmount){
        List<Amount> res = new ArrayList<Amount>();
        HashMap<Integer,Double> adhash = decodeAmountDetailsFromString(ad);
        UserDAO dao = new UserDAO();
        dao.open();
        Set<Integer> ids = adhash.keySet();
        for(int id:ids){
            String[] info = dao.getSingleUserInfo(id);
            String fullName = info[0];
            String email = info[1];
            //ricalcolo spesa singolo user
            double amountDetail = adhash.get(id);
            double amount = 0;
            if(id == idPagante){
                amount = totalAmount - amountDetail;
            } else {
                amount = -amountDetail;
            }
            Amount a = new Amount(id, fullName, amount, email);
            res.add(a);
        }
        return res;
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
