package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ListView listView;
    Button addButton;
    ContactsDbHelper databaseHelper;
    SQLiteDatabase db;
    Cursor cursor;
    SimpleCursorAdapter simpleCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        addButton = findViewById(R.id.addButton);
        listView = findViewById(R.id.contactListView);
        databaseHelper = new ContactsDbHelper(getApplicationContext());

        Log.d(TAG, "Database helper initialized: " + databaseHelper);

        // Handle add button
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddContactActivity.class);
            startActivity(intent);
        });

        // Handle list item
        listView.setOnItemClickListener((parent, view, position, id) -> {
            view.setSelected(true);
            showPopupMenu(view, id);
        });
    }

    private void showPopupMenu(View view, final long contactId) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.context_menu);

        // popup menu
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                editContact(contactId);
                return true;
            } else if (itemId == R.id.menu_delete) {
                deleteContact(contactId);
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void editContact(long contactId) {

        cursor = db.rawQuery(
                "SELECT * FROM " + ContactsDbHelper.TABLE +
                        " WHERE " + ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            @SuppressLint("Range") String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

            Log.d(TAG, "Edit Contact - ID: " + contactId + ", Name: " + name + ", Phone: " + phone + ", Email: " + email);

            // Edit contacts
            Intent intent = new Intent(this, EditContactActivity.class);
            intent.putExtra("contactId", contactId);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("email", email);

            startActivity(intent);
        } else {
            Log.e(TAG, "No contact found with ID: " + contactId);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void deleteContact(long contactId) {
        // Delete contacts
        int rowsDeleted = db.delete(ContactsDbHelper.TABLE, ContactsDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Deleted contact with ID: " + contactId);
        } else {
            Toast.makeText(this, "Error deleting contact", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Failed to delete contact with ID: " + contactId);
        }

        refreshList();
    }

    private void refreshList() {
        // Refresh the contact list
        cursor = db.rawQuery("SELECT * FROM " + databaseHelper.TABLE + " ORDER BY " + ContactsDbHelper.COLUMN_NAME + " ASC", null);
        simpleCursorAdapter.changeCursor(cursor);
        simpleCursorAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();


        db = databaseHelper.getReadableDatabase();

        Log.d(TAG, "Database connection opened for reading.");

        // Fetch contacts in alphabetical order
        cursor = db.rawQuery("SELECT * FROM " + databaseHelper.TABLE + " ORDER BY " + ContactsDbHelper.COLUMN_NAME + " ASC", null);

        String[] headers = {
                ContactsDbHelper.COLUMN_NAME,
                ContactsDbHelper.COLUMN_PHONE,
                ContactsDbHelper.COLUMN_EMAIL
        };


        simpleCursorAdapter = new SimpleCursorAdapter(
                this,
                R.layout.list_item_contact,
                cursor,
                headers,
                new int[]{R.id.text1, R.id.text2, R.id.text3},
                0
        );

        listView.setAdapter(simpleCursorAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();


        if (cursor != null) {
            cursor.close();
            Log.d(TAG, "Cursor closed.");
        }


        if (db != null) {
            db.close();
            Log.d(TAG, "Database connection closed.");
        }
    }
}
