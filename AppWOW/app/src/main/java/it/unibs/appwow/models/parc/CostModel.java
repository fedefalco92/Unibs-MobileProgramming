package it.unibs.appwow.models.parc;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class CostModel implements Parcelable {

    private static final byte PRESENT = 1;

    private static final byte NOT_PRESENT = 0;

    private int mId;
    private int mIdGroup;
    private int mIdUser;
    private double mAmount;
    private String mName;
    private String mNotes;
    private long mCreatedAt;
    private long mUpdatedAt;
    private long mArchivedAt;
    private String mPosition;
    private String mAmountDetails; // FIXME: 06/05/2016 da sostituire con un vector da riempire al momento dell'importazione dal DB

    public CostModel(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, long archivedAt, String position, String amountDetails) {
        mId = id;
        mIdGroup = idGroup;
        mIdUser = idUser;
        mAmount = amount;
        mName = name;
        mNotes = notes;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
        mArchivedAt = archivedAt;
        mPosition = position;
        mAmountDetails = amountDetails;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getIdGroup() {
        return mIdGroup;
    }

    public void setIdGroup(int idGroup) {
        mIdGroup = idGroup;
    }

    public int getIdUser() {
        return mIdUser;
    }

    public void setIdUser(int idUser) {
        mIdUser = idUser;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getNotes() {
        return mNotes;
    }

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(long createdAt) {
        mCreatedAt = createdAt;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public long getArchivedAt() {
        return mArchivedAt;
    }

    public void setArchivedAt(long archivedAt) {
        mArchivedAt = archivedAt;
    }

    public String getPosition() {
        return mPosition;
    }

    public void setPosition(String position) {
        mPosition = position;
    }

    public String getAmountDetails() {
        return mAmountDetails;
    }

    public void setAmountDetails(String amountDetails) {
        mAmountDetails = amountDetails;
    }

    public static CostModel create(JSONObject costJs) throws JSONException {
        int id = costJs.getInt("id");
        int idGroup = costJs.getInt("idGroup");
        int idUser = costJs.getInt("idUser");
        double amount = costJs.getDouble("amount");
        String name = costJs.getString("name");
        String notes = costJs.getString("notes");
        long createdAt = DateUtils.dateStringToLong(costJs.getString("created_at"));
        long updatedAt = DateUtils.dateStringToLong(costJs.getString("updated_at"));
        long archivedAt = DateUtils.dateStringToLong(costJs.getString("archived_at"));
        String position = costJs.getString("position");
        String amountDetails = costJs.getString("amount_details");

        return new CostModel(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, archivedAt, position, amountDetails);
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
    public CostModel(Parcel in)
    {
        this.mId = in.readInt();
        this.mIdGroup = in.readInt();
        this.mIdUser = in.readInt();
        this.mAmount = in.readDouble();
        if(in.readByte() == PRESENT)
        {
            this.mName = in.readString();
        }

        if(in.readByte() == PRESENT)
        {
            this.mNotes = in.readString();
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
            this.mArchivedAt= in.readLong();
        }

        if(in.readByte() == PRESENT)
        {
            this.mPosition = in.readString();
        }
        if(in.readByte() == PRESENT)
        {
            this.mAmountDetails = in.readString();
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
        dest.writeInt(this.mIdGroup);
        dest.writeInt(this.mIdUser);
        dest.writeDouble(this.mAmount);
        if(!TextUtils.isEmpty(this.mName))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mName);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mNotes))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mNotes);
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

        if(this.mArchivedAt!=0)
        {
            dest.writeByte(PRESENT);
            dest.writeLong(this.mArchivedAt);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mPosition))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mPosition);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

        if(!TextUtils.isEmpty(this.mAmountDetails))
        {
            dest.writeByte(PRESENT);
            dest.writeString(this.mAmountDetails);
        } else
        {
            dest.writeByte(NOT_PRESENT);
        }

    }
}
