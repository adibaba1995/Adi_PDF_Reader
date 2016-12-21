package com.adisoftwares.bookreader.pdf.reader.books;

import android.content.Context;
import android.database.Cursor;

import com.adisoftwares.bookreader.pdf.reader.books.cache.LoadBookImage;
import com.adisoftwares.bookreader.pdf.reader.books.database.BookContract;

/**
 * Created by Aditya Thanekar on 4/6/2016.
 */
public class LastReadAdapter extends BooksAdapter {


    public LastReadAdapter(Context context, Cursor data) {
        super(context, data);
    }

    @Override
    public void onBindViewHolder(final BooksViewHolder holder, int position) {
        data.moveToPosition(position);
        try {
            final String path = data.getString(data.getColumnIndex(BookContract.RecentsEntry.COLUMN_PATH));
            holder.title.setText(Utility.getFileNameFromUrl(path));

            holder.thumbnail.post(new Runnable() {
                @Override
                public void run() {
                    LoadBookImage.get().loadBitmap(path, holder.thumbnail, holder.thumbnail.getWidth(), holder.thumbnail.getHeight(), 0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

