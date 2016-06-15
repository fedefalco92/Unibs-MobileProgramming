package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.TransactionModel;
import it.unibs.appwow.utils.dummy.DummyTransactionContent;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class TransactionDAO implements LocalDB_DAO {
    private static final String TAG_LOG = TransactionDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {
            AppDB.Transactions._ID,
            AppDB.Transactions.COLUMN_ID_BALANCING,
            AppDB.Transactions.COLUMN_ID_FROM,
            AppDB.Transactions.COLUMN_ID_TO,
            AppDB.Transactions.COLUMN_AMOUNT,
            AppDB.Transactions.COLUMN_PAYED_AT
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
    private ContentValues transactionToValues(TransactionModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Transactions._ID, data.getId());
        values.put(AppDB.Transactions.COLUMN_ID_BALANCING, data.getIdBalancing());
        values.put(AppDB.Transactions.COLUMN_ID_FROM, data.getIdFrom());
        values.put(AppDB.Transactions.COLUMN_ID_TO, data.getIdTo());
        values.put(AppDB.Transactions.COLUMN_AMOUNT, data.getAmount());
        values.put(AppDB.Transactions.COLUMN_PAYED_AT, data.getPayedAt());
        return values;
    }

    // from database to Object
    private TransactionModel cursorToTransaction(Cursor cursor) {
        int id = cursor.getInt(0);
        int idBalancing = cursor.getInt(1);
        int idFrom = cursor.getInt(2);
        int idTo = cursor.getInt(3);
        double amount = cursor.getDouble(4);
        long payedAt = cursor.getLong(5);

        return new TransactionModel(id, idBalancing, idFrom, idTo, amount, payedAt);
    }

    public TransactionModel insertTransaction(TransactionModel data) {

        database.replace(AppDB.Transactions.TABLE_TRANSACTIONS, null,
               transactionToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Transactions.TABLE_TRANSACTIONS,allColumns,
                AppDB.Transactions._ID + " = ?",
                new String[] {"" + data.getId()},null,null,null);
        cursor.moveToFirst();
        TransactionModel d = cursorToTransaction(cursor);
        cursor.close();
        return d;

    }

    public void resetAllTransactions(int idGroup) {
       // database.delete(AppDB.Transactions.TABLE_TRANSACTIONS, AppDB.Transactions. + " = ?" , new String[]{"" + idGroup});
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
