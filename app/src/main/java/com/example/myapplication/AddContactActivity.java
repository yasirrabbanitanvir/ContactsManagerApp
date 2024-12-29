package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddContactActivity extends AppCompatActivity {

    EditText nameBox;
    EditText phoneBox;
    EditText emailBox;
    SQLiteDatabase db;
    ContactsDbHelper sqlHelper;

    private static final String TAG = "AddContactActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        nameBox = findViewById(R.id.nameTextAdd);
        phoneBox = findViewById(R.id.phoneTextAdd);
        emailBox = findViewById(R.id.emailTextAdd);

        Log.d(TAG, "nameBox: " + nameBox + ", phoneBox: " + phoneBox + ", emailBox: " + emailBox);

        // Initialize database
        sqlHelper = new ContactsDbHelper(this);
        db = sqlHelper.getWritableDatabase();

        Log.d(TAG, "Database initialized: " + db);

        // Save button logic
        Button saveButton = findViewById(R.id.saveButtonAdd);
        saveButton.setOnClickListener(v -> {
            String addName = nameBox != null && nameBox.getText() != null ? nameBox.getText().toString().trim() : "";
            String addPhone = phoneBox != null && phoneBox.getText() != null ? phoneBox.getText().toString().trim() : "";
            String addEmail = emailBox != null && emailBox.getText() != null ? emailBox.getText().toString().trim() : "";

            Log.d(TAG, "Inputs - Name: " + addName + ", Phone: " + addPhone + ", Email: " + addEmail);

            if (addName.isEmpty()) {
                Toast.makeText(AddContactActivity.this, "Name field is required", Toast.LENGTH_SHORT).show();
                return;
            } else if (addPhone.isEmpty() && addEmail.isEmpty()) {
                Toast.makeText(AddContactActivity.this, "Phone or Email field is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db == null) {
                Toast.makeText(AddContactActivity.this, "Database not initialized", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for duplicates
            if (isDuplicate(addName, addPhone, addEmail)) {
                Toast.makeText(AddContactActivity.this, "Duplicate contact found. Please use different details.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Insert into database
            ContentValues cv = new ContentValues();
            cv.put(ContactsDbHelper.COLUMN_NAME, addName);
            cv.put(ContactsDbHelper.COLUMN_PHONE, addPhone);
            cv.put(ContactsDbHelper.COLUMN_EMAIL, addEmail);

            long rowId = db.insert(ContactsDbHelper.TABLE, null, cv);
            if (rowId == -1) {
                Toast.makeText(AddContactActivity.this, "Error saving contact", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Database insert failed");
            } else {
                Toast.makeText(AddContactActivity.this, "Contact saved successfully", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Contact inserted with ID: " + rowId);
                goHome();
            }
        });

        // Back button logic
        Button backButton = findViewById(R.id.backButtonAdd);
        backButton.setOnClickListener(v -> goHome());
    }

    private boolean isDuplicate(String name, String phone, String email) {
        Cursor cursor = db.query(
                ContactsDbHelper.TABLE,
                null,
                ContactsDbHelper.COLUMN_NAME + "=? OR " +
                        ContactsDbHelper.COLUMN_PHONE + "=? OR " +
                        ContactsDbHelper.COLUMN_EMAIL + "=?",
                new String[]{name, phone, email},
                null,
                null,
                null
        );

        boolean isDuplicate = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        return isDuplicate;
    }

    private void goHome() {
        if (db != null) {
            db.close();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
