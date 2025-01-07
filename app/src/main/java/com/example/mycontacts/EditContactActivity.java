package com.example.mycontacts;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditContactActivity extends AppCompatActivity {
    SQLiteDatabase db;
    ContactsDbHelper sqlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        // Retrieve data
        long contactId = getIntent().getLongExtra("contactId", 0);
        String name = getIntent().getStringExtra("name");
        String phone = getIntent().getStringExtra("phone");
        String email = getIntent().getStringExtra("email");

        Log.d("EditContactActivity", "Contact ID: " + contactId + ", Name: " + name + ", Phone: " + phone + ", Email: " + email);

        // Initialize Views
        EditText nameEditText = findViewById(R.id.nameTextEdit);
        EditText phoneEditText = findViewById(R.id.phoneTextEdit);
        EditText emailEditText = findViewById(R.id.emailTextEdit);

        nameEditText.setText(name);
        phoneEditText.setText(phone);
        emailEditText.setText(email);

        // Initialize Database
        sqlHelper = new ContactsDbHelper(this);
        db = sqlHelper.getWritableDatabase();

        if (db == null) {
            Log.e("EditContactActivity", "Database initialization failed!");
            Toast.makeText(this, "Database error. Please restart the app.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Save Button Action
        Button saveButton = findViewById(R.id.saveButtonEdit);
        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString();
            String updatedPhone = phoneEditText.getText().toString();
            String updatedEmail = emailEditText.getText().toString();

            if (updatedName.isEmpty()) {
                Toast.makeText(EditContactActivity.this, "Name field is required", Toast.LENGTH_SHORT).show();
                return;
            } else if (updatedPhone.isEmpty() && updatedEmail.isEmpty()) {
                Toast.makeText(EditContactActivity.this, "Phone or Email field is required", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update Contact in Database
            ContentValues cv = new ContentValues();
            cv.put(ContactsDbHelper.COLUMN_NAME, updatedName);
            cv.put(ContactsDbHelper.COLUMN_PHONE, updatedPhone);
            cv.put(ContactsDbHelper.COLUMN_EMAIL, updatedEmail);

            int rowsUpdated = db.update(ContactsDbHelper.TABLE, cv, ContactsDbHelper.COLUMN_ID + "=?", new String[]{String.valueOf(contactId)});
            if (rowsUpdated > 0) {
                Toast.makeText(EditContactActivity.this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(EditContactActivity.this, "Failed to update contact", Toast.LENGTH_SHORT).show();
            }

            goHome();
        });

        // Back Button Action
        Button backButton = findViewById(R.id.backButtonEdit);
        backButton.setOnClickListener(v -> goHome());
    }

    private void goHome() {
        if (db != null) db.close();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}
