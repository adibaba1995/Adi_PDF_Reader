package com.adisoftwares.bookreader.pdf.reader.books.widget;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.adisoftwares.bookreader.pdf.reader.books.BookData;
import com.adisoftwares.bookreader.pdf.reader.books.BookReaderApplication;
import com.adisoftwares.bookreader.pdf.reader.books.R;
import com.adisoftwares.bookreader.pdf.reader.books.cache.LoadBookImage;
import com.adisoftwares.bookreader.pdf.reader.books.Utility;
import com.adisoftwares.bookreader.pdf.reader.books.database.BookContract;
import com.adisoftwares.bookreader.pdf.reader.books.pdf.PDFBookData;

import java.util.ArrayList;

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

//        Intent intent = new Intent(mContext, PdfViewActivity.class);
//        intent.setAction(Intent.ACTION_VIEW);
//        Uri uri = Uri.parse(data.getPath());
//        intent.setData(uri);
//        PendingIntent configPendingIntent = PendingIntent.getActivity(mContext, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mView.setOnClickPendingIntent(R.id.widgetCollectionGrid, configPendingIntent);


        final Intent fillInIntent = new Intent();
        fillInIntent.setAction(Intent.ACTION_VIEW);
        fillInIntent.setData(Uri.parse(data.getPath()));
        mView.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

//
//        mView.setOnClickFillInIntent(R.id.widgetCollectionGrid, intent);

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
        BookReaderApplication.getContext().grantUriPermission("com.adisoftwares.bookreader.pdf.reader.books.widget", BookContract.RecentsEntry.CONTENT_URI, Intent.FLAG_GRANT_READ_URI_PERMISSION);
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