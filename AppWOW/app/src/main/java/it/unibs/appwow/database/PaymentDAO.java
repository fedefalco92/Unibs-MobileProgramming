package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.Payment;
import it.unibs.appwow.models.parc.PaymentModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class PaymentDAO implements LocalDB_DAO {

    private static final String TAG_LOG = PaymentDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {
            AppDB.Payments._ID,
            AppDB.Payments.COLUMN_ID_GROUP,
            AppDB.Payments.COLUMN_ID_USER,
            AppDB.Payments.COLUMN_AMOUNT,
            AppDB.Payments.COLUMN_NAME,
            AppDB.Payments.COLUMN_NOTES,
            AppDB.Payments.COLUMN_CREATED_AT,
            AppDB.Payments.COLUMN_UPDATED_AT,
            AppDB.Payments.COLUMN_IS_EXCHANGE,
            AppDB.Payments.COLUMN_POSITION,
            AppDB.Payments.COLUMN_POSITION_ID,
            AppDB.Payments.COLUMN_AMOUNT_DETAILS
    };

    private String[] allColumnsExtra = {
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments._ID,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_ID_GROUP,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_ID_USER,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_AMOUNT,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_NAME,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_NOTES,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_CREATED_AT,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_UPDATED_AT,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_IS_EXCHANGE,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_POSITION,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_POSITION_ID,
            AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_AMOUNT_DETAILS,
            AppDB.Users.TABLE_USERS + "." + AppDB.Users.COLUMN_FULLNAME,
            AppDB.Users.TABLE_USERS + "." + AppDB.Users.COLUMN_EMAIL
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
    private ContentValues costToValues(PaymentModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Payments._ID, data.getId());
        values.put(AppDB.Payments.COLUMN_ID_GROUP, data.getIdGroup());
        values.put(AppDB.Payments.COLUMN_ID_USER, data.getIdUser());
        values.put(AppDB.Payments.COLUMN_AMOUNT, data.getAmount());
        values.put(AppDB.Payments.COLUMN_NAME, data.getName());
        values.put(AppDB.Payments.COLUMN_NOTES,data.getNotes());
        values.put(AppDB.Payments.COLUMN_CREATED_AT, data.getCreatedAt());
        values.put(AppDB.Payments.COLUMN_UPDATED_AT, data.getUpdatedAt());
        // FIXME: 30/06/2016 VERIFICARE FUNZIONAMENTO BOOLEAN - INTEGER
        values.put(AppDB.Payments.COLUMN_IS_EXCHANGE, data.isExchange());
        values.put(AppDB.Payments.COLUMN_POSITION, data.getPosition());
        values.put(AppDB.Payments.COLUMN_POSITION_ID, data.getPositionId());
        values.put(AppDB.Payments.COLUMN_AMOUNT_DETAILS, data.getAmountDetails());
        return values;
    }

    // from database to Object

    private PaymentModel cursorToPaymentModel(Cursor cursor) {
        int id = cursor.getInt(0);
        int idGroup = cursor.getInt(1);
        int idUser = cursor.getInt(2);
        double amount = cursor.getDouble(3);
        String name = cursor.getString(4);
        String notes = cursor.getString(5);
        long createdAt = cursor.getLong(6);
        long updatedAt = cursor.getLong(7);
        boolean isExchange = cursor.getInt(8) != 0;
        String position = cursor.getString(9);
        String position_id = cursor.getColumnName(10);
        String amountDetails = cursor.getString(11);

        return new PaymentModel(id, idGroup, idUser, amount, name, notes,createdAt, updatedAt,position, position_id, amountDetails, isExchange);
    }

    private Payment cursorToPayment(Cursor cursor) {
        int id = cursor.getInt(0);
        int idGroup = cursor.getInt(1);
        int idUser = cursor.getInt(2);
        double amount = cursor.getDouble(3);
        String name = cursor.getString(4);
        String notes = cursor.getString(5);
        long createdAt = cursor.getLong(6);
        long updatedAt = cursor.getLong(7);
        boolean isExchange = cursor.getInt(8) != 0;
        String position = cursor.getString(9);
        String position_id = cursor.getString(10);
        String amountDetails = cursor.getString(11);
        String fullName = cursor.getString(12);
        String email  = cursor.getString(13);

        return new Payment(id, idGroup, idUser, fullName, email, amount, name, notes, createdAt, updatedAt, position, position_id, amountDetails, isExchange);
    }

    public PaymentModel insertPayment(PaymentModel data) {

        database.replace(AppDB.Payments.TABLE_PAYMENTS, null,
                costToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Payments.TABLE_PAYMENTS,allColumns,
                AppDB.Users._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        PaymentModel d = cursorToPaymentModel(cursor);
        cursor.close();
        return d;

    }

    public void resetAllCosts(int idGroup) {
        int affected_rows = database.delete(AppDB.Payments.TABLE_PAYMENTS, AppDB.Payments.COLUMN_ID_GROUP + " = ?" , new String[]{"" + idGroup});
        Log.d(TAG_LOG, "RESET_ALL_COSTS_AFFECTED_ROWS: " + affected_rows);
    }


    public List<PaymentModel> getAllCosts(int idGroup) {
        List<PaymentModel> data = new ArrayList<PaymentModel>();
        Cursor cursor = database.query(AppDB.Payments.TABLE_PAYMENTS,
                allColumns, AppDB.Payments.COLUMN_ID_GROUP + " = " + idGroup,null,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            PaymentModel d = cursorToPaymentModel(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public List<Payment> getAllPayments(int idGroup) {
        List<Payment> data = new ArrayList<Payment>();
        String orderby = AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_CREATED_AT + " DESC";
        Cursor cursor = database.query(AppDB.Payments.TABLE_PAYMENTS + " LEFT JOIN " + AppDB.Users.TABLE_USERS + " ON " +
                AppDB.Payments.TABLE_PAYMENTS + "." + AppDB.Payments.COLUMN_ID_USER + "=" + AppDB.Users.TABLE_USERS + "." + AppDB.Users._ID,
                allColumnsExtra, AppDB.Payments.COLUMN_ID_GROUP + " = " + idGroup,null,null,null,orderby);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Payment d = cursorToPayment(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public void removeSinglePayment(int id) {
        database.delete(AppDB.Payments.TABLE_PAYMENTS ,AppDB.Payments._ID + " = ? ;",new String[] {String.valueOf(id)});
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
