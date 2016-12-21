package com.adisoftwares.bookreader.pdf.reader.books;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by adityathanekar on 06/02/16.
 */
public abstract class BookData implements Parcelable {

    private long id;

    private String path;


    public abstract Bitmap getThumbnail(int width, int height);

    public BookData(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    protected BookData(Parcel in) {
        id = in.readLong();
        path = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(path);
    }
}
