package com.adisoftwares.bookreader.pdf;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.GetChars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.adisoftwares.bookreader.OnItemClickListener;
import com.adisoftwares.bookreader.R;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.OutlineItem;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 27/02/16.
 */
public class TOCFragment extends Fragment implements OnItemClickListener {
    public static final String CORE_OBJECT = "com.adisoftwares.bookreader.pdf.core";
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

    OutlineItem mItems[];

    private OutlineItemSelected outlineItemSelected;

    private OutlineAdapter outlineAdapter= null;

    @Bind(R.id.outline_recycler_view)
    RecyclerView outlineRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.toc_fragment, container, false);

        ButterKnife.bind(this, rootView);
        // Restore the position within the list from last viewing
        outlineRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //outlineRecyclerView.lineListView.setSelection(OutlineActivityData.get().position);
        //outlineListView.setDividerHeight(0);
        //outlineListView.setEmptyView(rootView.findViewById(R.id.circularProgress));

        //outlineListView.setOnItemClickListener(this);

        //mCore = (MuPDFCore)getArguments().getSerializable(CORE_OBJECT);

        new OutlineTask().execute();

        return rootView;

    }

    public static TOCFragment newInstance(MuPDFCore core) {
        Bundle args = new Bundle();
        args.putSerializable(CORE_OBJECT, core);
        TOCFragment fragment = new TOCFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onItemClick(View view, int position) {
        //OutlineActivityData.get().position = outlineListView.getFirstVisiblePosition();
        if(outlineItemSelected != null)
            outlineItemSelected.outlineItemSelected(mItems[position].page);
    }

    class OutlineTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            mItems = ((MuPDFCore)getArguments().getSerializable(CORE_OBJECT)).getOutline();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outlineRecyclerView.setAdapter(new OutlineAdapter(getActivity().getLayoutInflater(), mItems, getActivity()));
            //outlineListView.setAdapter(new OutlineAdapter(getActivity().getLayoutInflater(),mItems, getActivity()));
        }
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }
}
