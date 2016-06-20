package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

import it.unibs.appwow.models.parc.LocalUser;
import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class UserModel {
    private int mId;
    private String mFullName;
    private String mEmail;
    private long mCreatedAt;
    private long mUpdatedAt;

    //private double mAmount;

    public UserModel(int id, String fullName, String email, long createdAt, long updatedAt) {
        this.mId = id;
        this.mFullName = fullName;
        this.mEmail = email;
        this.mCreatedAt = createdAt;
        this.mUpdatedAt = updatedAt;
        //this.mAmount = 0;
    }
/*
    public UserModel(int id, String fullName, String email, long createdAt, long updatedAt, double amount) {
        this.mId = id;
        this.mFullName = fullName;
        this.mEmail = email;
        this.mCreatedAt = createdAt;
        this.mUpdatedAt = updatedAt;
        this.mAmount = amount;
    }*/

    private UserModel (int id){
        this.mId = id;
    }

    public static UserModel create(JSONObject ujs) throws JSONException{
        int id = ujs.getInt("id");
        String fullName = ujs.getString("fullName");
        String email = ujs.getString("email");
        long createdAt = DateUtils.dateStringToLong(ujs.getString("created_at"));
        long updatedAt = DateUtils.dateStringToLong(ujs.getString("updated_at"));
        return new UserModel(id, fullName,email,createdAt,updatedAt);
    }

    public static UserModel create(int id)
    {
        final UserModel user = new UserModel(id);
        return user;
    }

    public UserModel withFullName(String newFullName){
        this.mFullName = newFullName;
        return this;
    }

    public UserModel withEmail(String newEmail){
        this.mEmail = newEmail;
        return this;
    }

    public UserModel withCreatedAt(long createdAt){
        this.mCreatedAt = createdAt;
        return this;
    }

    public UserModel withUpdatedAt(long updatedAt){
        this.mUpdatedAt = updatedAt;
        return this;
    }
    /*
    public UserModel withAmount(long amount){
        this.mAmount = amount;
        return this;
    }*/

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String mFullName) {
        this.mFullName = mFullName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(long mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(long mUpdatedAt) {
        this.mUpdatedAt = mUpdatedAt;
    }
/*
    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double mAmount) {
        this.mAmount = mAmount;
    }*/

    public static UserModel create(LocalUser user) {
        return UserModel.create(user.getId()).withFullName(user.getFullName()).withEmail(user.getEmail());
    }

    @Override
    public int hashCode() {
        return mId;
    }
}
