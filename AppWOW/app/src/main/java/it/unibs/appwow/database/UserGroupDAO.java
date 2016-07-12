package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.SliderAmount;
import it.unibs.appwow.models.UserGroupModel;
import it.unibs.appwow.models.UserModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class UserGroupDAO implements LocalDB_DAO {

    private static final String TAG_LOG = UserGroupDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {AppDB.UserGroup.COLUMN_ID_GROUP,
            AppDB.UserGroup.COLUMN_ID_USER,
            AppDB.UserGroup.COLUMN_AMOUNT,
            AppDB.UserGroup.COLUMN_UPDATED_AT
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
    private ContentValues userGroupToValues(UserGroupModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.UserGroup.COLUMN_ID_GROUP, data.getGroupId());
        values.put(AppDB.UserGroup.COLUMN_ID_USER, data.getUserId());
        values.put(AppDB.UserGroup.COLUMN_AMOUNT, data.getAmount());
        values.put(AppDB.UserGroup.COLUMN_UPDATED_AT, data.getUpdatedAt());
        return values;
    }

    // from database to Object
    private UserGroupModel cursorToUserGroup(Cursor cursor) {
        int groupId = cursor.getInt(0);
        int userId = cursor.getInt(1);
        double amount = cursor.getDouble(2);
        long updatedAt = cursor.getLong(3);

        return new UserGroupModel(groupId, userId,amount,updatedAt);
    }

    public UserGroupModel insertUserGroup(UserGroupModel data) {

        database.replace(AppDB.UserGroup.TABLE_USER_GROUP, null,
                userGroupToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.UserGroup.TABLE_USER_GROUP, allColumns,
                AppDB.UserGroup.COLUMN_ID_USER + " = ? AND " + AppDB.UserGroup.COLUMN_ID_GROUP + " = ?",
                new String[] {String.valueOf(data.getUserId()), String.valueOf(data.getGroupId())},null,null,null);
        cursor.moveToFirst();
        UserGroupModel d = cursorToUserGroup(cursor);
        cursor.close();
        return d;
    }

    public long getUpdatedAt(int userId, int groupId){
        List<UserGroupModel> data = new ArrayList<UserGroupModel>();
        Cursor cursor = database.query(AppDB.UserGroup.TABLE_USER_GROUP,
                allColumns,
                AppDB.UserGroup.COLUMN_ID_USER + "=" + userId + " AND " + AppDB.UserGroup.COLUMN_ID_GROUP + "=" + groupId,
                null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            UserGroupModel d = cursorToUserGroup(cursor);
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

    public HashMap<Integer,UserModel> getAllUsers(int idGroup) {
        HashMap<Integer, UserModel> res = new HashMap<Integer, UserModel>();
        String query = "SELECT users._id, users.fullName, users.email FROM user_group LEFT JOIN  users ON user_group.idUser = users._id WHERE user_group.idGroup = ?;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup)});
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            String fullName = cursor.getString(1);
            String email = cursor.getString(2);
            UserModel user = UserModel.create(id).withFullName(fullName).withEmail(email);
            res.put(id,user);

            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return res;
    }

    public List<SliderAmount> getAllSliderAmounts(int idGroup){
        List<SliderAmount> data = new ArrayList<SliderAmount>();
        String query = "SELECT users._id, users.fullName, users.email FROM user_group LEFT JOIN  users ON user_group.idUser = users._id WHERE user_group.idGroup = ? ORDER BY users.fullName;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup)});
        cursor.moveToFirst();
        //int id = 0;
        while(!cursor.isAfterLast()) {
            //Amount d = cursorToAmount(cursor, id);
            SliderAmount d = cursorToSliderAmount(cursor);
            data.add(d);
            cursor.moveToNext();
            //id++;
        }
        cursor.close(); // remember to always close the cursor!

        return data;
    }

    private SliderAmount cursorToSliderAmount(Cursor cursor){
        int id = cursor.getInt(0);
        String fullName = cursor.getString(1);
        String email = cursor.getString(2);

        return new SliderAmount(id, fullName, email);
    }
}
