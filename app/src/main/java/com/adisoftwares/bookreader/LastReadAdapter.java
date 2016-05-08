package com.adisoftwares.bookreader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.adisoftwares.bookreader.cache.LoadBookImage;
import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.pdf.PDFBookData;

import butterknife.BindView;
import butterknife.ButterKnife;

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

