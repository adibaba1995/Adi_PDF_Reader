package com.adisoftwares.bookreader.pdf.reader.books;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by adityathanekar on 07/02/16.
 */
public abstract class DrawThumbnail {
    public abstract Bitmap getThumbnail(Context context, int width, int height, String fileName);
}
