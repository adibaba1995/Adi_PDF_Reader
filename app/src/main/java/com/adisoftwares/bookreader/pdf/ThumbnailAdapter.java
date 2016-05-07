package com.adisoftwares.bookreader.pdf;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.adisoftwares.bookreader.LoadThumbnailsOfPages;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.androidquery.AQuery;
import com.artifex.mupdfdemo.MuPDFCore;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by Aditya Thanekar on 6/16/2015.
 */
public class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailAdapter.ThumbnailViewHolder> {

    private LayoutInflater inflater;

    MuPDFCore mCore;
    MuPDFCore.Cookie cookie;
    private Context mContext;
    LoadThumbnailsOfPages thumbnailLoader;

    public interface OnRecyclerViewItemSelected {
        public void onRecyclerViewItemSelected(int position);
    }

    private OnRecyclerViewItemSelected onRecyclerViewItemSelected;

    private Bitmap mLoadingBitmap;

    public ThumbnailAdapter(MuPDFCore core, Context context, String path) {
        mCore = core;
        cookie = mCore.new Cookie();
        mContext = context;
        thumbnailLoader = new LoadThumbnailsOfPages(mCore, cookie, mContext, path);
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
        mLoadingBitmap = null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.thumbnail, parent, false);
        ThumbnailViewHolder holder = new ThumbnailViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ThumbnailViewHolder holder, int position) {
        AQuery aq = new AQuery(holder.itemView);
        aq.id(R.id.page_no).text("" + (position + 1));
        //holder.thumbnail.setImageBitmap(thumbnailLoader.loadBitmap(position));
        drawPageImageView(aq.id(R.id.thumbnail).getImageView(), position);
        //holder.page_no.setText(position + "");
    }

    @Override
    public int getItemCount() {
        return mCore.countPages();
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView page_no;

        public ThumbnailViewHolder(View itemView) {
            super(itemView);
            thumbnail = (ImageView)itemView.findViewById(R.id.thumbnail);
            page_no = (TextView)itemView.findViewById(R.id.page_no);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onRecyclerViewItemSelected != null)
                        onRecyclerViewItemSelected.onRecyclerViewItemSelected(getAdapterPosition());
                }
            });
        }

    }

    public static boolean cancelPotentialWork(ImageView v, int position) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(v);

        if (bitmapWorkerTask != null) {
            final int bitmapPosition = bitmapWorkerTask.position;
            if (bitmapPosition != position) {
                bitmapWorkerTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    private void drawPageImageView(ImageView v, int position) {
        if (cancelPotentialWork(v, position)) {

            final BitmapWorkerTask task = new BitmapWorkerTask(v, position);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), mLoadingBitmap, task);
            v.setImageDrawable(asyncDrawable);
            task.execute();
        }
    }

    class BitmapWorkerTask extends AsyncTask<Integer, Void, File> {

        private final WeakReference<ImageView> viewHolderReference;
        private int position;
        private ImageView imgview;
        private Bitmap bmp = null;
        private AQuery aq;
        private Drawable d = null;

        public BitmapWorkerTask(ImageView v, int position) {
            viewHolderReference = new WeakReference<ImageView>(v);
            this.position = position;
            imgview = v;
            aq = new AQuery(mContext);
        }

        @Override
        protected File doInBackground(Integer... params) {
//			File fi = getPageThumbnail(position);
            bmp = getPageThumbnailBitmap(position);
            return null;
        }

        @Override
        protected void onPostExecute(File file) {
            if (isCancelled()) {
//				file = null;
                bmp = null;
            }
            if (viewHolderReference != null && /*file*/bmp != null) {
                final ImageView imageview = viewHolderReference.get();
                if (imageview != null) {
                    final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageview);
                    if (this == bitmapWorkerTask && imageview != null) {
//						imageview.setImageBitmap(bmp);

                        Animation fadeAnimation = AnimationUtils.loadAnimation(mContext, R.anim.fadein);
                        aq.id(imageview).image(bmp).animate(fadeAnimation);
//						Picasso.with(mContext).load(file).into(imageview);
                    }
                }
            }
        }

    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public Bitmap getPageThumbnailBitmap(int position) {
        Bitmap bmp = null;
        PointF pageSize = mCore.getPageSize(position);
        int sizeY = (int) Utility.convertDpToPixel(200, mContext);
        if (sizeY == 0)
            sizeY = 190;
        int sizeX = (int) (pageSize.x / pageSize.y * sizeY);
        Point newSize = new Point(sizeX, sizeY);
        bmp = Bitmap.createBitmap(newSize.x, newSize.y, Bitmap.Config.ARGB_4444);
        mCore.drawPage(bmp, position, newSize.x, newSize.y, 0, 0, newSize.x, newSize.y, cookie);
        //mCore.drawThumbnailPage(bmp, position, newSize.x, newSize.y, 0, 0, newSize.x, newSize.y);
        return bmp;
    }

    public void setRecyclerViewCallbacks(OnRecyclerViewItemSelected onRecyclerViewItemSelected) {
        this.onRecyclerViewItemSelected = onRecyclerViewItemSelected;
    }

}
