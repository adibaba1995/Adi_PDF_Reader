package com.adisoftwares.bookreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.support.v4.util.LruCache;

import com.artifex.mupdfdemo.MuPDFCore;

/**
 * Created by adityathanekar on 28/02/16.
 */
public class LoadThumbnailsOfPages extends MemoryBitmapLoader {
    private static LoadThumbnailsOfPages pageThumbnailLoader;
    private MuPDFCore mCore;
    private Context mContext;
    private MuPDFCore.Cookie cookie;
    protected String path;

    public LoadThumbnailsOfPages(MuPDFCore core, MuPDFCore.Cookie cookie, Context context, String path) {
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
        this.mContext = context;
        this.mCore = core;
        this.path = path;
        this.cookie = cookie;
    }

    @Override
    public Bitmap loadBitmap(int width, int height, BookData bookData, int position) {
        // params comes from the execute() call: params[0] is the url.
        Bitmap thumbnail = mLruCache.get(path + position);
        if(thumbnail == null) {
            thumbnail = getPageThumbnailBitmap(position);
            if(thumbnail != null)
                addBitmapToMemoryCache(path + position, thumbnail);
        }
        return thumbnail;
    }

    public Bitmap loadBitmap(int position) {
        return this.loadBitmap(0, 0, null, position);
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
}
