package com.adisoftwares.bookreader;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

/**
 * Created by adityathanekar on 28/02/16.
 */
public class LoadBookImage extends BitmapLoader {

    private static LoadBookImage bookImageLoader;

    private LoadBookImage() {
        //Find out maximum memory available to application
        //1024 is used because LruCache constructor takes int in kilobytes
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/4th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;
        //Log.d("Aditya", "max memory " + maxMemory + " cache size " + cacheSize);

        // LruCache takes key-value pair in constructor
        // key is the string to refer bitmap
        // value is the stored bitmap
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static BitmapLoader get() {
        if(bookImageLoader == null)
            bookImageLoader = new LoadBookImage();
        return bookImageLoader;
    }

    @Override
    public Bitmap loadBitmap(int width, int height, BookData bookData, int position) {
        // params comes from the execute() call: params[0] is the url.
        Bitmap thumbnail = mLruCache.get(bookData.getPath());
        if(thumbnail == null) {
            thumbnail = bookData.getThumbnail(width, height);
            if(thumbnail != null)
                addBitmapToMemoryCache(bookData.getPath(), thumbnail);
        }
        return thumbnail;
    }
}
