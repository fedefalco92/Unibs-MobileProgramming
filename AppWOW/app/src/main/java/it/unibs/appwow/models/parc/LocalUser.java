package it.unibs.appwow.models.parc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Alessandro on 18/05/2016.
 */
public class LocalUser implements Parcelable{

    private static final byte PRESENT = 1;

    private static final byte NOT_PRESENT = 0;

    private static final String USER_MODEL_PREFERENCES = "User_prefs";
    private static final String ID_KEY = "id";
    private static final String FULLNAME_KEY = "fullname";
    private static final String EMAIL_KEY = "email";

    public static final Parcelable.Creator<LocalUser> CREATOR = new Parcelable.Creator<LocalUser>()
    {
        public LocalUser createFromParcel(Parcel in)
        {
            return new LocalUser(in);
        }

        public LocalUser[] newArray(int size)
        {
            return new LocalUser[size];
        }
    };

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    private int mId;
    private String mFullName;
    private String mPassword;
    private String mEmail;

    //added to handle the case of an administrator (of any group...)

    private boolean mIsGroupAdmin;

    /**
     * Read from the parcelized object.
     *
     * @param in
     */
    public LocalUser(Parcel in)
    {
        this.mId = in.readInt();
        if(in.readByte() == PRESENT)
        {
            this.mFullName = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mPassword = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mEmail = in.readString();
        }
    }

    @Override
    public int describeContents()
    {
        // in case of reference to a file descriptor: return Parcel.CONTENTS_FILE_DESCRIPTOR;
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.mId);
        if(!TextUtils.isEmpty(this.mFullName))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mFullName);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mPassword))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mPassword);
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

    public LocalUser(String email, String password){
        this.mEmail = email;
        this.mPassword = password;
        this.mIsGroupAdmin = false;
    }

    private LocalUser(int id){
        this.mId = id;
        this.mIsGroupAdmin = false;
    }

    public static LocalUser create(int id)
    {
        final LocalUser LocalUser = new LocalUser(id);
        return LocalUser;
    }

    public LocalUser withPassword(String newPassword)
    {
        this.mPassword = newPassword;
        return this;
    }

    public LocalUser withFullName(String newFullName){
        this.mFullName = newFullName;
        return this;
    }

    public LocalUser withEmail(String newEmail)
    {
        this.mEmail = newEmail;
        return this;
    }


    public boolean isGroupAdmin() {
        return mIsGroupAdmin;
    }

    public void setIsGroupAdmin(){
        mIsGroupAdmin = true;
    }

    public boolean isLogged(){
        return !TextUtils.isEmpty(this.mFullName);
    }

    public String getFullName(){
        return this.mFullName;
    }

    public String getPassword(){
        return this.mPassword;
    }

    public String getEmail(){
        return this.mEmail;
    }

    public void save(final Context ctx) {
        final SharedPreferences prefs = ctx.getSharedPreferences(USER_MODEL_PREFERENCES,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(ID_KEY, mId);
        editor.putString(FULLNAME_KEY, mFullName);
        editor.putString(EMAIL_KEY, mEmail);
        editor.commit();
    }

    public static LocalUser load(final Context ctx) {
        final SharedPreferences prefs = ctx.getSharedPreferences(USER_MODEL_PREFERENCES,
                Context.MODE_PRIVATE);
        int id = prefs.getInt(ID_KEY,0);
        LocalUser localUser = null;
        if(id != 0) {
            localUser = localUser.create(id).withEmail(prefs.getString(EMAIL_KEY, null)).withFullName(prefs.getString(FULLNAME_KEY, null));
            return localUser;
        }
        return localUser;
    }

    public void logout(final Context ctx) {
        ctx.getSharedPreferences(USER_MODEL_PREFERENCES,
                Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Override
    public String toString(){
        //if(!mAdmin){
            return "Fullname: " + mFullName + ", Email: " + mEmail;
        //}
        //else{
        //    return "Fullname: " + mFullName + ", Email: " + mEmail + " ADMIN";
        //}
    }

    /*@Override
    public boolean equals(Object u){
       return (((LocalUser) u).getId()== mId);
    }*/

    @Override
    public int hashCode(){
        return mId;
    }

}
