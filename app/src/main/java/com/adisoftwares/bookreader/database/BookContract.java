package com.adisoftwares.bookreader.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by adityathanekar on 19/03/16.
 */
public class BookContract {

    public static final String CONTENT_AUTHORITY = "com.adisoftwares.bookreader";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PDF_BOOKMARK = "pdf_bookmark";
    public static final String PATH_PDF_RECENTS = "pdf_recents";

    public static long getIdFromUri(Uri uri) {
        return Long.valueOf(uri.getPathSegments().get(1));
    }

    public static final class BookmarkEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PDF_BOOKMARK).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PDF_BOOKMARK;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PDF_BOOKMARK;

        public static final String TABLE_NAME = "bookmark";

        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_FILE_NAME = "filename";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_BOOKMARK_NAME = "bookmark_name";
        public static final String COLUMN_PAGE_NO = "pageno";

        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class RecentsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PDF_RECENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PDF_RECENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PDF_RECENTS;

        public static final String TABLE_NAME = "recents";

        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_FILE_NAME = "filename";
        public static final String COLUMN_ADD_TIME = "addtime";

        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
