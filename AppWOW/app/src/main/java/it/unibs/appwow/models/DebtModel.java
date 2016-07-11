package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class DebtModel {
    private int mId;
    private int mIdGroup;
    private int mIdFrom;
    private int mIdTo;
    private double mAmount;

    public DebtModel(int id, int idGroup, int idFrom, int idTo, double amount) {
        mId = id;
        mIdGroup = idGroup;
        mIdFrom = idFrom;
        mIdTo = idTo;
        mAmount = amount;
    }

    public static DebtModel create(JSONObject djs) throws JSONException {
        int id = djs.getInt("id");
        int idGroup = djs.getInt("idGroup");
        int idFrom = djs.getInt("idFrom");
        int idTo = djs.getInt("idTo");
        double amount = djs.getDouble("amount");
        return new DebtModel(id, idGroup, idFrom, idTo, amount);
        //return null;
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

    public int getIdGroup() {
        return mIdGroup;
    }

    public void setIdGroup(int idGroup) {
        mIdGroup = idGroup;
    }
}
