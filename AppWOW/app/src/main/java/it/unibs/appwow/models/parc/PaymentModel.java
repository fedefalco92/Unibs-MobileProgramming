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
public class PaymentModel implements Parcelable {

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
    private boolean mIsExchange;
    /**
     * la stringa mPosition è una posizione fittizia tipo "casa mia" oppure un ID di google places preceduto da "###"
     */
    private String mPosition;
    private String mAmountDetails; // FIXME: 06/05/2016 da sostituire con un vector da riempire al momento dell'importazione dal DB

    public PaymentModel(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, String position, String amountDetails) {
        mId = id;
        mIdGroup = idGroup;
        mIdUser = idUser;
        mAmount = amount;
        mName = name;
        mNotes = notes;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
        mPosition = position;
        mAmountDetails = amountDetails;
        mIsExchange = false;
    }

    public PaymentModel(int id, int idGroup, int idUser, double amount, String name, String notes, long createdAt, long updatedAt, String position, String amountDetails, boolean isExchange) {
        mId = id;
        mIdGroup = idGroup;
        mIdUser = idUser;
        mAmount = amount;
        mName = name;
        mNotes = notes;
        mCreatedAt = createdAt;
        mUpdatedAt = updatedAt;
        mPosition = position;
        mAmountDetails = amountDetails;
        mIsExchange = isExchange;
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

    public boolean isExchange() {
        return mIsExchange;
    }

    public void setExchange(boolean exchange) {
        mIsExchange = exchange;
    }

    public static PaymentModel create(JSONObject paymentJs) throws JSONException {
        int id = paymentJs.getInt("id");
        int idGroup = paymentJs.getInt("idGroup");
        int idUser = paymentJs.getInt("idUser");
        double amount = paymentJs.getDouble("amount");
        String name = paymentJs.getString("name");
        String notes = paymentJs.getString("notes");
        long createdAt = DateUtils.dateStringToLong(paymentJs.getString("created_at"));
        long updatedAt = DateUtils.dateStringToLong(paymentJs.getString("updated_at"));
        // FIXME: 30/06/2016 VERIFICARE FUNZIONAMENTO GETBOOLEAN
        boolean isExchange = paymentJs.getInt("isExchange")!= 0;
        String position = paymentJs.getString("position");
        String amountDetails = paymentJs.getString("amount_details");

        return new PaymentModel(id, idGroup, idUser, amount, name, notes, createdAt, updatedAt, position, amountDetails, isExchange);
    }

    public static final Parcelable.Creator<PaymentModel> CREATOR = new Parcelable.Creator<PaymentModel>()
    {
        public PaymentModel createFromParcel(Parcel in)
        {
            return new PaymentModel(in);
        }

        public PaymentModel[] newArray(int size)
        {
            return new PaymentModel[size];
        }
    };

    /**
     * Read from the parcelized object.
     *
     * @param in
     */
    public PaymentModel(Parcel in)
    {
        this.mId = in.readInt();
        this.mIdGroup = in.readInt();
        this.mIdUser = in.readInt();
        this.mAmount = in.readDouble();
        this.mIsExchange = in.readByte() != 0;
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
        dest.writeByte((byte) (isExchange() ? 1 : 0));
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