package com.adisoftwares.bookreader;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Files;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.PDFBookData;
import com.adisoftwares.bookreader.pdf.PdfViewActivity;
import com.adisoftwares.bookreader.view.AutofitRecyclerView;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 03/02/16.
 */
public class BookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String BOOK_LIST = "com.adisoftwares.bookreader.book_list";
    public static final String TITLE = "com.adisoftwares.bookreader.title";
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int FILES_LOADER = 0;

    protected ArrayList<BookData> booksList;

    protected BooksAdapter adapter;

    protected BookLoaderTask bookLoaderTask;

    protected DataLoadedListener dataLoadedListener;

    protected interface DataLoadedListener {
        public void dataLoaded();
    }

    protected View emptyView;
    private View errorView;

    protected Cursor cursor = null;

    @Bind(R.id.books_recycler_view)
    AutofitRecyclerView recyclerView;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.recycler_view_container)
    FrameLayout recyclerViewContainer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(BOOK_LIST, booksList);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.book_grid_fragment, container, false);

        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null)
            booksList = savedInstanceState.getParcelableArrayList(BOOK_LIST);
        else
            booksList = new ArrayList<>();

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            errorView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, recyclerView, false);
            ((Button) errorView.findViewById(R.id.grant_permission)).setOnClickListener(new View.OnClickListener() {
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

        return rootView;
    }

    protected void openBook(String path) {
        Intent intent;
        if (path.endsWith(".pdf")) {
            Uri uri = Uri.parse(path);
            intent = new Intent(getActivity(), PdfViewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            startActivity(intent);
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
        String sortOrder = Files.FileColumns.DATA + " ASC";
        String selection = Files.FileColumns.DATA + " LIKE ?";
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
        if (bookLoaderTask != null)
            bookLoaderTask.cancel(true);
        ButterKnife.unbind(this);
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
            while (data.moveToNext()) {
                if (isCancelled()) {
                    break;
                }
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

    protected void setDataLoadedListener(DataLoadedListener listener) {
        dataLoadedListener = listener;
    }
}
