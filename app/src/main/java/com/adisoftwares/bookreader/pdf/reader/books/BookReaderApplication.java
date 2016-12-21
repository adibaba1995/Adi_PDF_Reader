package com.adisoftwares.bookreader.pdf.reader.books;

import android.app.Application;
import android.content.Context;

/**
 * Created by adityathanekar on 26/03/16.
 */
public class BookReaderApplication extends Application {

    private static Context context;

    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
