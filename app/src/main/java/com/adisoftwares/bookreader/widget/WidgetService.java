package com.adisoftwares.bookreader.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by adityathanekar on 05/05/16.
 */
public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        WidgetDataProvider dataProvider = new WidgetDataProvider(
                getApplicationContext(), intent);
        return dataProvider;
    }

}
