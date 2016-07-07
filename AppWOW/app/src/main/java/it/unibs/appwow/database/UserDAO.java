package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.UserModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class UserDAO implements LocalDB_DAO {

    private static final String TAG_LOG = UserDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {AppDB.Users._ID,
            AppDB.Users.COLUMN_FULLNAME,
            AppDB.Users.COLUMN_EMAIL,
            AppDB.Users.COLUMN_CREATED_AT,
            AppDB.Users.COLUMN_UPDATED_AT
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
    private ContentValues userToValues(UserModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Users._ID, data.getId());
        values.put(AppDB.Users.COLUMN_FULLNAME, data.getFullName());
        values.put(AppDB.Users.COLUMN_EMAIL, data.getEmail());
        values.put(AppDB.Users.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Users.COLUMN_UPDATED_AT, data.getUpdatedAt());
        return values;
    }

    // from database to Object
    private UserModel cursorToUser(Cursor cursor) {
        int id = cursor.getInt(0);
        String fullName = cursor.getString(1);
        String email = cursor.getString(2);
        long createdAt = cursor.getLong(3);
        long updatedAt = cursor.getLong(4);

        return new UserModel(id, fullName,email,createdAt,updatedAt);
    }

    public UserModel insertUser(UserModel data) {

        database.replace(AppDB.Users.TABLE_USERS, null,
                userToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Users.TABLE_USERS,allColumns,
                AppDB.Users._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        UserModel d = cursorToUser(cursor);
        cursor.close();
        return d;
        /*
        boolean success = false;
        try {
            database.beginTransaction();
            //database.execSQL(QUERY);
            database.replace(AppDB.Users.TABLE_USERS, null,
                    userToValues(data));
            database.setTransactionSuccessful();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return success;
        */
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
    }

    public void resetAllUsers() {
        database.delete(AppDB.Users.TABLE_USERS,null,null);
    }

    /*
    public void updateSingleUser(long id, String groupName, int photoUri, long createdAt, long updatedAt, long idAdmin) {
        ContentValues groupToInsert = new ContentValues();
        groupToInsert.put(AppDB.Groups._ID, id);
        groupToInsert.put(AppDB.Groups.COLUMN_NAME, groupName);
        groupToInsert.put(AppDB.Groups.COLUMN_PHOTO, photoUri);
        groupToInsert.put(AppDB.Groups.COLUMN_CREATED_AT, createdAt);
        groupToInsert.put(AppDB.Groups.COLUMN_UPDATED_AT, updatedAt);
        groupToInsert.put(AppDB.Groups.COLUMN_ID_ADMIN, idAdmin);

        database.update(AppDB.Groups.TABLE_GROUPS, groupToInsert, AppDB.Groups._ID + " = ?",new String[] {"" + id});
    }*/

    public String[] getSingleUserInfo(int idUser){
        String[] res = new String[2];
        String[] columns = {
                    AppDB.Users.COLUMN_FULLNAME,
                    AppDB.Users.COLUMN_EMAIL
        };
        String selection = AppDB.Users._ID + " = ?";
        String[] args = {String.valueOf(idUser)};
        Cursor cursor = database.query(AppDB.Users.TABLE_USERS, columns, selection, args, null, null, null);
        if(cursor.moveToFirst()){
            res[0] = cursor.getString(0);
            res[1] = cursor.getString(1);
        }
        cursor.close();
        return res;
    }
}
