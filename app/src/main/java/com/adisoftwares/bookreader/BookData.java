package com.adisoftwares.bookreader;

import android.graphics.Bitmap;

/**
 * Created by adityathanekar on 06/02/16.
 */
public abstract class BookData {

    private long id;

    private String path;

    private String bookName;

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

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
