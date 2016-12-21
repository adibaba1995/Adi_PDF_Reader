package com.adisoftwares.bookreader.pdf.reader.books.pdf;

import android.app.Fragment;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.reader.books.BookReaderApplication;
import com.adisoftwares.bookreader.pdf.reader.books.R;
import com.adisoftwares.bookreader.pdf.reader.books.database.BookContract;
import com.adisoftwares.bookreader.pdf.reader.books.view.AutofitRecyclerView;
import com.artifex.mupdfdemo.MuPDFCore;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 06/05/16.
 */
//This class displays the bookmarks of a particular book.
public class BookmarkFragment extends Fragment implements ThumbnailAdapter.OnRecyclerViewItemSelected, LoaderManager.LoaderCallbacks<Cursor>, BookmarkAdapter.OnRecyclerViewItemSelected {

    private OutlineItemSelected outlineItemSelected;

    public static final int BOOKMARKS_LOADER = 1;

    @BindView(R.id.thumbnail_recycler_view)
    AutofitRecyclerView thumbnailRecyclerView;
    @BindView(R.id.fragment_container)
    FrameLayout thumbnailRecyclerViewContainer;

    private MuPDFCore mCore;
    private Unbinder unbinder;

    private ArrayList<Integer> bookmarkList;
    BookmarkAdapter adapter;

    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.thumbnail_fragment, container, false);

        mCore = (MuPDFCore)getArguments().getSerializable(getString(R.string.pdf_core));

        unbinder = ButterKnife.bind(this, rootView);

        bookmarkList = new ArrayList<>();

        adapter = new BookmarkAdapter(mCore, getActivity(), getArguments().getString(getString(R.string.pdf_file_path)), null);
        adapter.setRecyclerViewCallbacks(this);

        thumbnailRecyclerView.setAdapter(adapter);

        ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(BOOKMARKS_LOADER, null, this);
        return rootView;
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }

    public static BookmarkFragment newInstance(MuPDFCore muPDFCore, String path) {
        BookmarkFragment fragment = new BookmarkFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookReaderApplication.getContext().getString(R.string.pdf_core), muPDFCore);
        args.putString(BookReaderApplication.getContext().getString(R.string.pdf_file_path), path);
        fragment.setArguments(args);
        return fragment;
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
        String[] selectionArgs = new String[]{getArguments().getString(getString(R.string.pdf_file_path))};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.getCount() == 0) {
            emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null, false);
            ((TextView) emptyView.findViewById(R.id.empty_text)).setText(R.string.no_bookmarks);
            thumbnailRecyclerViewContainer.addView(emptyView);
        }
        else {
            adapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}

