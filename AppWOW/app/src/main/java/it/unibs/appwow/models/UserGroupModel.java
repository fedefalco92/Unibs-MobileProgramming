package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

import it.unibs.appwow.database.UserGroupDAO;
import it.unibs.appwow.utils.DateUtils;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class UserGroupModel {
    private int mGroupId;
    private int mUserId;
    private double mAmount;
    private long mUpdatedAt;

    public UserGroupModel(int groupId, int userId, double amount, long updatedAt) {
        this.mGroupId = groupId;
        this.mUserId = userId;
        this.mAmount = amount;
        this.mUpdatedAt = updatedAt;
    }

    public static UserGroupModel create(JSONObject o) throws JSONException {
        int groupId = o.getInt("idGroup");
        int userId = o.getInt("idUser");
        double amount = o.getDouble("amount");
        long updatedAt = DateUtils.dateStringToLong(o.getString("updated_at"));
        return new UserGroupModel(groupId, userId, amount, updatedAt);
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int userId) {
        mUserId = userId;
    }

    public double getAmount() {
        return mAmount;
    }

    public void setAmount(double amount) {
        mAmount = amount;
    }

    public long getUpdatedAt() {
        return mUpdatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        mUpdatedAt = updatedAt;
    }

    public boolean isUpdated(){
        boolean updated = true;
        UserGroupDAO  dao = new UserGroupDAO();
        dao.open();
        long local_updated_at = dao.getUpdatedAt(mUserId,mGroupId);
        if(local_updated_at < mUpdatedAt) updated = false;
        dao.close();
        return updated;

    }
}
