package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.Transaction;
import it.unibs.appwow.models.TransactionModel;

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

    private Transaction cursorToTransactionWithFullName(Cursor cursor) {
        int id = cursor.getInt(0);
        int idBalancing = cursor.getInt(1);
        int idFrom = cursor.getInt(2);
        int idTo = cursor.getInt(3);
        double amount = cursor.getDouble(4);
        long payedAt = cursor.getLong(5);
        String fullName = cursor.getString(6);

        return new Transaction(id, idBalancing, idFrom, idTo, amount, payedAt, fullName);
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

    public List<Transaction> getAllTransactionsFrom(int idGroup, int idUser) {
        List<Transaction> data = new ArrayList<Transaction>();
        //String query = "SELECT idTo, amount FROM groups LEFT JOIN balancings ON groups._id = balancings.idGroup LEFT JOIN transactions ON balancings._id = transactions.idBalancing WHERE groups._id = ? AND transactions.idFrom = ?";
        String query = "SELECT transactions._id, transactions.amount, transactions.idBalancing, transactions.idFrom, transactions.idTo, transactions.payed_at, users.fullName " +
                "FROM groups LEFT JOIN balancings ON groups._id = balancings.idGroup LEFT JOIN transactions ON balancings._id = transactions.idBalancing LEFT JOIN users ON transactions.idTo = users._id "+
                "WHERE groups._id = ? AND  (transactions.idFrom = ? OR transactions.idTo = ?)";
        String[] values =  new String[]{String.valueOf(idGroup), String.valueOf(idUser),String.valueOf(idUser)};
        Cursor cursor = database.rawQuery(query, values);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Transaction d = cursorToTransactionWithFullName(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }



}
