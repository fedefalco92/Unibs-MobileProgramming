package it.unibs.appwow.models;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Massi on 10/05/2016.
 */
public class Amount {
    private int mUserId;
    private String mFullName;
    private double mAmount;

    public Amount(int userId, String fullname, double amount) {
        this.setUserId(userId);
        this.setFullName(fullname);
        this.setAmount(amount);
    }


    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        this.mFullName = fullName;
    }

    public double getAmount() {
        return mAmount;
    }

    public String getAmountString(){
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(mAmount);
    }

    public void setAmount(double amount) {
        this.mAmount = amount;
    }
}
