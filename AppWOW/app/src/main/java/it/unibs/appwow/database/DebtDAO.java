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
 * It is a DebtDAO that manages Debt Table
 */
public class DebtDAO implements LocalDB_DAO {
    private static final String TAG_LOG = DebtDAO.class.getName();

    private SQLiteDatabase database;
    private AppSQLiteHelper dbHelper;
    private final String[] allColumns = {
            AppDB.Debts._ID,
            AppDB.Debts.COLUMN_ID_GROUP,
            AppDB.Debts.COLUMN_ID_FROM,
            AppDB.Debts.COLUMN_ID_TO,
            AppDB.Debts.COLUMN_AMOUNT,
    };

    private final String[] allColumnsExtra = {
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

    /*
    public List<Debt> getAllDebtsExtra(int idGroup, boolean showOnlyYourDebts, int localUserId) {
        List<Debt> data = new ArrayList<Debt>();
        String whereDebts = "";
        String [] params = new String[] {String.valueOf(idGroup)};
        if(showOnlyYourDebts){
            whereDebts = " AND ( debts.idFrom = ? OR debts.idTo = ? ) ";
            params =  new String[]{String.valueOf(idGroup), String.valueOf(localUserId), String.valueOf(localUserId)};
        }

        Cursor cursor = database.query("debts LEFT JOIN users users1 ON debts.idFrom = users1._id LEFT JOIN users users2 ON debts.idTo = users2._id",
                allColumnsExtra,AppDB.Debts.COLUMN_ID_GROUP + " = ? " + whereDebts + ";", params,null,null,null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Debt d = cursorToDebtWithFullNamesAndEmails(cursor);
            data.add(d);
            cursor.moveToNext();
        }
        cursor.close(); // remember to always close the cursor!
        return data;
    }*/

    public List<Debt> getAllDebtsExtra(int idGroup){
        List<Debt> data = new ArrayList<Debt>();
        String [] params = new String[] {String.valueOf(idGroup)};

        Cursor cursor = database.query("debts LEFT JOIN users users1 ON debts.idFrom = users1._id LEFT JOIN users users2 ON debts.idTo = users2._id",
                allColumnsExtra, AppDB.Debts.COLUMN_ID_GROUP + " = ? ", params,null,null,null);
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
