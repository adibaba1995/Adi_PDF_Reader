package com.adisoftwares.bookreader;

import android.graphics.Bitmap;
import android.os.Environment;

import com.adisoftwares.bookreader.cache.BitmapLoader;
import com.adisoftwares.bookreader.cache.BitmapLruCache;
import com.adisoftwares.bookreader.cache.CacheableBitmapDrawable;

import java.io.File;
//import android.support.v4.util.LruCache;

/**
 * Created by adityathanekar on 28/02/16.
 */
public class LoadBookImage extends BitmapLoader {

    private static LoadBookImage bookImageLoader;

    private LoadBookImage() {
        File cacheLocation;

        // If we have external storage use it for the disk cache. Otherwise we use
        // the cache dir
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLocation = new File(
                    Environment.getExternalStorageDirectory() + "/Android-BitmapCache");
        } else {
            cacheLocation = new File(BookReaderApplication.getContext().getFilesDir() + "/Android-BitmapCache");
        }
        cacheLocation.mkdirs();

        BitmapLruCache.Builder builder = new BitmapLruCache.Builder(BookReaderApplication.getContext());
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheLocation);

        mCache = builder.build();
    }

    public static BitmapLoader get() {
        if(bookImageLoader == null)
            bookImageLoader = new LoadBookImage();
        return bookImageLoader;
    }

    @Override
    public Bitmap loadBitmap(int width, int height, BookData bookData, int position) {
        // params comes from the execute() call: params[0] is the url.
//        Bitmap thumbnail = mLruCache.get(bookData.getPath());
        CacheableBitmapDrawable cacheadThumbnail = mCache.get(bookData.getPath());
        Bitmap thumbnail;
        if(cacheadThumbnail == null) {
            thumbnail = bookData.getThumbnail(width, height);
            if(thumbnail != null)
                addBitmapToMemoryCache(bookData.getPath(), thumbnail);
        }
        else {
            thumbnail = getBitmapFromMemCache(bookData.getPath());
        }
        return thumbnail;
    }
}
