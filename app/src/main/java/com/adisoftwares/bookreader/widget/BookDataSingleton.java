package com.adisoftwares.bookreader.widget;

import com.adisoftwares.bookreader.BookData;

import java.util.ArrayList;

/**
 * Created by adityathanekar on 06/05/16.
 */
public class BookDataSingleton {
    private static ArrayList<BookData> bookList;

    public static ArrayList<BookData> getInstance() {
        if(bookList == null)
            bookList = new ArrayList<>();
        return bookList;
    }

    private BookDataSingleton() {
    }
}
