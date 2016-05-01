package com.adisoftwares.bookreader;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.pdf.PDFBookData;

import java.io.File;

/**
 * Created by adityathanekar on 02/05/16.
 */
public class LastAddedFragment extends BookFragment {
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = null;
        String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String selection = MediaStore.Files.FileColumns.DATA + " LIKE ?";
        Bundle arguments = getArguments();
        String bookName = null;
        if (arguments != null)
            bookName = args.getString(TITLE);
        if (bookName == null)
            bookName = "";
        String[] selectionArgs = new String[]{"%" + bookName + ".pdf"};
        //String[] selectionArgs = new String[]{"%.pdf"};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }
}
