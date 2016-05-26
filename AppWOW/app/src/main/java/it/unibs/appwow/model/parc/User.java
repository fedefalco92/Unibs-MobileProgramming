package it.unibs.appwow.model.parc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Alessandro on 18/05/2016.
 */
public class User implements Parcelable{

    private static final byte PRESENT = 1;

    private static final byte NOT_PRESENT = 0;

    private static final String USER_MODEL_PREFERENCES = "User_prefs";
    private static final String ID_KEY = "id";
    private static final String FULLNAME_KEY = "fullname";
    private static final String EMAIL_KEY = "email";

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>()
    {
        public User createFromParcel(Parcel in)
        {
            return new User(in);
        }

        public User[] newArray(int size)
        {
            return new User[size];
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

    private boolean mAdmin;

    /**
     * Read from the parcelized object.
     *
     * @param in
     */
    public User(Parcel in)
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

    public User(String email, String password){
        this.mEmail = email;
        this.mPassword = password;
        this.mAdmin = false;
    }

    private User(int id){
        this.mId = id;
    }

    public static User create(int id)
    {
        final User User = new User(id);
        return User;
    }

    public User withPassword(String newPassword)
    {
        this.mPassword = newPassword;
        return this;
    }

    public User withFullName(String newFullName){
        this.mFullName = newFullName;
        return this;
    }

    public User withEmail(String newEmail)
    {
        this.mEmail = newEmail;
        return this;
    }


    public boolean ismAdmin() {
        return mAdmin;
    }

    public void setmAdmin(){
        mAdmin = true;
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

    public static User load(final Context ctx) {
        final SharedPreferences prefs = ctx.getSharedPreferences(USER_MODEL_PREFERENCES,
                Context.MODE_PRIVATE);
        int id = prefs.getInt(ID_KEY,0);
        User user = null;
        if(id != 0) {
            user = user.create(id).withEmail(prefs.getString(EMAIL_KEY, null)).withFullName(prefs.getString(FULLNAME_KEY, null));
            return user;
        }
        return user;
    }

    public void logout(final Context ctx) {
        ctx.getSharedPreferences(USER_MODEL_PREFERENCES,
                Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Override
    public String toString(){
        if(!mAdmin){
            return "Fullname: " + mFullName + ", Email: " + mEmail;
        }
        else{
            return "Fullname: " + mFullName + ", Email: " + mEmail + " ADMIN";
        }

    }

}
