package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.net.IDN;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.CostModel;
import it.unibs.appwow.models.UserModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class CostsDAO implements LocalDB_DAO {

    private static final String TAG_LOG = CostsDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {
            AppDB.Costs._ID,
            AppDB.Costs.COLUMN_ID_GROUP,
            AppDB.Costs.COLUMN_ID_USER,
            AppDB.Costs.COLUMN_AMOUNT,
            AppDB.Costs.COLUMN_NAME,
            AppDB.Costs.COLUMN_NOTES,
            AppDB.Costs.COLUMN_CREATED_AT,
            AppDB.Costs.COLUMN_UPDATED_AT,
            AppDB.Costs.COLUMN_ARCHIVED_AT,
            AppDB.Costs.COLUMN_POSITION,
            AppDB.Costs.COLUMN_AMOUNT_DETAILS
    };

    @Override
    public void open() {
        if(dbHelper == null) {
            dbHelper = new AppSQLiteHelper(MyApplication.getAppContext());
        }
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        dbHelper.close();
    }


    // from Object to database
    private ContentValues costToValues(CostModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Costs._ID, data.getId());
        values.put(AppDB.Costs.COLUMN_ID_GROUP, data.getIdGroup());
        values.put(AppDB.Costs.COLUMN_ID_USER, data.getIdUser());
        values.put(AppDB.Costs.COLUMN_AMOUNT, data.getAmount());
        values.put(AppDB.Costs.COLUMN_NAME, data.getName());
        values.put(AppDB.Costs.COLUMN_NOTES,data.getNotes());
        values.put(AppDB.Costs.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Costs.COLUMN_UPDATED_AT, data.getUpdatedAt());
        values.put(AppDB.Costs.COLUMN_ARCHIVED_AT, data.getArchivedAt());
        values.put(AppDB.Costs.COLUMN_POSITION, data.getPosition());
        values.put(AppDB.Costs.COLUMN_AMOUNT_DETAILS, data.getAmountDetails());
        return values;
    }

    // from database to Object
    private CostModel cursorToCost(Cursor cursor) {
        int id = cursor.getInt(0);
        int idGroup = cursor.getInt(1);
        int idUser = cursor.getInt(2);
        double amount = cursor.getDouble(3);
        String name = cursor.getString(4);
        String notes = cursor.getString(5);
        long createdAt = cursor.getLong(6);
        long updatedAt = cursor.getLong(7);
        long archivedAt = cursor.getLong(8);
        String position = cursor.getString(9);
        String amountDetails = cursor.getString(10);

        return new CostModel(id, idGroup, idUser, amount, name, notes,createdAt, updatedAt, archivedAt, position, amountDetails);
    }

    public CostModel insertCost(CostModel data) {

        database.replace(AppDB.Costs.TABLE_COSTS, null,
                costToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Costs.TABLE_COSTS,allColumns,
                AppDB.Users._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        CostModel d = cursorToCost(cursor);
        cursor.close();
        return d;

    }

    public void resetAllCosts(int idGroup) {
        database.delete(AppDB.Costs.TABLE_COSTS, AppDB.Costs.COLUMN_ID_GROUP + " = ?" , new String[]{"" + idGroup});
    }

    public List<CostModel> getAllCosts(int idGroup) {
        List<CostModel> data = new ArrayList<CostModel>();
        Cursor cursor = database.query(AppDB.Costs.TABLE_COSTS,
                allColumns,AppDB.Costs.COLUMN_ID_GROUP + " = " + idGroup,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CostModel d = cursorToCost(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }
/*
    public List<UserModel> getAllUsers(int idGroup) {
        List<UserModel> data = new ArrayList<UserModel>();
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns,null,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            GroupModel d = cursorToGroup(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }*/
    /*
    public long getUpdatedAt(int userId){
        List<UserModel> data = new ArrayList<UserModel>();
        Cursor cursor = database.query(AppDB.Users.TABLE_USERS,
                allColumns, AppDB.Users._ID + "=" + userId,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            UserModel d = cursorToUser(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        if(data.size()!=1){
            return 0L;
        } else {
            return data.get(0).getUpdatedAt();
        }
    }*/

}
