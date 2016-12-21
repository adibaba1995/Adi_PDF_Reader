package com.adisoftwares.bookreader.pdf.reader.books.cache;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.adisoftwares.bookreader.pdf.reader.books.BookData;

import java.lang.ref.WeakReference;

/**
 * Created by adityathanekar on 28/02/16.
 */
public abstract class MemoryBitmapLoader {

    protected LruCache<String, Bitmap> mLruCache;

    class BitmapLoaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        int width, height, position;
        BookData bookData;

        public BitmapLoaderTask(ImageView imageView, int width, int height, int position, BookData bookData) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.bookData = bookData;
            this.position = position;
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params) {
            return loadBitmap(width, height, bookData, position);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapLoaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                if (this == bitmapDownloaderTask && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    public abstract Bitmap loadBitmap(int width, int height, BookData bookData, int position);

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }


    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapLoaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapLoaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapLoaderTask>(bitmapDownloaderTask);
        }

        public BitmapLoaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public void loadBitmap(String filePath, ImageView imageView, int width, int height, int position, BookData data) {
        if (cancelPotentialDownload(filePath, imageView)) {
            BitmapLoaderTask task = new BitmapLoaderTask(imageView, width, height, position, data);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.execute();
        }
    }

    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapLoaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    private static BitmapLoaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }
}
