package com.adisoftwares.bookreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by adityathanekar on 15/04/16.
 */
public class SearchResultFragment extends BookFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        String title = getArguments().getString(getString(R.string.book_title));
        if (title != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);

        return rootView;
    }

    public static SearchResultFragment newInstance(String title) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(BookReaderApplication.getContext().getString(R.string.book_title), title);
        fragment.setArguments(args);
        return fragment;
    }

}
