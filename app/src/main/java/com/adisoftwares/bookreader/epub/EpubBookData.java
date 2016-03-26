package com.adisoftwares.bookreader.epub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.adisoftwares.bookreader.BookData;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Created by adityathanekar on 09/02/16.
 */
public class EpubBookData extends BookData {

    private Context context;
    private String path;

    public EpubBookData(String path, Context context) throws Exception {
        super(path);
        this.context = context;
        this.path = path;
    }

    @Override
    public Bitmap getThumbnail(int width, int height) {
        return getCover();
    }

    private Bitmap getCover() {
        try {
            Book book = new EpubReader().readEpubLazy( path, "UTF-8" );
            Bitmap bitmap = BitmapFactory.decodeByteArray(book.getCoverImage().getData(), 0, book.getCoverImage().getData().length);
            return bitmap;
        }catch (Exception e) {

        }
        return null;
    }
}