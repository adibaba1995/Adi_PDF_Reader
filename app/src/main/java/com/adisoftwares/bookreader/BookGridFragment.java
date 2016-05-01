package com.adisoftwares.bookreader;

import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.provider.MediaStore.Files;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 03/02/16.
 */
public class BookGridFragment extends BookFragment implements BookFragment.DataLoadedListener {

    @Bind(R.id.read_last)
    FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView =  super.onCreateView(inflater, container, savedInstanceState);

        ButterKnife.bind(this, rootView);

        setDataLoadedListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_grid_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        final SearchSuggestionsAdapter adapter = new SearchSuggestionsAdapter(getActivity(), null, 0);
        searchView.setSuggestionsAdapter(adapter);
        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                cursor.moveToPosition(position);
                openBook(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)));
                return true;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                BusStation.getBus().post(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {

                final Handler messageHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        if (cursor != null) {
                            adapter.changeCursor(cursor);
                        }
                    }
                };

                new Thread() {
                    public void run() {
                        Uri uri = MediaStore.Files.getContentUri("external");
                        String[] projection = null;
                        String sortOrder = Files.FileColumns.DATA + " ASC";
                        String selection = Files.FileColumns.DATA + " LIKE ?";
                        String[] selectionArgs = new String[]{"/%" + newText + "%.pdf"};
                        cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                        messageHandler.sendEmptyMessage(0);
                    }
                }.start();

                adapter.changeCursor(cursor);
                return true;
            }
        });
    }

    @Override
    public void dataLoaded() {
        if(booksList.size() == 0 || !getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).contains(Preference.LAST_READ_BOOK_PATH))
            fab.setVisibility(View.INVISIBLE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBook(getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(Preference.LAST_READ_BOOK_PATH, ""));
            }
        });
    }
}
