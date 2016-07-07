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
    private String mEmail;

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public Amount(int userId, String fullname, double amount, String email) {
        this.setUserId(userId);
        this.setFullName(fullname);
        this.setAmount(amount);
        this.setEmail(email);
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

    public static String getAmountString(double amount){
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(amount);
    }

    public void setAmount(double amount) {
        this.mAmount = amount;
    }

    @Override
    public String toString() {
        // FIXME: 07/07/2016  OCIO ALLA VALUTA
        return mFullName + " (" + mEmail + "): " +  getAmountString() + " eur";
    }
}
