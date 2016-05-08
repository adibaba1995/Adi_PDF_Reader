package com.adisoftwares.bookreader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;

import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.R;

/**
 * Created by adityathanekar on 19/03/16.
 */
public class BookDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public BookDbHelper(Context context) {
        super(context, context.getString(R.string.database_name), null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_BOOKMARKS_TABLE = BookReaderApplication.getContext().getString(R.string.create_bookmarks_table_query, BookContract.BookmarkEntry.TABLE_NAME, BookContract.BookmarkEntry._ID, BookContract.BookmarkEntry.COLUMN_PATH, BookContract.BookmarkEntry.COLUMN_FILE_NAME, BookContract.BookmarkEntry.COLUMN_PAGE_NO, BookContract.BookmarkEntry.COLUMN_BOOKMARK_NAME, BookContract.BookmarkEntry.COLUMN_TIME);

//        final String SQL_CREATE_BOOKMARKS_TABLE = "CREATE TABLE IF NOT EXISTS " + BookContract.BookmarkEntry.TABLE_NAME + " ("
//                + BookContract.BookmarkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + BookContract.BookmarkEntry.COLUMN_PATH + " TEXT, "
//                + BookContract.BookmarkEntry.COLUMN_FILE_NAME+ " TEXT, "
//                + BookContract.BookmarkEntry.COLUMN_PAGE_NO + " INTEGER, "
//                + BookContract.BookmarkEntry.COLUMN_BOOKMARK_NAME + " TEXT, "
//                + BookContract.BookmarkEntry.COLUMN_TIME + " TEXT)";

        final String SQL_CREATE_RECENTS_TABLE = BookReaderApplication.getContext().getString(R.string.create_recents_table_query, BookContract.RecentsEntry.TABLE_NAME, BookContract.RecentsEntry._ID, BookContract.RecentsEntry.COLUMN_PATH, BookContract.RecentsEntry.COLUMN_FILE_NAME, BookContract.RecentsEntry.COLUMN_ADD_TIME);

//        final String SQL_CREATE_RECENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + BookContract.RecentsEntry.TABLE_NAME + " ("
//                + BookContract.RecentsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//                + BookContract.RecentsEntry.COLUMN_PATH + " TEXT, "
//                + BookContract.RecentsEntry.COLUMN_FILE_NAME+ " TEXT, "
//                + BookContract.RecentsEntry.COLUMN_ADD_TIME + " TEXT)";

        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE);
        db.execSQL(SQL_CREATE_RECENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
