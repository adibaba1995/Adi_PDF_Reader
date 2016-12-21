package com.adisoftwares.bookreader.pdf.reader.books.pdf;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.reader.books.R;
import com.adisoftwares.bookreader.pdf.reader.books.Utility;
import com.artifex.mupdfdemo.OutlineItem;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 11/03/16.
 */
public class OutlineAdapter extends RecyclerView.Adapter<OutlineAdapter.OutlineViewHolder> {

    private OutlineItem mItems[];
    private final LayoutInflater mInflater;
    private Context context;
    public OutlineAdapter(LayoutInflater inflater, OutlineItem items[], Context context) {
        mInflater = inflater;
        mItems    = items;
        this.context = context;
    }

    public interface OutlineItemSelected {
        public void onItemClick(View view, int position);
    }

    private OutlineItemSelected outlineItemSelected;

    public void setOutlineItemSelected(OutlineItemSelected outlineItemSelected) {
        this.outlineItemSelected = outlineItemSelected;
    }

    @Override
    public OutlineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.outline_item, parent, false);
        OutlineViewHolder holder = new OutlineViewHolder(view);
        return holder;
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    @Override
    public void onBindViewHolder(OutlineViewHolder holder, int position) {
        int padding = 0;
        int level = mItems[position].level;
        if (level > 8) level = 8;
        String space = "";
        for (int i=0; i<level;i++)
            padding += (int) Utility.convertDpToPixel(20, context);
        holder.title.setPadding(padding, 0, 0, 0);
        holder.title.setText(space+mItems[position].title);
        holder.page.setText(String.valueOf(mItems[position].page+1));
    }

    class OutlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.page)
        TextView page;

        public OutlineViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(outlineItemSelected != null) {
//                OutlineActivityData.get().position = getAdapterPosition();
                outlineItemSelected.onItemClick(v, mItems[getAdapterPosition()].page);
            }
        }
    }

    public void setItems(OutlineItem[] items) {
        mItems = items;
    }
}
