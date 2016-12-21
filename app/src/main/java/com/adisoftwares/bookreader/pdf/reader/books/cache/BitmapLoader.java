package com.adisoftwares.bookreader.pdf.reader.books.cache;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.widget.ImageView;

import com.adisoftwares.bookreader.pdf.reader.books.BookData;
import com.adisoftwares.bookreader.pdf.reader.books.BookReaderApplication;
import com.adisoftwares.bookreader.pdf.reader.books.R;
import com.adisoftwares.bookreader.pdf.reader.books.Utility;
import com.adisoftwares.bookreader.pdf.reader.books.pdf.PDFBookData;

import java.lang.ref.WeakReference;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

/**
 * Created by adityathanekar on 28/02/16.
 */
//This Class is used to load bitmap asynchronously
public abstract class BitmapLoader {

    protected BitmapLruCache mCache;

    class BitmapLoaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;
        int width, height, position;
        String filePath;

        public BitmapLoaderTask(ImageView imageView, int width, int height, int position, String filePath) {
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.width = width;
            this.height = height;
            this.position = position;
            this.filePath = filePath;
        }

        @Override
        // Actual download method, run in the task thread
        protected Bitmap doInBackground(String... params) {
            BookData bookData = null;
            try {
                bookData = new PDFBookData(filePath);
                return loadBitmap(width, height, bookData, position);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.BLACK);


            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
            Bitmap errorIcon = Utility.getBitmapFromVector(VectorDrawableCompat.create(BookReaderApplication.getContext().getResources(), R.drawable.error, null));
            canvas.drawBitmap(errorIcon, (bitmap.getWidth() / 2) - (errorIcon.getWidth() / 2), (bitmap.getHeight() / 2) - (errorIcon.getHeight() / 2), paint);
            return bitmap;
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
//            mLruCache.put(key, bitmap);
            mCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
//        return mLruCache.get(key);
        Bitmap bitmap;
        CacheableBitmapDrawable drawable = mCache.get(key);
        if(drawable == null)
            bitmap = null;
        else
            bitmap = mCache.get(key).getBitmap();
        return bitmap;
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

    public void loadBitmap(String filePath, ImageView imageView, int width, int height, int position) {
        if (cancelPotentialDownload(filePath, imageView)) {
            BitmapLoaderTask task = new BitmapLoaderTask(imageView, width, height, position, filePath);
            DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
            imageView.setImageDrawable(downloadedDrawable);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
