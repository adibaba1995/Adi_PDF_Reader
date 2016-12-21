package com.adisoftwares.bookreader.pdf.reader.books.pdf;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.adisoftwares.bookreader.pdf.reader.books.BookData;
import com.adisoftwares.bookreader.pdf.reader.books.BookReaderApplication;

/**
 * Created by adityathanekar on 06/02/16.
 */
public class PDFBookData extends BookData {

    private MuPDFThumb thumbnail;

    public PDFBookData(String path) throws Exception {
        super(path);
        thumbnail = new MuPDFThumb(BookReaderApplication.getContext(), path);
    }

    @Override
    public Bitmap getThumbnail(int width, int height) {
        return thumbnail.thumbOfFirstPage(width, height);
    }


    protected PDFBookData(Parcel in) {
        super(in);
        thumbnail = (MuPDFThumb) in.readValue(MuPDFThumb.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(thumbnail);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PDFBookData> CREATOR = new Parcelable.Creator<PDFBookData>() {
        @Override
        public PDFBookData createFromParcel(Parcel in) {
            return new PDFBookData(in);
        }

        @Override
        public PDFBookData[] newArray(int size) {
            return new PDFBookData[size];
        }
    };
}