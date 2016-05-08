package com.adisoftwares.bookreader.pdf;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.GetChars;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.OnItemClickListener;
import com.adisoftwares.bookreader.R;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.OutlineItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 27/02/16.
 */
public class TOCFragment extends Fragment implements ThumbnailAdapter.OnRecyclerViewItemSelected {

    OutlineItem mItems[];

    private OutlineItemSelected outlineItemSelected;

    private OutlineAdapter outlineAdapter = null;

    @BindView(R.id.outline_recycler_view)
    RecyclerView outlineRecyclerView;
    @BindView(R.id.toc_container)
    FrameLayout toc_container;

    Unbinder unbinder;

    private OutlineTask outlineTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.toc_fragment, container, false);

        unbinder = ButterKnife.bind(this, rootView);
        // Restore the position within the list from last viewing
        outlineRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        outlineTask = new OutlineTask();
        outlineTask.execute();

        return rootView;

    }

    public static TOCFragment newInstance(MuPDFCore core) {
        Bundle args = new Bundle();
        args.putSerializable(BookReaderApplication.getContext().getString(R.string.pdf_core), core);
        TOCFragment fragment = new TOCFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onRecyclerViewItemSelected(int position) {

    }

    class OutlineTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View emptyView = getActivity().getLayoutInflater().inflate(R.layout.progress_bar, null);
            toc_container.removeAllViews();
            toc_container.addView(emptyView);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mItems = ((MuPDFCore) getArguments().getSerializable(getString(R.string.pdf_core))).getOutline();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            toc_container.removeAllViews();
            toc_container.addView(outlineRecyclerView);
            if (mItems != null) {
                OutlineAdapter outlineAdapter = new OutlineAdapter(getActivity().getLayoutInflater(), mItems, getActivity());
                outlineAdapter.setOutlineItemSelected(new OutlineAdapter.OutlineItemSelected() {
                    @Override
                    public void onItemClick(View view, int position) {
//                        OutlineActivityData.get().position = outlineListView.getFirstVisiblePosition();
                        if (outlineItemSelected != null)
                            outlineItemSelected.outlineItemSelected(position);
                    }
                });
                outlineRecyclerView.setAdapter(outlineAdapter);
            } else {
                View emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null);
                ((TextView) emptyView.findViewById(R.id.empty_text)).setText(R.string.empty_toc);
                ((TextView) emptyView.findViewById(R.id.empty_text)).setTextColor(getResources().getColor(android.R.color.black));
                toc_container.addView(emptyView);
            }
            //outlineListView.setAdapter(new OutlineAdapter(getActivity().getLayoutInflater(),mItems, getActivity()));
        }
    }

    public void setActivityCallbacks(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (outlineTask != null)
            outlineTask.cancel(true);
    }
}
