package com.adisoftwares.bookreader.pdf;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.adisoftwares.bookreader.AutofitRecyclerView;
import com.adisoftwares.bookreader.R;
import com.artifex.mupdfdemo.MuPDFCore;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 27/02/16.
 */
public class ThumbnailFragment extends Fragment implements ThumbnailAdapter.OnRecyclerViewItemSelected {

    public static final String CORE_OBJECT = "com.adisoftwares.bookreader.pdf.core";
    public static final String FILE_PATH = "com.adisoftwares.bookreader.pdf.file_path";
    public static final String PAGE_NO = "com.adisoftwares.bookreader.pdf.page_no";

    private OutlineItemSelected outlineItemSelected;

    @Bind(R.id.thumbnail_recycler_view)
    AutofitRecyclerView thumbnailRecyclerView;

    private MuPDFCore mCore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.thumbnail_fragment, container, false);

        ButterKnife.bind(this, rootView);

        //thumbnailGridView.setAdapter(new ThumbnailAdapter((MuPDFCore)getArguments().getSerializable(CORE_OBJECT), getActivity(), getArguments().getString(FILE_PATH)));

        mCore = (MuPDFCore)getArguments().getSerializable(CORE_OBJECT);

        ThumbnailAdapter adapter = new ThumbnailAdapter(mCore, getActivity(), getArguments().getString(FILE_PATH));
        adapter.setRecyclerViewCallbacks(this);

        thumbnailRecyclerView.setAdapter(adapter);
        thumbnailRecyclerView.scrollToPosition(getArguments().getInt(PAGE_NO));
        return rootView;
    }

    public void setData(MuPDFCore muPDFCore, String path, int pageNo) {
        Bundle args = new Bundle();
        args.putSerializable(CORE_OBJECT, muPDFCore);
        args.putString(FILE_PATH, path);
        args.putSerializable(PAGE_NO, pageNo);
        setArguments(args);
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }

    @Override
    public void onRecyclerViewItemSelected(int position) {
        if(outlineItemSelected != null)
            outlineItemSelected.outlineItemSelected(position);
    }
}
