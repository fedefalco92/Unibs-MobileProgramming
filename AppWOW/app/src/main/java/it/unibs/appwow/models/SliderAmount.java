package it.unibs.appwow.models;

import android.support.v7.widget.AppCompatSeekBar;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Alessandro on 06/07/2016.
 */
public class SliderAmount {
    private int mUserId;
    private String mFullName;
    private double mAmount;
    private String mEmail;
    private EditText mAmountText;
    private AppCompatSeekBar mSeekBar;

    public SliderAmount(int userId, String fullname, double amount, String email) {
        this.setUserId(userId);
        this.setFullName(fullname);
        this.setAmount(amount);
        this.setEmail(email);
    }

    public SliderAmount(int userId, String fullname, String email) {
        this.setUserId(userId);
        this.setFullName(fullname);
        this.setAmount(0);
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

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
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

    public EditText getAmountView() {
        return mAmountText;
    }

    public void setAmountView(EditText amountText) {
        mAmountText = amountText;
    }

    public AppCompatSeekBar getSeekBar() {
        return mSeekBar;
    }

    public void setSeekBar(AppCompatSeekBar seekBar) {
        mSeekBar = seekBar;
    }

    @Override
    public int hashCode() {
        return mUserId;
    }

    @Override
    public boolean equals(Object o) {
        SliderAmount os = (SliderAmount) o;
        return(os.hashCode() == hashCode());
    }
}
