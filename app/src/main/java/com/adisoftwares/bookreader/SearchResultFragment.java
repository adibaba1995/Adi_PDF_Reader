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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        String title = getArguments().getString(TITLE);
        if (title != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);

        return rootView;
    }

    public static SearchResultFragment newInstance(String title) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

}
