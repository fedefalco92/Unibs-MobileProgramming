package it.unibs.appwow.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

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

    public static final Parcelable.Creator<Payment> CREATOR = new Parcelable.Creator<Payment>()
    {
        public Payment createFromParcel(Parcel in)
        {
            return new Payment(in);
        }

        public Payment[] newArray(int size)
        {
            return new Payment[size];
        }
    };

    public Payment(Parcel in) {
        super(in);
        if(in.readByte() == super.PRESENT){
            this.mFullName = in.readString();
        }
        if(in.readByte() == super.PRESENT){
            this.mEmail = in.readString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        if(!TextUtils.isEmpty(this.mFullName))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mFullName);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mEmail))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mEmail);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }
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
