package com.adisoftwares.bookreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by adityathanekar on 19/03/16.
 */
public class BookDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "books.db";

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + BookContract.BookmarkEntry.TABLE_NAME + " (" +
                BookContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BookContract.BookmarkEntry.COLUMN_PATH + " TEXT NOT NULL, " +
                BookContract.BookmarkEntry.COLUMN_FILE_NAME + " TEXT NOT NULL, " +
                BookContract.BookmarkEntry.COLUMN_BOOKMARK_NAME + " TEXT NOT NULL, " +
                BookContract.BookmarkEntry.COLUMN_TIME + " INTEGER NOT NULL, " +
                BookContract.BookmarkEntry.COLUMN_PAGE_NO + " INTEGER" +
                ");";

        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
