package com.adisoftwares.bookreader;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by adityathanekar on 14/04/16.
 */
public class SearchSuggestionsAdapter extends CursorAdapter {

    public SearchSuggestionsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView textView = (TextView)view.findViewById(android.R.id.text1);
        textView.setText(Utility.getFileNameFromUrl(cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))));
    }

}