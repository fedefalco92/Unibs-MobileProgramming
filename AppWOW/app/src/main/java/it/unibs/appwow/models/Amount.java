package it.unibs.appwow.models;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Massi on 10/05/2016.
 */
public class Amount {
    private static final int MAX_EMAIL_TO_STRING_LENGTH = 10;
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

    public String getFormattedString() {
        return mFullName + " (" + cropEmail() + "): " +  getAmountString() + " eur";
    }

    private String cropEmail(){
        if(mEmail.length()<MAX_EMAIL_TO_STRING_LENGTH) return mEmail;
        /*else {
            return mEmail.substring(0,MAX_EMAIL_TO_STRING_LENGTH/2-1) + "..." + mEmail.substring( mEmail.length() - MAX_EMAIL_TO_STRING_LENGTH/2, mEmail.length());
        }*/

        else {
            String [] splitted = mEmail.split("@");
            return splitted[0] + "@" + splitted[1].substring(0,1) + "...";
        }
    }
}
