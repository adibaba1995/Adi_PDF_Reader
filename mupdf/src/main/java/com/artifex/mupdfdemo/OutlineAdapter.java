package com.artifex.mupdfdemo;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class OutlineAdapter extends BaseAdapter {
	private final OutlineItem    mItems[];
	private final LayoutInflater mInflater;
	private Context context;
	public OutlineAdapter(LayoutInflater inflater, OutlineItem items[], Context context) {
		mInflater = inflater;
		mItems    = items;
		this.context = context;
	}

	public int getCount() {
		return mItems.length;
	}

	public Object getItem(int arg0) {
		return null;
	}

	public long getItemId(int arg0) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		int padding = 0;
		if (convertView == null) {
			v = mInflater.inflate(R.layout.outline_entry, null);
		} else {
			v = convertView;
		}
		int level = mItems[position].level;
		if (level > 8) level = 8;
		String space = "";
		for (int i=0; i<level;i++)
			padding += (int)convertDpToPixel(20, context);
		v.setPadding(padding, 0, 0, 0);
		((TextView)v.findViewById(R.id.title)).setText(space+mItems[position].title);
		((TextView)v.findViewById(R.id.page)).setText(String.valueOf(mItems[position].page+1));
		return v;
	}

	public static float convertDpToPixel(float dp, Context context){
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

}
