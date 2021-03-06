package it.unibs.appwow.database;

import android.provider.BaseColumns;

/**
 * This class contains all strings for our DB.
 */
public final class AppDB {
    public static final String DATABASE_NAME = "localDatabase.db";
    public static final int DATABASE_VERSION = 1;

    private AppDB() {
    }

    public static class Users implements BaseColumns {
        public static final String TABLE_USERS = "users";
        public static final String COLUMN_FULLNAME = "fullName";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
    }

    public static class Groups implements BaseColumns{
        public static final String TABLE_GROUPS = "groups";
        public static final String COLUMN_ID_ADMIN = "idAdmin";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHOTO = "photo";
        public static final String COLUMN_PHOTO_UPDATED_AT = "photo_updated_at";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
        public static final String COLUMN_HIGHLIGHTED = "highlighted";
    }

    public static class UserGroup{
        public static final String TABLE_USER_GROUP = "user_group";
        public static final String COLUMN_ID_USER = "idUser";
        public static final String COLUMN_ID_GROUP = "idGroup";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_UPDATED_AT = "updatedAt";
    }


    public static class Payments implements BaseColumns{
        public static final String TABLE_PAYMENTS = "payments";
        public static final String COLUMN_ID_GROUP = "idGroup";
        public static final String COLUMN_ID_USER = "idUser";
        public static final String COLUMN_ID_USER_TO = "idUserTo";
        public static final String COLUMN_AMOUNT = "amount";
        public static final String COLUMN_CURRENCY = "currency";
        public static final String COLUMN_FORALL = "forAll";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_UPDATED_AT = "updated_at";
        public static final String COLUMN_IS_EXCHANGE = "isExchange";
        public static final String COLUMN_POSITION = "position";
        public static final String COLUMN_POSITION_ID = "position_id";
        public static final String COLUMN_AMOUNT_DETAILS = "amount_details";
    }

    public static class Debts implements BaseColumns{
        public static final String TABLE_DEBTS = "debts";
        public static final String COLUMN_ID_GROUP = "idGroup";
        public static final String COLUMN_ID_FROM = "idFrom";
        public static final String COLUMN_ID_TO = "idTo";
        public static final String COLUMN_AMOUNT = "amount";
    }
}
