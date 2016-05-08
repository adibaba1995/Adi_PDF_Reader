package com.adisoftwares.bookreader.cache;

import android.graphics.Bitmap;
import android.os.Environment;

import com.adisoftwares.bookreader.BookData;
import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.R;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

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
                    Environment.getExternalStorageDirectory() + BookReaderApplication.getContext().getString(R.string.bitmap_cache_path));
        } else {
            cacheLocation = new File(BookReaderApplication.getContext().getFilesDir() + BookReaderApplication.getContext().getString(R.string.bitmap_cache_path));
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
