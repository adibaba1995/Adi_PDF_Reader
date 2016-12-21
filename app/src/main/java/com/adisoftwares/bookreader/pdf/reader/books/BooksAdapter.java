package com.adisoftwares.bookreader.pdf.reader.books;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.reader.books.cache.LoadBookImage;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Aditya Thanekar on 6/16/2015.
 */
public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BooksViewHolder> {

    private LayoutInflater inflater;

    Cursor data;

    protected OnItemClickListener mItemClickListener;

    Context context;

    public BooksAdapter(Context context, Cursor data) {
        this.data = data;
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        data.moveToPosition(position);
        return data.getInt(data.getColumnIndex(MediaStore.Files.FileColumns._ID));
    }

    @Override
    public BooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.book_grid_item, parent, false);
        BooksViewHolder holder = new BooksViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final BooksViewHolder holder, int position) {
        data.moveToPosition(position);
        try {
            final String path = data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            holder.title.setText(Utility.getFileNameFromUrl(path));

            holder.thumbnail.post(new Runnable() {
                @Override
                public void run() {
                    LoadBookImage.get().loadBitmap(path, holder.thumbnail, holder.thumbnail.getWidth(), holder.thumbnail.getHeight(), 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (data == null)
            return 0;
        else if (!data.isClosed())
            return data.getCount();
        else return 0;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public void swapCursor(Cursor data) {
        this.data = data;
        notifyDataSetChanged();
    }

    class BooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.display_name)
        TextView title;

        public BooksViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(getAdapterPosition());
            }
        }

    }

//    @Override
//    public void onViewDetachedFromWindow(BooksViewHolder holder) {
//        super.onViewDetachedFromWindow(holder);
//        data.close();
//    }
}
