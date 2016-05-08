package com.adisoftwares.bookreader;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adisoftwares.bookreader.BookFragment;
import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.pdf.PDFBookData;

import java.io.File;

/**
 * Created by adityathanekar on 02/05/16.
 */
public class RecentsFragment extends BookFragment {

    LastReadAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  super.onCreateView(inflater, container, savedInstanceState);
        adapter = new LastReadAdapter(getActivity(), null);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                filesCursor.moveToPosition(position);
                openBook(filesCursor.getString(filesCursor.getColumnIndex(BookContract.RecentsEntry.COLUMN_PATH)));
            }
        });
        recyclerView.setAdapter(adapter);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = BookContract.RecentsEntry.CONTENT_URI;
        String[] projection = null;
        String sortOrder = BookContract.RecentsEntry.COLUMN_ADD_TIME + " DESC";
        String selection = null;
        String[] selectionArgs = null;

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        filesCursor = data;
        adapter.swapCursor(data);
    }
}
