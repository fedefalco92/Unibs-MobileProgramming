package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.BalancingModel;
import it.unibs.appwow.models.TransactionModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class BalancingDAO implements LocalDB_DAO {
    private static final String TAG_LOG = BalancingDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {
            AppDB.Balancings._ID,
            AppDB.Balancings.COLUMN_ID_GROUP,
            AppDB.Balancings.COLUMN_CREATED_AT,
            AppDB.Balancings.COLUMN_COSTS_ID
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
    private ContentValues balancingToValues(BalancingModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Balancings._ID, data.getId());
        values.put(AppDB.Balancings.COLUMN_ID_GROUP, data.getIdGroup());
        values.put(AppDB.Balancings.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Balancings.COLUMN_COSTS_ID, data.getCostsId());
        return values;
    }

    // from database to Object
    private BalancingModel cursorToBalancing(Cursor cursor) {
        int id = cursor.getInt(0);
        int idGroup = cursor.getInt(1);
        long createdAt = cursor.getLong(2);
        String costsId = cursor.getString(3);

        return new BalancingModel(id, idGroup, createdAt, costsId);
    }

    public BalancingModel insertBalancing(BalancingModel data) {

        database.replace(AppDB.Balancings.TABLE_BALANCINGS, null,
                balancingToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Balancings.TABLE_BALANCINGS,allColumns,
                AppDB.Balancings._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        BalancingModel d = cursorToBalancing(cursor);
        cursor.close();
        return d;

    }

    public void resetAllBalancings(int idGroup) {
        database.delete(AppDB.Balancings.TABLE_BALANCINGS, AppDB.Balancings.COLUMN_ID_GROUP + " = ?" , new String[]{"" + idGroup});
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


}
