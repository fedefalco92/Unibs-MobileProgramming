package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.parc.Group;

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
    private ContentValues groupToValues(Group data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Groups._ID, data.getId());
        values.put(AppDB.Groups.COLUMN_ID_ADMIN, data.getIdAdmin());
        values.put(AppDB.Groups.COLUMN_NAME, data.getGroupName());
        values.put(AppDB.Groups.COLUMN_PHOTO, data.getPhotoUri());
        values.put(AppDB.Groups.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Groups.COLUMN_UPDATED_AT, data.getUpdatedAt());
        values.put(AppDB.Groups.COLUMN_HIGHLIGHTED, data.getHighlighted());

        return values;
    }

    // from database to Object
    private Group cursorToGroup(Cursor cursor) {
        int id = cursor.getInt(0);
        int idAdmin = cursor.getInt(1);
        String groupName = cursor.getString(2);
        String photoUri = cursor.getString(3);
        long createdAt = cursor.getLong(4);
        long updatedAt = cursor.getLong(5);
        int highlighted = cursor.getInt(6);


        return new Group(id,groupName, photoUri, createdAt, updatedAt, idAdmin, highlighted);
    }

    public Group insertGroup(Group data) {
        database.replace(AppDB.Groups.TABLE_GROUPS, null,
                groupToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,allColumns,
                AppDB.Groups._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        Group d = cursorToGroup(cursor);
        cursor.close();
        return d;
    }

    // TODO: 12/05/16 aggiungere parametro User.
    public List<Group> getAllGroups() {
        List<Group> data = new ArrayList<Group>();
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns,null,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Group d = cursorToGroup(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public long getUpdatedAt(int groupId){
        List<Group> data = new ArrayList<Group>();
        Cursor cursor = database.query(AppDB.Groups.TABLE_GROUPS,
                allColumns, AppDB.Groups._ID + "=" + groupId,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Group d = cursorToGroup(cursor);
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
    
    public void deleteSingleLGroup(Group data) {
        database.delete(AppDB.Groups.TABLE_GROUPS,AppDB.Groups._ID + " = ?",new String[] {"" + data.getId()});
    }
    
    public void updateSingleGroup(long id, String groupName, int photoUri, long createdAt, long updatedAt, long idAdmin) {
        ContentValues groupToInsert = new ContentValues();
        groupToInsert.put(AppDB.Groups._ID, id);
        groupToInsert.put(AppDB.Groups.COLUMN_NAME, groupName);
        groupToInsert.put(AppDB.Groups.COLUMN_PHOTO, photoUri);
        groupToInsert.put(AppDB.Groups.COLUMN_CREATED_AT, createdAt);
        groupToInsert.put(AppDB.Groups.COLUMN_UPDATED_AT, updatedAt);
        groupToInsert.put(AppDB.Groups.COLUMN_ID_ADMIN, idAdmin);

        database.update(AppDB.Groups.TABLE_GROUPS, groupToInsert, AppDB.Groups._ID + " = ?",new String[] {"" + id});
    }
}
