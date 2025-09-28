/*
 * File: AppDatabase.java
 * Purpose: Simple SQLite helper for user storage
 */
package com.example.evcharging.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppDatabase extends SQLiteOpenHelper {
    public static final String DB_NAME = "evcharging.db";
    public static final int DB_VERSION = 1;

    public AppDatabase(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table (local cache of EV owner)
        db.execSQL("CREATE TABLE IF NOT EXISTS users (nic TEXT PRIMARY KEY, name TEXT, email TEXT, phone TEXT, role TEXT, active INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // For now drop and recreate
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }
}
