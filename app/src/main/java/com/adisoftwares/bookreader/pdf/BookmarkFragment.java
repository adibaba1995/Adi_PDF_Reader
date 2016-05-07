package com.adisoftwares.bookreader.pdf;

import android.app.Fragment;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.view.AutofitRecyclerView;
import com.artifex.mupdfdemo.MuPDFCore;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 06/05/16.
 */
public class BookmarkFragment extends Fragment implements ThumbnailAdapter.OnRecyclerViewItemSelected, LoaderManager.LoaderCallbacks<Cursor>, BookmarkAdapter.OnRecyclerViewItemSelected {

    private OutlineItemSelected outlineItemSelected;

    public static final String FILE_PATH = "com.adisoftwares.bookreader.pdf.file_path";
    public static final String CORE_OBJECT = "com.adisoftwares.bookreader.pdf.core";

    public static final int BOOKMARKS_LOADER = 1;

    @BindView(R.id.thumbnail_recycler_view)
    AutofitRecyclerView thumbnailRecyclerView;

    private MuPDFCore mCore;
    private Unbinder unbinder;

    private ArrayList<Integer> bookmarkList;
    BookmarkAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.thumbnail_fragment, container, false);

        mCore = (MuPDFCore)getArguments().getSerializable(CORE_OBJECT);

        unbinder = ButterKnife.bind(this, rootView);

        bookmarkList = new ArrayList<>();

        adapter = new BookmarkAdapter(mCore, getActivity(), getArguments().getString(FILE_PATH), bookmarkList);
        adapter.setRecyclerViewCallbacks(this);

        thumbnailRecyclerView.setAdapter(adapter);

        ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(BOOKMARKS_LOADER, null, this);
        return rootView;
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }

    public void setData(MuPDFCore muPDFCore, String path) {
        Bundle args = new Bundle();
        args.putSerializable(CORE_OBJECT, muPDFCore);
        args.putString(FILE_PATH, path);
        setArguments(args);
    }

    @Override
    public void onRecyclerViewItemSelected(int position) {
        if(outlineItemSelected != null)
            outlineItemSelected.outlineItemSelected(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = BookContract.BookmarkEntry.CONTENT_URI;
        String[] projection = null;
        String sortOrder = BookContract.BookmarkEntry.COLUMN_PAGE_NO + " ASC";
        String selection = BookContract.BookmarkEntry.COLUMN_PATH + " = ?";
        String[] selectionArgs = new String[]{getArguments().getString(FILE_PATH)};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (bookmarkList != null)
            bookmarkList.clear();
        if(data.isBeforeFirst())
            while (data.moveToNext()) {
                Log.d("Aditya", String.valueOf(data.getInt(data.getColumnIndex(BookContract.BookmarkEntry.COLUMN_PAGE_NO))));
                bookmarkList.add(data.getInt(data.getColumnIndex(BookContract.BookmarkEntry.COLUMN_PAGE_NO)));
            }
        adapter.notifyDataSetChanged();

//        bookLoaderTask = new BookLoaderTask();
//        bookLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

