package com.adisoftwares.bookreader.widget;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.adisoftwares.bookreader.BookData;
import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.LoadBookImage;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.adisoftwares.bookreader.database.BookContract;
import com.adisoftwares.bookreader.pdf.PDFBookData;
import com.adisoftwares.bookreader.pdf.PdfViewActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adityathanekar on 05/05/16.
 */
public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    ArrayList<BookData> bookList;

    Context mContext = null;

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
        bookList = BookDataSingleton.getInstance();
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews mView = new RemoteViews(mContext.getPackageName(), R.layout.widget_book_item);
        BookData data = bookList.get(position);
        mView.setTextViewText(R.id.display_name, Utility.getFileNameFromUrl(data.getPath()));
        mView.setImageViewBitmap(R.id.thumbnail, LoadBookImage.get().loadBitmap(150, 200, data, position));

        PendingIntent pendingIntent;
        Intent intent = new Intent();
        intent.setClass(mContext,PdfViewActivity.class);
        pendingIntent =  PendingIntent.getActivity(mContext, 0, intent, 0);
        mView.setOnClickPendingIntent(R.id.thumbnail, pendingIntent);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BookDataSingleton.getInstance().get(position).getPath()), BookReaderApplication.getContext(), PdfViewActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(BookReaderApplication.getContext(), 0, intent, 0);
//        mView.setOnClickPendingIntent(R.id.widget_item, pendingIntent);


//        LoadBookImage.get().loadBitmap(data.getPath(), holder.thumbnail, holder.thumbnail.getWidth(), holder.thumbnail.getHeight(), 0, bookData);
        return mView;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
//        initData();
    }

    private void initData() {
        BookReaderApplication.getContext().grantUriPermission("com.adisoftwares.bookreader.widget", BookContract.RecentsEntry.CONTENT_URI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        bookList.clear();
        ContentResolver resolver = BookReaderApplication.getContext().getContentResolver();
        Cursor cursor = resolver.query(BookContract.RecentsEntry.CONTENT_URI, null, null, null, null);
        if (cursor.isBeforeFirst()) {
            while (cursor.moveToNext()) {
                try {
                    PDFBookData data = new PDFBookData(cursor.getString(cursor.getColumnIndex(BookContract.RecentsEntry.COLUMN_PATH)));
                    bookList.add(data);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {

    }

}