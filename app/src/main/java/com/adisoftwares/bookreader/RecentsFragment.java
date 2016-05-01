package com.adisoftwares.bookreader;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.adisoftwares.bookreader.BookFragment;
import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.pdf.PDFBookData;

import java.io.File;

/**
 * Created by adityathanekar on 02/05/16.
 */
public class RecentsFragment extends BookFragment {

    private BookLoaderTask bookLoaderTask;

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
        bookLoaderTask = new BookLoaderTask();
        bookLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    }

    public class BookLoaderTask extends AsyncTask<Cursor, Integer, Void> {

        @Override
        protected Void doInBackground(Cursor... params) {
            BookData bookData;
            Cursor data = params[0];
            File book;
            while (data.moveToNext()) {
                if (isCancelled()) {
                    break;
                }
                bookData = null;
                book = new File(data.getString(data.getColumnIndex(BookContract.RecentsEntry.COLUMN_PATH)));
                try {
                    if (book.isDirectory()) {
                        continue;
                    } else {
                        if (book.getAbsolutePath().endsWith(".pdf"))
                            bookData = new PDFBookData(data.getString(data.getColumnIndex(BookContract.RecentsEntry.COLUMN_PATH)));
                        else
                            continue;
                        bookData.setId(data.getLong(data.getColumnIndex(BookContract.RecentsEntry._ID)));
                        booksList.add(bookData);
                        publishProgress(booksList.size() - 1);
                    }
                } catch (Exception e) {
                    Log.d("Aditya", e.toString());
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            adapter.notifyItemInserted(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(booksList.size() == 0) {
                recyclerViewContainer.addView(emptyView);
            }
            if(dataLoadedListener != null)
                dataLoadedListener.dataLoaded();
        }
    }
}
