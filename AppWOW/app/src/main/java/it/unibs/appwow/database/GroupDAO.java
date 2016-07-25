package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.Amount;
import it.unibs.appwow.models.SliderAmount;
import it.unibs.appwow.models.parc.GroupModel;

/**
 * Created by federicofalcone on 12/05/16.
 */
public class GroupDAO implements LocalDB_DAO {

    private static final String TAG_LOG = GroupDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {AppDB.Groups._ID,
            AppDB.Groups.COLUMN_ID_ADMIN,
            AppDB.Groups.COLUMN_NAME,
            AppDB.Groups.COLUMN_PHOTO,
            AppDB.Groups.COLUMN_PHOTO_UPDATED_AT,
            AppDB.Groups.COLUMN_CREATED_AT,
            AppDB.Groups.COLUMN_UPDATED_AT,
            AppDB.Groups.COLUMN_HIGHLIGHTED};

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
    private ContentValues groupToValues(GroupModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Groups._ID, data.getId());
        values.put(AppDB.Groups.COLUMN_ID_ADMIN, data.getIdAdmin());
        values.put(AppDB.Groups.COLUMN_NAME, data.getGroupName());
        values.put(AppDB.Groups.COLUMN_PHOTO, data.getPhotoFileName());
        values.put(AppDB.Groups.COLUMN_PHOTO_UPDATED_AT, data.getPhotoUpdatedAt());
        values.put(AppDB.Groups.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Groups.COLUMN_UPDATED_AT, data.getUpdatedAt());
        values.put(AppDB.Groups.COLUMN_HIGHLIGHTED, data.getHighlighted());

