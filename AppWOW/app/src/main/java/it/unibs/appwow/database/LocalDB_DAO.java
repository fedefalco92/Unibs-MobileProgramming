package it.unibs.appwow.database;

import android.database.sqlite.SQLiteDatabase;

import it.unibs.appwow.MyApplication;

/**
 * Created by Massi on 12/05/2016.
 */
public interface LocalDB_DAO {

    public void open();
    public void close();

}
