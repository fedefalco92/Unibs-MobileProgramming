package it.unibs.appwow.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class BalancingModel {
    private int mId;
    private int mIdGroup;
    private long mCreatedAt;
    private String mCostsId;

    public BalancingModel(int id, int idGroup, long createdAt, String costsId) {
        mId = id;
        mIdGroup = idGroup;
        mCreatedAt = createdAt;
        mCostsId = costsId;
    }

    public static BalancingModel create(JSONObject bjs) throws JSONException {
        // FIXME: 15/06/2016 ASPETTARE CHE FEDE ABBIA CREATO L'API
        int id = bjs.getInt("id");
        int idGroup = bjs.getInt("idGroup");
        long createdAt = bjs.getLong("created_at");
        String costsId = bjs.getString("costsId");
        //return new BalancingModel(id, idGroup, createdAt,costsId);
        return null;
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

    public long getCreatedAt() {
        return mCreatedAt;
    }

    public void setCreatedAt(long createdAt) {
        mCreatedAt = createdAt;
    }

    public String getCostsId() {
        return mCostsId;
    }

    public void setCostsId(String costsId) {
        mCostsId = costsId;
    }
}
