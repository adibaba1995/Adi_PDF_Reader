package com.adisoftwares.bookreader.pdf;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;

import com.artifex.mupdfdemo.MuPDFCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by adityathanekar on 06/02/16.
 */
public class MuPDFThumb extends MuPDFCore {
    public MuPDFThumb(Context context, String filename) throws Exception{
        super(context, filename);
    }

    public Bitmap thumbOfFirstPage(int w, int h){
        PointF pageSize = getPageSize(0);
        float mSourceScale = Math.max(w/pageSize.x, h/pageSize.y);
        Point size = new Point((int)(pageSize.x*mSourceScale), (int)(pageSize.y*mSourceScale));
        final Bitmap bp = Bitmap.createBitmap(size.x,size.y, Bitmap.Config.ARGB_8888);

        updatePage(bp,0,size.x, size.y, 0, 0, size.x, size.y,new Cookie());

        return bp;
    }
}