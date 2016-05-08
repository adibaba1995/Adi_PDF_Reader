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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Files;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.PdfViewActivity;
import com.adisoftwares.bookreader.view.AutofitRecyclerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

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
    @BindView(R.id.ad_view)
    AdView adView;

    private Unbinder unbinder;

    private String bookName = null;

    private Menu bookMenu;

    public interface SearchViewTextSubmitted {
        public void submitText(String text);
    }

    private SearchViewTextSubmitted searchViewTextSubmitted;

    public BookFragment setSearchViewTextSubmittedListener(SearchViewTextSubmitted searchViewTextSubmitted) {
        this.searchViewTextSubmitted = searchViewTextSubmitted;
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

        Bundle arguments = getArguments();
        if (arguments != null) {
            bookName = arguments.getString(getString(R.string.book_title));
            if (bookName != null)
                toolbar.setSubtitle(bookName);
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
            setSearcViewVisiblity(false);
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

        ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBook(getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(Preference.LAST_READ_BOOK_PATH, ""));
            }
        });

        if (!getActivity().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE).contains(Preference.LAST_READ_BOOK_PATH))
            fab.setVisibility(View.INVISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AdRequest adRequest = new AdRequest.Builder().build();
                if (adView != null) {
                    adView.loadAd(adRequest);
                    adView.setAdListener(new AdListener() {
                        @Override
                        public void onAdLoaded() {
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(coordinatorLayout.getWidth(), coordinatorLayout.getHeight());
                            params.setMargins(0, 0, 0, adView.getHeight());
                            coordinatorLayout.setLayoutParams(params);
                        }
                    });
                }
            }
        }, 5000);

        return rootView;
    }

    private void setSearcViewVisiblity(boolean visiblity) {
        if (bookMenu != null)
            bookMenu.findItem(R.id.search).setVisible(false);
    }

    private void addView(View view) {
        recyclerViewContainer.removeAllViews();
        recyclerViewContainer.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adView != null)
            adView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (adView != null)
            adView.pause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_grid_menu, menu);
        bookMenu = menu;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            setSearcViewVisiblity(false);
        }
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
                if (searchViewTextSubmitted != null)
                    searchViewTextSubmitted.submitText(query);
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
                        Uri uri = Files.getContentUri(getString(R.string.files_content_uri_external));
                        String[] projection = {Files.FileColumns._ID, Files.FileColumns.DATA};
                        String sortOrder = Files.FileColumns.TITLE + " ASC";
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
                    ActivityOptions.makeCustomAnimation(getActivity(), R.anim.silde_in_left, R.anim.slide_out_right).toBundle();
            startActivity(intent, bndlanimation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    setSearcViewVisiblity(true);
                    addView(recyclerView);
                    ((AppCompatActivity) getActivity()).getSupportLoaderManager().initLoader(FILES_LOADER, null, this);
                } else {
                    setSearcViewVisiblity(false);
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
        if (adView != null)
            adView.destroy();
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
