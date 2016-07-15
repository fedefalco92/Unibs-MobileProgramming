package it.unibs.appwow.models.parc;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Massi on 05/05/2016.
 */
public class GroupModel implements Parcelable {

    public static int HIGHLIGHTED = 1;
    public static int NOT_HIGHLIGHTED = 0;

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
    private String mPhotoFileName;
    private long mPhotoUpdatedAt;
    private long mCreatedAt;
    private long mUpdatedAt;
    private int mIdAdmin;
    private int mHighlighted;
    //private HashMap<Integer, LocalUser> mUsers;

    public int getId() {
        return mId;
    }

    public String getGroupName() {
        return mGroupName;
    }

    public String getPhotoFileName() {
        return mPhotoFileName;
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

    /*
    public HashMap<Integer, LocalUser> getUsers() {
        return mUsers;
    }*/

    public void setGroupName(String mGroupName) {
        this.mGroupName = mGroupName;
    }

    public void setPhotoFileName(String mPhotoUri) {
        this.mPhotoFileName = mPhotoUri;
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

    public long getPhotoUpdatedAt() {
        return mPhotoUpdatedAt;
    }

    public void setPhotoUpdatedAt(long photoUpdatedAt) {
        mPhotoUpdatedAt = photoUpdatedAt;
    }

    public static final Parcelable.Creator<GroupModel> CREATOR = new Parcelable.Creator<GroupModel>()
    {
        public GroupModel createFromParcel(Parcel in)
        {
            return new GroupModel(in);
        }

        public GroupModel[] newArray(int size)
        {
            return new GroupModel[size];
        }
    };

    /**
     * Read from the parcelized object.
     *
     * @param in
     */
    public GroupModel(Parcel in)
    {
        this.mId = in.readInt();
        if(in.readByte() == PRESENT)
        {
            this.mGroupName = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mPhotoFileName = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mPhotoUpdatedAt = in.readLong();
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
        //this.mUsers = new HashMap<Integer, LocalUser>();
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
        if (!TextUtils.isEmpty(this.mGroupName)) {
            dest.writeByte(PRESENT);
            dest.writeString(this.mGroupName);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (!TextUtils.isEmpty(this.mPhotoFileName)) {
            dest.writeByte(PRESENT);
            dest.writeString(this.mPhotoFileName);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (this.mPhotoUpdatedAt != 0) {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mPhotoUpdatedAt);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (this.mCreatedAt != 0) {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mCreatedAt);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (this.mUpdatedAt != 0) {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mUpdatedAt);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (this.mIdAdmin != 0) {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mIdAdmin);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

        if (this.mHighlighted != 0) {
            dest.writeByte(PRESENT);
            dest.writeInt(this.mHighlighted);
        } else {
            dest.writeByte(NOT_PRESENT);
        }

    }

    public GroupModel(int id, String groupName, String photoFileName, long photoUpdatedAt, long createdAt, long updatedAt, int idAdmin, int highlighted) {
        //this.mUsers = new HashMap<Integer, LocalUser>();
        this.mId = id;
        this.mGroupName = groupName;
        this.mPhotoFileName = photoFileName;
        this.mPhotoUpdatedAt =photoUpdatedAt;
        this.mCreatedAt = createdAt;
        this.mUpdatedAt = updatedAt;
        this.mIdAdmin = idAdmin;
        this.mHighlighted = highlighted;
    }

    private GroupModel(String groupName) {
        //this.mUsers = new HashMap<Integer, LocalUser>();
        this.mId = 0;
        this.mGroupName = groupName;
        this.mPhotoFileName = null;
        this.mPhotoUpdatedAt = 0;
        this.mCreatedAt = 0;
        this.mUpdatedAt = 0;
        this.mIdAdmin = 0;
        this.mHighlighted = NOT_HIGHLIGHTED;
    }

    public static GroupModel create(String groupName){
        final GroupModel g = new GroupModel(groupName);
        return g;
    }

    public GroupModel withId(int id){
        this.mId = id;
        return this;
    }

    public GroupModel withAdmin(int idAdmin){
        this.mIdAdmin = idAdmin;
        return this;
    }

    public GroupModel withPhotoFileName(String photoUri){
        this.mPhotoFileName = photoUri;
        return this;
    }

    /**
     * Adds a user to mUsers
     * //@param user
     * //@return true if the user has been successfully added,
     *          false if the user with such ID already exists
     */
    /*
    public boolean addUser(LocalUser user){
       return (mUsers.put(user.getUserId(),user) == null);
    }

    public boolean removeUser(LocalUser toRemove) {
        return (mUsers.removeItem(toRemove.getUserId())!= null);
    }

    public LocalUser getUser(int id){
        return mUsers.get(id);
    }

    public int getUsersCount(){
        return mUsers.size();
    }

    public LocalUser getAdminUser(){
        return mUsers.get(getIdAdmin());
    }*/

    public static GroupModel create(JSONObject gjs) throws JSONException{
        int id = gjs.getInt("id");
        int idAdmin = gjs.getInt("idAdmin");
        String name = gjs.getString("name");
        long photoUpdatedAt = DateUtils.dateStringToLong(gjs.getString("photo_updated_at"));
        long createdAt = DateUtils.dateStringToLong(gjs.getString("created_at"));
        long updatedAt = DateUtils.dateStringToLong(gjs.getString("updated_at"));

        return new GroupModel(id,name, "", photoUpdatedAt,createdAt,updatedAt,idAdmin, 0);

    }

    public boolean isHighlighted(){
        return mHighlighted == HIGHLIGHTED;
    }

    public void highlight(){
        setHighlighted(HIGHLIGHTED);
    }
    public void unHighlight(){
        setHighlighted(NOT_HIGHLIGHTED);
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(mGroupName);
        return builder.toString();
    }
}
