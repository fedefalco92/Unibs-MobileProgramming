package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class TransactionModel {
    private int mId;
    private int mIdBalancing;
    private int mIdFrom;
    private int mIdTo;
    private double mAmount;
    private long mPayedAt;

    public TransactionModel(int id, int idBalancing, int idFrom, int idTo, double amount, long payedAt) {
        mId = id;
        mIdBalancing = idBalancing;
        mIdFrom = idFrom;
        mIdTo = idTo;
        mAmount = amount;
        mPayedAt = payedAt;
    }

    public static TransactionModel create(JSONObject tjs) throws JSONException {
        // FIXME: 15/06/2016  ASPETTARE CHE FEDE FACCIA LE API
        int id = tjs.getInt("id");
        int idBalancing = tjs.getInt("idBalancing");
        int idFrom = tjs.getInt("idFrom");
        int idTo = tjs.getInt("idTo");
        double amount = tjs.getDouble("amount");
        long payedAt = tjs.getLong("payed_at");
        //return new TransactionModel(id, idBalancing, idFrom, idTo, amount, payedAt);
        return null;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getIdFrom() {
        return mIdFrom;
    }

    public void setIdFrom(int idFrom) {
        mIdFrom = idFrom;
    }

    public int getIdTo() {
        return mIdTo;
    }

    public void setIdTo(int idTo) {
        mIdTo = idTo;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public long getPayedAt() {
        return mPayedAt;
    }

    public void setPayedAt(long payedAt) {
        mPayedAt = payedAt;
    }

    public int getIdBalancing() {
        return mIdBalancing;
    }

    public void setIdBalancing(int idBalancing) {
        mIdBalancing = idBalancing;
    }
}
