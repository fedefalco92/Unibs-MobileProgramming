package it.unibs.appwow.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import it.unibs.appwow.MyApplication;

/**
 * Created by Massi on 12/05/2016.
 */
public abstract class LocalDB_DAO {
    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;

    public void open() {
        if(dbHelper == null) {
            dbHelper = new AppSQLiteHelper(MyApplication.getAppContext());
        }
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }
}
