package it.unibs.appwow.database;

import android.provider.BaseColumns;

/**
 * This class contains all strings for our DB.
 */
public final class AppDBOffline {
    public static final String DATABASE_NAME = "localDatabaseOffline.db";
    public static final int DATABASE_VERSION = 1;



    private AppDBOffline() {

    }

    public static class Users implements BaseColumns {
        public static final String TABLE_USERS = "users";
        public static final String COLUMN_FULLNAME = "fullName";
    }

    public static class Groups implements BaseColumns{
        public static final String TABLE_GROUPS = "groups";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_CREATED_AT = "created_at";
    }

    public static class UserGroup{
        public static final String TABLE_USER_GROUP = "user_group";
        public static final String COLUMN_ID_USER = "idUser";
        public static final String COLUMN_ID_GROUP = "idGroup";
        public static final String COLUMN_AMOUNT = "amount";
    }

    public static class Costs implements BaseColumns{
        public static final String TABLE_COSTS = "costs";
        public static final String COLUMN_ID_GROUP = "idGroup";
        public static final String COLUMN_ID_USER = "idUser";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_AMOUNT_DETAILS = "amount_details";
    }

    public static class Transactions implements BaseColumns{
        public static final String TABLE_TRANSACTIONS = "transactions";
        public static final String COLUMN_ID_FROM = "idFrom";
        public static final String COLUMN_ID_TO = "idTo";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_PAYED_AT = "payed_at";
    }
}
