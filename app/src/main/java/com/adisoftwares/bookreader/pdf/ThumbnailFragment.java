package com.adisoftwares.bookreader.pdf;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.view.AutofitRecyclerView;
import com.adisoftwares.bookreader.R;
import com.artifex.mupdfdemo.MuPDFCore;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 27/02/16.
 */
public class ThumbnailFragment extends Fragment implements ThumbnailAdapter.OnRecyclerViewItemSelected {

    private OutlineItemSelected outlineItemSelected;

    @BindView(R.id.thumbnail_recycler_view)
    AutofitRecyclerView thumbnailRecyclerView;

    private MuPDFCore mCore;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.thumbnail_fragment, container, false);

        unbinder = ButterKnife.bind(this, rootView);

        //thumbnailGridView.setAdapter(new ThumbnailAdapter((MuPDFCore)getArguments().getSerializable(CORE_OBJECT), getActivity(), getArguments().getString(FILE_PATH)));

        mCore = (MuPDFCore)getArguments().getSerializable(getString(R.string.pdf_core));

        ThumbnailAdapter adapter = new ThumbnailAdapter(mCore, getActivity(), getArguments().getString(getString(R.string.pdf_file_path)));
        adapter.setRecyclerViewCallbacks(this);

        thumbnailRecyclerView.setAdapter(adapter);
        thumbnailRecyclerView.scrollToPosition(getArguments().getInt(getString(R.string.pdf_page_no)));
        return rootView;
    }

    public static ThumbnailFragment newInstance(MuPDFCore muPDFCore, String path, int pageNo) {
        ThumbnailFragment fragment = new ThumbnailFragment();
        Bundle args = new Bundle();
        args.putSerializable(BookReaderApplication.getContext().getString(R.string.pdf_core), muPDFCore);
        args.putString(BookReaderApplication.getContext().getString(R.string.pdf_file_path), path);
        args.putSerializable(BookReaderApplication.getContext().getString(R.string.pdf_page_no), pageNo);
        fragment.setArguments(args);
        return fragment;
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
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
}
