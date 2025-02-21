package com.example.mycontacts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class ContactsDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "ContactsDbHelper";
    private static final String DATABASE_NAME = "contacts.db";
    private static final int SCHEMA = 1;

    public static final String TABLE = "contacts";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_EMAIL = "email";

    // Users table
    public static final String USERS_TABLE = "users";
    public static final String USERS_COLUMN_ID = "_id";
    public static final String USERS_COLUMN_USERNAME = "username";
    public static final String USERS_COLUMN_PASSWORD = "password";

    public ContactsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA);
        Log.d(TAG, "Database helper created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables...");

        // Create contacts table
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_EMAIL + " TEXT)");

        // Create users table
        db.execSQL("CREATE TABLE " + USERS_TABLE + " (" +
                USERS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                USERS_COLUMN_USERNAME + " TEXT UNIQUE, " +
                USERS_COLUMN_PASSWORD + " TEXT)");

        Log.d(TAG, "Tables created successfully. Adding initial data...");


        String[] names = {
                "Yasir", "Roky", "Polok", "Pritam", "Shuvo", "Raihan", "Rasel",
                "Alamin", "Sohag", "Emon"
        };

        String[] phones = {
                "01752822254", "01752820029", "01752828000", "01752830000", "01752831111",
                "01752832222", "01752833333", "01752834444", "01752835555", "01752836666"
        };

        String[] emails = {
                "yasir@gmail.com", "roky@gmail.com", "polok@gmail.com", "pritam@gmail.com",
                "shuvo@gmail.com", "raihan@gmail.com", "rasel@gmail.com", "alamin@gmail.com",
                "sohag@gmail.com", "emon@gmail.com"
        };


        for (int i = 0; i < names.length; i++) {
            String query = "INSERT INTO " + TABLE + " (" + COLUMN_NAME + ", " + COLUMN_PHONE + ", " + COLUMN_EMAIL + ") " +
                    "VALUES ('" + names[i] + "', '" + phones[i] + "', '" + emails[i] + "')";
            db.execSQL(query);
        }

        // Pre-inserted user information
        String defaultUser = "INSERT INTO " + USERS_TABLE + " (" + USERS_COLUMN_USERNAME + ", " + USERS_COLUMN_PASSWORD + ") " +
                "VALUES ('admin', 'pass123')";
        db.execSQL(defaultUser);

        Log.d(TAG, "Initial data added successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database...");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE);
        onCreate(db);
    }

    /**
     * Fetch all contacts ordered by name.
     *
     * @return List of contacts as HashMaps.
     */
    public ArrayList<HashMap<String, String>> getAllContactsAlphabetically() {
        ArrayList<HashMap<String, String>> contactList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query for select all contacts
        Cursor cursor = db.query(TABLE, null, null, null, null, null, COLUMN_NAME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> contact = new HashMap<>();
                contact.put(COLUMN_ID, cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                contact.put(COLUMN_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                contact.put(COLUMN_PHONE, cursor.getString(cursor.getColumnIndex(COLUMN_PHONE)));
                contact.put(COLUMN_EMAIL, cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL)));
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contactList;
    }
}