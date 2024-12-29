package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.CallLog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.net.Uri;


import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    ListView listView;
    Button addButton, logoutButton;
    ContactsDbHelper databaseHelper;
    SQLiteDatabase db;
    Cursor cursor;
    SimpleCursorAdapter simpleCursorAdapter;
    boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check login status
        if (!isLoggedIn) {
            setContentView(R.layout.activity_login);

            Button loginButton = findViewById(R.id.loginButton);
            Button signupButton = findViewById(R.id.signupButton);  // Signup button

            loginButton.setOnClickListener(v -> handleLogin());

            signupButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            });
        } else {
            initMainScreen();
        }
    }

    private void handleLogin() {
        String username = ((android.widget.EditText) findViewById(R.id.usernameInput)).getText().toString().trim();
        String password = ((android.widget.EditText) findViewById(R.id.passwordInput)).getText().toString().trim();

        if (validateLogin(username, password)) {
            isLoggedIn = true;
            initMainScreen();
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMainScreen() {
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        logoutButton = findViewById(R.id.logoutButton);
        listView = findViewById(R.id.contactListView);
        databaseHelper = new ContactsDbHelper(getApplicationContext());

        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddContactActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            getMenuInflater().inflate(R.menu.menu_logout, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_logout_confirm) {
                    performLogout();
                    return true;
                } else if (item.getItemId() == R.id.menu_logout_cancel) {
                    Toast.makeText(MainActivity.this, "Logout cancelled", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            view.setSelected(true);
            showPopupMenu(view, id);
        });

        fetchDataAndDisplay();
    }

    private void fetchDataAndDisplay() {
        db = databaseHelper.getReadableDatabase();
        cursor = db.rawQuery("SELECT * FROM " + ContactsDbHelper.TABLE + " ORDER BY " + ContactsDbHelper.COLUMN_NAME + " ASC", null);

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

    private void performLogout() {
        Toast.makeText(this, "You have been logged out!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateLogin(String username, String password) {
        if ("admin".equals(username) && "1234".equals(password)) {
            return true;
        }
        return false;
    }

    private void showPopupMenu(View view, final long contactId) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.context_menu);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_edit) {
                editContact(contactId);
                return true;
            } else if (itemId == R.id.menu_delete) {
                deleteContact(contactId);
                return true;
            } else if (itemId == R.id.menu_call) {
                callContact(contactId);
                return true;
            } else if (itemId == R.id.menu_message) {
                messageContact(contactId);
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void editContact(long contactId) {
        cursor = db.rawQuery(
                "SELECT * FROM " + ContactsDbHelper.TABLE + " WHERE " + ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_EMAIL));

            Intent intent = new Intent(this, EditContactActivity.class);
            intent.putExtra("contactId", contactId);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("email", email);

            startActivity(intent);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void deleteContact(long contactId) {
        int rowsDeleted = db.delete(ContactsDbHelper.TABLE, ContactsDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});

        if (rowsDeleted > 0) {
            Toast.makeText(this, "Contact deleted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error deleting contact", Toast.LENGTH_SHORT).show();
        }

        refreshList();
    }

    private void callContact(long contactId) {
        cursor = db.rawQuery(
                "SELECT * FROM " + ContactsDbHelper.TABLE + " WHERE " + ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
            startActivity(intent);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void messageContact(long contactId) {
        cursor = db.rawQuery(
                "SELECT * FROM " + ContactsDbHelper.TABLE + " WHERE " + ContactsDbHelper.COLUMN_ID + "=?",
                new String[]{String.valueOf(contactId)}
        );

        if (cursor != null && cursor.moveToFirst()) {
            String phone = cursor.getString(cursor.getColumnIndex(ContactsDbHelper.COLUMN_PHONE));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phone));
            startActivity(intent);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void refreshList() {
        cursor = db.rawQuery("SELECT * FROM " + databaseHelper.TABLE + " ORDER BY " + ContactsDbHelper.COLUMN_NAME + " ASC", null);
        simpleCursorAdapter.changeCursor(cursor);
        simpleCursorAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isLoggedIn) {
            fetchDataAndDisplay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (cursor != null) {
            cursor.close();
        }

        if (db != null) {
            db.close();
        }
    }
}