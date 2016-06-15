package it.unibs.appwow.models.ser;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;

import it.unibs.appwow.models.parc.User;

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

    private int mId;
    private String mGroupName;
    private String mPhotoUri;
    private long mCreatedAt;
    private long mUpdatedAt;
    private int mIdAdmin;
    private int mHighlighted;
    private HashMap<Integer, User> mUsers;

    public int getId() {
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

    public int getIdAdmin() {
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

    public void setIdAdmin(int mIdAdmin) {
        this.mIdAdmin = mIdAdmin;
    }

    public int getHighlighted() {
        return mHighlighted;
    }

    public void setHighlighted(int mHighlighted) {
        this.mHighlighted = mHighlighted;
    }


    public static final Parcelable.Creator<Group> CREATOR = new Parcelable.Creator<Group>()
    {
        public Group createFromParcel(Parcel in)
        {
            return new Group(in);
        }

        public Group[] newArray(int size)
        {
            return new Group[size];
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
            this.mIdAdmin = in.readInt();
        }
        if(in.readByte() == PRESENT)
        {
            this.mHighlighted = in.readInt();
        }
        // FIXME: 26/05/2016 LA HashMap con gli utenti viene annullata quando si parcellizza il gruppo
        this.mUsers = new HashMap<Integer, User>();
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

        if(this.mHighlighted!=0)
        {
            dest.writeByte(PRESENT);
            dest.writeInt(this.mHighlighted);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

    }

    public Group(int id, String groupName, String photoUri, long createdAt, long updatedAt, int idAdmin, int highlighted) {
        this.mUsers = new HashMap<Integer, User>();
        this.mId = id;
        this.mGroupName = groupName;
        this.mPhotoUri = photoUri;
        this.mCreatedAt = createdAt;
        this.mUpdatedAt = updatedAt;
        this.mIdAdmin = idAdmin;
        this.mHighlighted = highlighted;
    }

    private Group(String groupName) {
        this.mUsers = new HashMap<Integer, User>();
        this.mId = 0;
        this.mGroupName = groupName;
        this.mPhotoUri = null;
        this.mCreatedAt = 0;
        this.mUpdatedAt = 0;
        this.mIdAdmin = 0;
        this.mHighlighted = 0;
    }

    public static Group create(String groupName){
        final Group g = new Group(groupName);
        return g;
    }

    public Group withId(int id){
        this.mId = id;
        return this;
    }

    public Group withAdmin(int idAdmin){
        this.mIdAdmin = idAdmin;
        return this;
    }

    public Group withPhotoUri(String photoUri){
        this.mPhotoUri = photoUri;
        return this;
    }

    /**
     * Adds a user to mUsers
     * @param user
     * @return true if the user has been successfully added,
     *          false if the user with such ID already exists
     */
    public boolean addUser(User user){
       return (mUsers.put(user.getId(),user) == null);
    }

    public boolean removeUser(User toRemove) {
        return (mUsers.remove(toRemove.getId())!= null);
    }

    public User getUser(int id){
        return mUsers.get(id);
    }

    public int getUsersCount(){
        return mUsers.size();
    }

    public User getAdminUser(){
        return mUsers.get(getIdAdmin());
    }

    public boolean isHighlighted(){
        return mHighlighted > 0;
    }

    public void highlight(){
        setHighlighted(1);
    }
    public void unHighlight(){
        setHighlighted(0);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(mGroupName);
        return builder.toString();
    }


}
