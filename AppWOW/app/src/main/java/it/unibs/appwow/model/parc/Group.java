package it.unibs.appwow.model.parc;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Massi on 05/05/2016.
 */
public class Group implements Parcelable {

    private static final byte PRESENT = 1;

    private static final byte NOT_PRESENT = 0;

    private static final String ID_KEY = "id";
    private static final String GROUP_NAME_KEY = "groupName";
    private static final String PHOTO_URI_KEY = "photoUri";
    private static final String CREATED_AT_KEY = "createdAt";
    private static final String UPDATED_AT_KEY = "updatedAt";
    private static final String ID_ADMIN_KEY = "idAdmin";

    private long mId;
    private String mGroupName;
    private String mPhotoUri;
    private long mCreatedAt;
    private long mUpdatedAt;
    private long mIdAdmin;
    private HashMap<Integer, User> mUsers;


    public long getId() {
        return mId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public long getIdAdmin() {
        return mIdAdmin;
    }

    public HashMap<Integer, User> getUsers() {
        return mUsers;
    }

    public void setGroupName(String mGroupName) {
        this.mGroupName = mGroupName;
    }

    public void setPhotoUri(String mPhotoUri) {
        this.mPhotoUri = mPhotoUri;
    }

    public void setCreatedAt(long mCreatedAt) {
        this.mCreatedAt = mCreatedAt;
    }

    public void setUpdatedAt(long mUpdatedAt) {
        this.mUpdatedAt = mUpdatedAt;
    }

    public void setIdAdmin(long mIdAdmin) {
        this.mIdAdmin = mIdAdmin;
    }


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

    /**
     * Read from the parcelized object.
     *
     * @param in
     */
    public Group (Parcel in)
    {
        this.mId = in.readInt();
        if(in.readByte() == PRESENT)
        {
            this.mGroupName = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mPhotoUri = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mCreatedAt = in.readLong();
        }

        if(in.readByte() == PRESENT)
        {
            this.mUpdatedAt = in.readLong();
        }

        if(in.readByte() == PRESENT)
        {
            this.mIdAdmin = in.readLong();
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
        dest.writeLong(this.mId);
        if(!TextUtils.isEmpty(this.mGroupName))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mGroupName);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mPhotoUri))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mPhotoUri);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(this.mCreatedAt!=0)
        {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mCreatedAt);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(this.mUpdatedAt!=0)
        {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mUpdatedAt);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(this.mIdAdmin!=0)
        {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mIdAdmin);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

    }

    public Group(long id, String groupName, String photoUri, long createdAt, long updatedAt, long idAdmin) {
        this.mUsers = new HashMap<Integer, User>();
        this.mId = id;
        this.mGroupName = groupName;
        this.mPhotoUri = photoUri;
        this.mCreatedAt = createdAt;
        this.mUpdatedAt = updatedAt;
        this.mIdAdmin = idAdmin;
    }

    private Group(String groupName) {
        this.mUsers = new HashMap<Integer, User>();
        this.mId = 0;
        this.mGroupName = groupName;
        this.mPhotoUri = null;
        this.mCreatedAt = 0;
        this.mUpdatedAt = 0;
        this.mIdAdmin = 0;
    }

    public static Group create(String groupName){
        final Group g = new Group(groupName);
        return g;
    }

    public Group withAdmin(int idAdmin){
        this.mIdAdmin = idAdmin;
        return this;
    }

    public void addUser(User user){
        mUsers.put(user.getId(),user);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(mGroupName);
        return builder.toString();
    }

}
