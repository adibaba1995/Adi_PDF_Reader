package com.adisoftwares.bookreader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artifex.mupdfdemo.MuPDFCore;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Aditya Thanekar on 6/16/2015.
 */
public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BooksViewHolder> {

    private LayoutInflater inflater;

    private ArrayList<BookData> booksList;

    private OnItemClickListener mItemClickListener;


    Context context;

    public BooksAdapter(Context context, ArrayList<BookData> booksList) {
        this.booksList = booksList;
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return booksList.get(position).getId();
    }

    @Override
    public BooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.book_grid_item, parent, false);
        BooksViewHolder holder = new BooksViewHolder(view);
        return holder;
    }


    @Override
    public void onBindViewHolder(final BooksViewHolder holder, int position) {
        final BookData bookData = booksList.get(position);
        holder.title.setText(Utility.getFileNameFromUrl(bookData.getPath()));

        holder.thumbnail.post(new Runnable() {
            @Override
            public void run() {
                //holder.thumbnail.setImageBitmap(bookData.getThumbnail(holder.thumbnail.getMeasuredWidth(), holder.thumbnail.getMeasuredHeight()));
                //new BitmapLoaderTask(holder.thumbnail, holder.thumbnail.getMeasuredWidth(), holder.thumbnail.getMeasuredHeight(), bookData).execute();
                LoadBookImage.get().loadBitmap(bookData.getPath(), holder.thumbnail, holder.thumbnail.getWidth(), holder.thumbnail.getHeight(), 0, bookData);
            }
        });
    }

    @Override
    public int getItemCount() {
        return booksList.size();
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    class BooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.thumbnail)
        ImageView thumbnail;
        @Bind(R.id.display_name)
        TextView title;

        public BooksViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }

    }



}