        return values;
    }

    // from database to Object
    private GroupModel cursorToGroup(Cursor cursor) {
        int id = cursor.getInt(0);
        int idAdmin = cursor.getInt(1);
        String groupName = cursor.getString(2);
        String photoFileName = cursor.getString(3);
        long photoUpdatedAt = cursor.getLong(4);
        long createdAt = cursor.getLong(5);
        long updatedAt = cursor.getLong(6);
        int highlighted = cursor.getInt(7);

        return new GroupModel(id,groupName, photoFileName, photoUpdatedAt, createdAt, updatedAt, idAdmin, highlighted);
    }

    /*
    private Amount cursorToAmount(Cursor cursor, int id){
        String fullName = cursor.getString(0);
        double amount = cursor.getDouble(1);

        return new Amount(id, fullName, amount);
    }*/

    private Amount cursorToAmount(Cursor cursor){
        int id = cursor.getInt(0);
        String fullName = cursor.getString(1);
        double amount = cursor.getDouble(2);
        String email = cursor.getString(3);

        return new Amount(id, fullName, amount, email);
    }

    public GroupModel insertGroup(GroupModel data) {
        database.replace(AppDB.Groups.TABLE_GROUPS, null,
                groupToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,allColumns,
                AppDB.Groups._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        GroupModel d = cursorToGroup(cursor);
        cursor.close();
        return d;
    }

    public List<GroupModel> getAllGroups() {
        List<GroupModel> data = new ArrayList<GroupModel>();
        String orderby = AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups.COLUMN_HIGHLIGHTED + " DESC " + " , " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups.COLUMN_UPDATED_AT + " DESC";
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns,null,null,null,null,orderby);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            GroupModel d = cursorToGroup(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public List<Amount> getAllAmounts(int idGroup){
        List<Amount> data = new ArrayList<Amount>();
        //String query = "SELECT DISTINCT users.fullName, user_group.amount FROM user_group, users WHERE user_group.idGroup = ?; user_group.idUser = users._id";
        String query = "SELECT users._id, users.fullName, user_group.amount, users.email FROM user_group LEFT JOIN  users ON user_group.idUser = users._id WHERE user_group.idGroup = ?;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup)});
        cursor.moveToFirst();
        //int id = 0;
        while(!cursor.isAfterLast()) {
            //Amount d = cursorToAmount(cursor, id);
            Amount d = cursorToAmount(cursor);
            data.add(d);
            cursor.moveToNext();
            //id++;
        }
        cursor.close(); // remember to always close the cursor!

        return data;
    }


    public long getUpdatedAt(int groupId){
        List<GroupModel> data = new ArrayList<GroupModel>();
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns, AppDB.Groups._ID + "=" + groupId,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            GroupModel d = cursorToGroup(cursor);
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

    public long getPhotoUpdatedAt(int groupId){
        List<GroupModel> data = new ArrayList<GroupModel>();
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns, AppDB.Groups._ID + "=" + groupId,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            GroupModel d = cursorToGroup(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        if(data.size()!=1){
            return 0L;
        } else {
            return data.get(0).getPhotoUpdatedAt();
        }
    }

    public boolean highlightGroup(int id){
        ContentValues groupToUpdate = new ContentValues();
        groupToUpdate.put(AppDB.Groups.COLUMN_HIGHLIGHTED, 1);
        int res = database.update(AppDB.Groups.TABLE_GROUPS, groupToUpdate, AppDB.Groups._ID + " = ?",new String[] {"" + id});
        return res > 0;
    }

    public boolean unHighlightGroup(int id){
        ContentValues groupToUpdate = new ContentValues();
        groupToUpdate.put(AppDB.Groups.COLUMN_HIGHLIGHTED, 0);
        int res = database.update(AppDB.Groups.TABLE_GROUPS, groupToUpdate, AppDB.Groups._ID + " = ?",new String[] {"" + id});
        return res > 0;
    }

    
    public void resetAllGroups() {
        database.delete(AppDB.Groups.TABLE_GROUPS,null,null);
    }
    
    public void deleteSingleLGroup(int id) {
        database.delete(AppDB.Groups.TABLE_GROUPS,AppDB.Groups._ID + " = ? ;",new String[] {String.valueOf(id)});
    }
    
    public void updateSingleGroup(int id, int idAdmin, String groupName, String photoFileName, long photoUpdatedAt, long createdAt, long updatedAt, int highlighted) {
        ContentValues groupToInsert = groupToValues(new GroupModel(id, groupName, photoFileName, photoUpdatedAt, createdAt, updatedAt, idAdmin, highlighted));
        database.update(AppDB.Groups.TABLE_GROUPS, groupToInsert, AppDB.Groups._ID + " = ?",new String[] {"" + id});
    }

    public Set<Integer> getLocalGroupsIds() {
        Set<Integer> data = new HashSet<Integer>();
        //"SELECT groups._id FROM groups;";
        String query = "SELECT " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID
                + " FROM " + AppDB.Groups.TABLE_GROUPS;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            int id = cursor.getInt(0);
            data.add(id);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public String getGroupName(int idGroup) {
        String groupName = "";
        String query = "SELECT " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups.COLUMN_NAME + " FROM " +
                AppDB.Groups.TABLE_GROUPS + " WHERE " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID + " = ? ;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup)});
        cursor.moveToFirst();
        groupName = cursor.getString(0);
        cursor.close(); // remember to always close the cursor!
        return groupName;
    }

    public String getGroupAdminName(int idGroup) {
        String adminName = "";
        String query = "SELECT " + AppDB.Users.TABLE_USERS + "." + AppDB.Users.COLUMN_FULLNAME + " FROM " +
                AppDB.Groups.TABLE_GROUPS + " LEFT JOIN " + AppDB.Users.TABLE_USERS + " ON " +
                AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups.COLUMN_ID_ADMIN + " = " + AppDB.Users.TABLE_USERS + "." + AppDB.Users._ID +
                " WHERE " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID + " = ? ;";
        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup)});
        cursor.moveToFirst();
        adminName = cursor.getString(0);
        cursor.close(); // remember to always close the cursor!
        return adminName;
    }

    public void touchGroup(int idGroup, long timestamp){
        //"UPDATE groups SET updated_at = " + timestamp + " WHERE _id = " + idGroup;
        String query = "UPDATE " + AppDB.Groups.TABLE_GROUPS
                + " SET " + AppDB.Groups.COLUMN_UPDATED_AT + " = " + timestamp
                + " WHERE " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID + " = " + idGroup;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        cursor.close();
    }

    public Double getAmount(int idGroup, int idUser) {
        Double amount = null;
        String query = "SELECT " +AppDB.UserGroup.TABLE_USER_GROUP + "." + AppDB.UserGroup.COLUMN_AMOUNT +
                " FROM " + AppDB.UserGroup.TABLE_USER_GROUP +
                " WHERE " + AppDB.UserGroup.TABLE_USER_GROUP + "." + AppDB.UserGroup.COLUMN_ID_GROUP + " = ?" +
                " AND " + AppDB.UserGroup.TABLE_USER_GROUP + "." + AppDB.UserGroup.COLUMN_ID_USER + " = ?;";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idGroup), String.valueOf(idUser)});
        cursor.moveToFirst();
        if(!cursor.isAfterLast()){
            amount = new Double(cursor.getDouble(0));
            cursor.close(); // remember to always close the cursor!
        }
        return amount;
    }

    public boolean setPhotoFileName(int idGroup, String fileName) {
        String query = "UPDATE " + AppDB.Groups.TABLE_GROUPS
                + " SET " + AppDB.Groups.COLUMN_PHOTO + " = \"" + fileName + "\""
                + " WHERE " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID + " = " + idGroup;
        Cursor cursor = database.rawQuery(query, null);
        boolean ok = cursor.moveToFirst(); // Ritorna falso se e' vuoto.
        cursor.close();
        // FIXME: 04/07/2016 RITORNARE TRUE O FALSE...CAPIRE COME FUNZIONA RAWQUERY...EVENTUALMENTE USARE IL METODO UPDATE();
        return ok;
    }

    public void touchGroupPhoto(int idGroup, long timestamp) {
        String query = "UPDATE " + AppDB.Groups.TABLE_GROUPS
                + " SET " + AppDB.Groups.COLUMN_PHOTO_UPDATED_AT + " = " + timestamp
                + " WHERE " + AppDB.Groups.TABLE_GROUPS + "." + AppDB.Groups._ID + " = " + idGroup;
        Cursor cursor = database.rawQuery(query, null);
        cursor.moveToFirst();
        cursor.close();
    }

    public GroupModel getSingleGroup(int idGroup){
        GroupModel res = null;
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns,AppDB.Groups._ID + " = " + idGroup ,null,null,null,null);
        if(cursor.moveToFirst()){
            res = cursorToGroup(cursor);
        }
        cursor.close();
        return res;
    }
}
