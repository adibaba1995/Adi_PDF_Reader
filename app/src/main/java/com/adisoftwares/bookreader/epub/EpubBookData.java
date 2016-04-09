package com.adisoftwares.bookreader.epub;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.adisoftwares.bookreader.BookData;
import com.adisoftwares.bookreader.BookReaderApplication;

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
    private String path;

    public EpubBookData(String path) throws Exception {
        super(path);
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

    protected EpubBookData(Parcel in) {
        super(in);
        path = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EpubBookData> CREATOR = new Parcelable.Creator<EpubBookData>() {
        @Override
        public EpubBookData createFromParcel(Parcel in) {
            return new EpubBookData(in);
        }

        @Override
        public EpubBookData[] newArray(int size) {
            return new EpubBookData[size];
        }
    };
}