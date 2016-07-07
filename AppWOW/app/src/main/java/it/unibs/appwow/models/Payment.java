package it.unibs.appwow.models;

import android.os.Parcel;

import it.unibs.appwow.models.parc.PaymentModel;

/**
 * Created by Alessandro on 07/07/2016.
 */
public class Payment extends PaymentModel {

    private String mFullName;
    private String mEmail;

    public Payment(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, String position, String position_id, String amountDetails) {
        super(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, position, position_id, amountDetails);
    }

    public Payment(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, String position, String position_id, String amountDetails, boolean isExchange) {
        super(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, position, position_id, amountDetails, isExchange);
    }

    public Payment(int id, int idGroup, int idUser, String fullName, String email, double amount, String name, String notes, long createdAt, long updatedAt, String position, String position_id, String amountDetails, boolean isExchange) {
        super(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, position, position_id, amountDetails, isExchange);
        this.mFullName = fullName;
        this.mEmail = email;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public boolean hasDetails(){
        if(!super.getNotes().isEmpty()||!super.getPosition().isEmpty()) return true; else return false;
    }
}
