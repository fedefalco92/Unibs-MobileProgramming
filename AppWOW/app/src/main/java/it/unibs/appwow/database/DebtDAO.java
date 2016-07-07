package it.unibs.appwow.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unibs.appwow.MyApplication;
import it.unibs.appwow.models.Debt;
import it.unibs.appwow.models.DebtModel;

/**
 * Created by Alessandro on 15/06/2016.
 */
public class DebtDAO implements LocalDB_DAO {
    private static final String TAG_LOG = DebtDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private String[] allColumns = {
            AppDB.Debts._ID,
            AppDB.Debts.COLUMN_ID_GROUP,
            AppDB.Debts.COLUMN_ID_FROM,
            AppDB.Debts.COLUMN_ID_TO,
            AppDB.Debts.COLUMN_AMOUNT,
    };

    private String[] allColumnsExtra = {
            AppDB.Debts.TABLE_DEBTS + "." +AppDB.Debts._ID,
            AppDB.Debts.TABLE_DEBTS + "." +AppDB.Debts.COLUMN_ID_GROUP,
            AppDB.Debts.TABLE_DEBTS + "." +AppDB.Debts.COLUMN_ID_FROM,
            AppDB.Debts.TABLE_DEBTS + "." +AppDB.Debts.COLUMN_ID_TO,
            AppDB.Debts.TABLE_DEBTS + "." +AppDB.Debts.COLUMN_AMOUNT,
            "users1.fullName as fullNameFrom",
            "users2.fullName as fullNameTo",
            "users1.email as emailFrom",
            "users2.email as emailTo"
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
    private ContentValues debtToValues(DebtModel data) {
        ContentValues values = new ContentValues();
        values.put(AppDB.Debts._ID, data.getId());
        values.put(AppDB.Debts.COLUMN_ID_GROUP, data.getIdGroup());
        values.put(AppDB.Debts.COLUMN_ID_FROM, data.getIdFrom());
        values.put(AppDB.Debts.COLUMN_ID_TO, data.getIdTo());
        values.put(AppDB.Debts.COLUMN_AMOUNT, data.getAmount());
        return values;
    }

    // from database to Object
    private DebtModel cursorToDebt(Cursor cursor) {
        int id = cursor.getInt(0);
        int idGroup = cursor.getInt(1);
        int idFrom = cursor.getInt(2);
        int idTo = cursor.getInt(3);
        double amount = cursor.getDouble(4);

        return new DebtModel(id, idGroup, idFrom, idTo, amount);
    }
    /*
    private Debt cursorToDebtWithFullName(Cursor cursor) {
        int id = cursor.getInt(0);
        int idBalancing = cursor.getInt(1);
        int idFrom = cursor.getInt(2);
        int idTo = cursor.getInt(3);
        double amount = cursor.getDouble(4);
        String fullName = cursor.getString(5);

        return new Debt(id, idBalancing, idFrom, idTo, amount, fullName);
    }*/

    private Debt cursorToDebtWithFullNamesAndEmails(Cursor cursor) {
        int id = cursor.getInt(0);
        int idBalancing = cursor.getInt(1);
        int idFrom = cursor.getInt(2);
        int idTo = cursor.getInt(3);
        double amount = cursor.getDouble(4);
        String fullNameFrom = cursor.getString(5);
        String fullNameTo = cursor.getString(6);
        String emailFrom = cursor.getString(7);
        String emailTo = cursor.getString(8);

        return new Debt(id, idBalancing, idFrom, idTo, amount, fullNameFrom, fullNameTo, emailFrom, emailTo);
    }

    public DebtModel insertDebt(DebtModel data) {

        database.replace(AppDB.Debts.TABLE_DEBTS, null,
               debtToValues(data));
        // now read from DB the inserted person and return it
        Cursor cursor = database.query(AppDB.Debts.TABLE_DEBTS,allColumns,
                AppDB.Debts._ID + " = ?",
                new String[] {String.valueOf(data.getId())},null,null,null);
        cursor.moveToFirst();
        DebtModel d = cursorToDebt(cursor);
        cursor.close();
        return d;

    }

    public void resetAllDebts(int idGroup) {
       database.delete(AppDB.Debts.TABLE_DEBTS, AppDB.Debts.COLUMN_ID_GROUP + " = ?" , new String[]{String.valueOf(idGroup)});
    }

    /*
    public List<Debt> getAllDebtsFrom(int idGroup, int idUser) {
        List<Debt> data = new ArrayList<Debt>();
        //String query = "SELECT idTo, amount FROM groups LEFT JOIN balancings ON groups._id = balancings.idGroup LEFT JOIN transactions ON balancings._id = transactions.idBalancing WHERE groups._id = ? AND transactions.idFrom = ?";
        String query = "SELECT transactions._id, transactions.amount, transactions.idBalancing, transactions.idFrom, transactions.idTo, transactions.payed_at, users.fullName " +
                "FROM groups LEFT JOIN balancings ON groups._id = balancings.idGroup LEFT JOIN transactions ON balancings._id = transactions.idBalancing LEFT JOIN users ON transactions.idTo = users._id "+
                "WHERE groups._id = ? AND  (transactions.idFrom = ? OR transactions.idTo = ?)";
        String[] values =  new String[]{String.valueOf(idGroup), String.valueOf(idUser),String.valueOf(idUser)};
        Cursor cursor = database.rawQuery(query, values);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Debt d = cursorToDebtWithFullName(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }*/

    public List<DebtModel> getAllDebts(int idGroup) {
        List<DebtModel> data = new ArrayList<DebtModel>();
        Cursor cursor = database.query(AppDB.Debts.TABLE_DEBTS,
                allColumns,AppDB.Debts.COLUMN_ID_GROUP + " = ? ;",new String[] {String.valueOf(idGroup)},null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            DebtModel d = cursorToDebt(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }

    public List<Debt> getAllDebtsExtra(int idGroup) {
        List<Debt> data = new ArrayList<Debt>();
        Cursor cursor = database.query("debts LEFT JOIN users users1 ON debts.idFrom = users1._id LEFT JOIN users users2 ON debts.idTo = users2._id",
                allColumnsExtra,AppDB.Debts.COLUMN_ID_GROUP + " = ? ;",new String[] {String.valueOf(idGroup)},null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Debt d = cursorToDebtWithFullNamesAndEmails(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }



}
