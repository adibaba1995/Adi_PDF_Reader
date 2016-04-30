package com.adisoftwares.bookreader.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by adityathanekar on 19/03/16.
 */
public class BookContentProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = null;// = buildUriMatcher();
    private BookDbHelper mOpenHelper;

    static final int BOOKMARKS = 100;
    static final int BOOKMARKS_BY_BOOK_NAME = 101;

    private static final SQLiteQueryBuilder sBookmarksByBookNameQueryBuilder;

    private static final String sBookWithId =
            BookContract.BookmarkEntry.TABLE_NAME +
                    "." + BookContract.BookmarkEntry._ID + " = ? ";

    static {
        sBookmarksByBookNameQueryBuilder = new SQLiteQueryBuilder();
        sBookmarksByBookNameQueryBuilder.setTables(BookContract.BookmarkEntry.TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BookDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case BOOKMARKS_BY_BOOK_NAME:
                retCursor = getBooksById(uri, projection, sortOrder);
                break;
            case BOOKMARKS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BookContract.BookmarkEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getBooksById(
            Uri uri, String[] projection, String sortOrder) {
        long movieId = BookContract.getIdFromUri(uri);

        return sBookmarksByBookNameQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sBookWithId,
                new String[]{String.valueOf(movieId)},
                null,
                null,
                sortOrder
        );
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOKMARKS:
                return BookContract.BookmarkEntry.CONTENT_TYPE;
//            case BOOKMARKS_WITH_ID:
//                return BookContract.BookmarkEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case BOOKMARKS:
                long _id = db.insert(BookContract.BookmarkEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = BookContract.BookmarkEntry.buildBookUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        //Note: we should not return uri instead we should use passed in uri because otherwise it will not correctly notify cursors of the change.
        getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int noOfRowsDeleted = 0;
        if (selection == null)
            selection = "1";
        long id = BookContract.getIdFromUri(uri);
//        switch (match) {
//            case BOOKMARKS_WITH_ID:
//                noOfRowsDeleted = db.delete(BookContract.BookmarkEntry.TABLE_NAME, BookContract.BookmarkEntry._ID + "=?", new String[]{String.valueOf(id)});
//                break;
//            default:
//                throw new UnsupportedOperationException("Unknown uri: " + uri);
//        }
        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.
        if (noOfRowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        db.close();
        // Student: return the actual rows deleted
        return noOfRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

//    static UriMatcher buildUriMatcher() {
//        // 1) The code passed into the constructor represents the code to return for the root
//        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.
//        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
//        final String authority = BookContract.CONTENT_AUTHORITY;
//
//        // 2) Use the addURI function to match each of the types.  Use the constants from
//        // WeatherContract to help define the types to the UriMatcher.
//        matcher.addURI(authority, BookContract.PATH_ROUTINE, BOOKMARKS);
//        matcher.addURI(authority, BookContract.PATH_ROUTINE + "/#", BOOKMARKS_WITH_ID);
//
//        // 3) Return the new matcher!
//        return matcher;
//    }
}
