package com.adisoftwares.bookreader.pdf.reader.books;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Files;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adisoftwares.bookreader.pdf.reader.books.pdf.PdfViewActivity;
import com.adisoftwares.bookreader.pdf.reader.books.view.AutofitRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 03/02/16.
 */
public class BookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 0;
    private static final int FILES_LOADER = 0;

    protected BooksAdapter adapter;

    protected View emptyView;

    protected Cursor cursor = null, filesCursor;

    private int scrollPosition;

    @BindView(R.id.books_recycler_view)
    AutofitRecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_container)
    FrameLayout recyclerViewContainer;
    @BindView(R.id.read_last)
    FloatingActionButton fab;
    @BindView(R.id.coordinatorLayout)
    CoordinatorLayout coordinatorLayout;

    private Unbinder unbinder;

    private String bookName = null;

    public interface ChangeListener {
        public void submitText(String text);
    }

    private ChangeListener changeListener;

    public BookFragment setSearchViewTextSubmittedListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(FILES_LOADER, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.book_grid_fragment, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);

        Bundle arguments = getArguments();
        if (arguments != null) {
            bookName = arguments.getString(getString(R.string.book_title));
            if (bookName != null) {
                toolbar.setSubtitle(bookName);
                ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        if (arguments == null && bookName == null) {
            bookName = "";
            toolbar.setSubtitle(R.string.documents);
        }

        adapter = new BooksAdapter(getActivity(), null);
        recyclerView.setAdapter(adapter);
//        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
                                           @Override
                                           public void onItemClick(int position) {
                                               filesCursor.moveToPosition(position);
                                               openBook(filesCursor.getString(filesCursor.getColumnIndex(Files.FileColumns.DATA)));
                                           }
                                       }
        );

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null, false);
                ((TextView) emptyView.findViewById(R.id.empty_text)).setText(R.string.need_to_allow_access);
            } else {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, null, false);
                ((Button) emptyView.findViewById(R.id.error_button)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragmentCompat.requestPermissions(BookFragment.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
            }
            addView(emptyView);
        } else {
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBook(getActivity().getSharedPreferences(getString(R.string.preference_book_info), Context.MODE_PRIVATE).getString(getString(R.string.preference_last_read_book_path), ""));
            }
        });

        if (!getActivity().getSharedPreferences(getString(R.string.preference_book_info), Context.MODE_PRIVATE).contains(getString(R.string.preference_last_read_book_path)))
            fab.setVisibility(View.INVISIBLE);

        return rootView;
    }

    protected void addView(View view) {
        recyclerViewContainer.removeAllViews();
        recyclerViewContainer.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    if (changeListener != null)
                        changeListener.submitText(query);
                    return true;
                }
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                return false;
            }
        });
    }

    protected void openBook(String path) {
        Intent intent;
        if (path.endsWith(getString(R.string.pdf_extension))) {
            Uri uri = Uri.parse(path);
            intent = new Intent(getActivity(), PdfViewActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
            Bundle bndlanimation =
                    ActivityOptions.makeCustomAnimation(getActivity(), R.anim.silde_in_left, R.anim.slide_out_right).toBundle();
            startActivity(intent, bndlanimation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    addView(recyclerView);
                    ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(FILES_LOADER, null, this);
                } else {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null, false);
                        ((TextView) emptyView.findViewById(R.id.empty_text)).setText(R.string.need_to_allow_access);
                    } else {
                        emptyView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, null, false);
                        ((Button) emptyView.findViewById(R.id.error_button)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                FragmentCompat.requestPermissions(BookFragment.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_READ_EXTERNAL_STORAGE);
                            }
                        });
                    }
                    addView(emptyView);
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MediaStore.Files.getContentUri(getString(R.string.files_content_uri_external));
        String[] projection = {Files.FileColumns._ID, Files.FileColumns.DATA};
        String sortOrder = Files.FileColumns.TITLE + " ASC";
        String selection = Files.FileColumns.MIME_TYPE + " = ? AND " + Files.FileColumns.DATA + " LIKE ?";

        String[] selectionArgs = new String[]{getString(R.string.pdf_mime_type), "%/%" + bookName + "%"};
        //String[] selectionArgs = new String[]{"%.pdf"};

        CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        filesCursor = data;
        if (data.getCount() == 0) {
            emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null, false);
            ((TextView) emptyView.findViewById(R.id.empty_text)).setText(R.string.no_files_available);
            addView(emptyView);
        } else {
            addView(recyclerView);
            adapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity) getActivity()).getSupportLoaderManager().destroyLoader(FILES_LOADER);
    }

    public static BookFragment setSearchData(String searchData) {
        BookFragment fragment = new BookFragment();
        Bundle arguments = new Bundle();
        arguments.putString(BookReaderApplication.getContext().getString(R.string.book_title), searchData);
        fragment.setArguments(arguments);
        return fragment;
    }

}
