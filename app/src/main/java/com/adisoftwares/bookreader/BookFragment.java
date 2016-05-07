package com.adisoftwares.bookreader;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Files;
import android.widget.Button;
import android.widget.FrameLayout;

import com.adisoftwares.bookreader.pdf.PDFBookData;
import com.adisoftwares.bookreader.pdf.PdfViewActivity;
import com.adisoftwares.bookreader.view.AutofitRecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 03/02/16.
 */
public class BookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int FILES_LOADER = 0;

    protected ArrayList<BookData> booksList;

    protected BooksAdapter adapter;

    protected BookLoaderTask bookLoaderTask;

    protected View emptyView;
    private View errorView;

    protected Cursor cursor = null;

    private int scrollPosition;

    @BindView(R.id.books_recycler_view)
    AutofitRecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_container)
    FrameLayout recyclerViewContainer;
    @BindView(R.id.read_last)
    FloatingActionButton fab;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(getString(R.string.book_list), booksList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.book_grid_fragment, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            booksList = savedInstanceState.getParcelableArrayList(getString(R.string.book_list));
            Log.d("Aditya", "The size of list is " + booksList.size());
        }
        else
            booksList = new ArrayList<>();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            errorView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, recyclerView, false);
            ((Button) errorView.findViewById(R.id.error_button)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentCompat.requestPermissions(BookFragment.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_EXTERNAL_STORAGE);
                }
            });
        }

        emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, recyclerView, false);

        adapter = new BooksAdapter(getActivity(), booksList);
        recyclerView.setAdapter(adapter);
//        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
                                           @Override
                                           public void onItemClick(View view, int position) {
                                               String path = booksList.get(position).getPath();
                                               openBook(path);
                                           }
                                       }
        );

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            recyclerViewContainer.addView(errorView);
        } else {
            ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(FILES_LOADER, null, this);
        }

        ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBook(getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(Preference.LAST_READ_BOOK_PATH, ""));
            }
        });

        if(!getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).contains(Preference.LAST_READ_BOOK_PATH))
            fab.setVisibility(View.INVISIBLE);


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
                        String selection = Files.FileColumns.MIME_TYPE + " = ? AND " + Files.FileColumns.DATA + " LIKE ?";
                        String[] selectionArgs = new String[]{getString(R.string.pdf_mime_type), "%/%" + newText + "%"};
                        cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
                        messageHandler.sendEmptyMessage(0);
                    }
                }.start();

                adapter.changeCursor(cursor);
                return true;
            }
        });
    }

    protected void openBook(String path) {
        Intent intent;
        if (path.endsWith(".pdf")) {
            Uri uri = Uri.parse(path);
            intent = new Intent(getActivity(), PdfViewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getActivity(), R.anim.silde_in_left,R.anim.slide_out_right).toBundle();
            startActivity(intent, bndlanimation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    recyclerViewContainer.removeView(errorView);
                    ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(FILES_LOADER, null, this);
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Files.getContentUri("external");
        String[] projection = null;
        String sortOrder = null;
        String selection = Files.FileColumns.MIME_TYPE + " = ? AND " + Files.FileColumns.DATA + " LIKE ?";
        Bundle arguments = getArguments();
        String bookName = null;
        if (arguments != null)
            bookName = args.getString(getString(R.string.book_title));
        if (bookName == null)
            bookName = "";
        Log.d("Aditya", "Book name is " + bookName);
        String[] selectionArgs = new String[]{getString(R.string.pdf_mime_type), "%/%" + bookName + "%"};
        //String[] selectionArgs = new String[]{"%.pdf"};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookLoaderTask = new BookLoaderTask();
        bookLoaderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (bookLoaderTask != null)
//            bookLoaderTask.cancel(true);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((AppCompatActivity) getActivity()).getSupportLoaderManager().destroyLoader(FILES_LOADER);
    }

    public class BookLoaderTask extends AsyncTask<Cursor, Integer, Void> {

        @Override
        protected Void doInBackground(Cursor... params) {
            BookData bookData;
            Cursor data = params[0];
            File book;
            if (!data.isClosed() && data.isBeforeFirst()) {
                booksList.clear();
                while (data.moveToNext()) {
//                    if (isCancelled()) {
//                        break;
//                    }
                    bookData = null;
                    book = new File(data.getString(data.getColumnIndex(Files.FileColumns.DATA)));
                    try {
                        if (book.isDirectory()) {
                            continue;
                        } else {
                            if (book.getAbsolutePath().endsWith(".pdf"))
                                bookData = new PDFBookData(data.getString(data.getColumnIndex(Files.FileColumns.DATA)));
                            else
                                continue;
                            bookData.setId(data.getLong(data.getColumnIndex(Files.FileColumns._ID)));
                            booksList.add(bookData);
                            publishProgress(booksList.size() - 1);
                        }
                    } catch (Exception e) {
                        Log.d("Aditya", e.toString());
                    }
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
            recyclerViewContainer.removeAllViews();
            recyclerViewContainer.addView(recyclerView);
            if (booksList.size() == 0) {
                recyclerViewContainer.addView(emptyView);
            }
            recyclerView.scrollToPosition(scrollPosition);
        }
    }
}
