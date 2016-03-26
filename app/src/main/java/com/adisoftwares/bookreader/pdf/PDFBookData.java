package com.adisoftwares.bookreader.pdf;

import android.content.Context;
import android.graphics.Bitmap;

import com.adisoftwares.bookreader.BookData;
import com.artifex.mupdfdemo.MuPDFCore;

/**
 * Created by adityathanekar on 06/02/16.
 */
public class PDFBookData extends BookData {

    private MuPDFThumb thumbnail;

    public PDFBookData(String path, Context context) throws Exception {
        super(path);
        thumbnail = new MuPDFThumb(context, path);
    }

    @Override
    public Bitmap getThumbnail(int width, int height) {
        return thumbnail.thumbOfFirstPage(width, height);
    }

}
