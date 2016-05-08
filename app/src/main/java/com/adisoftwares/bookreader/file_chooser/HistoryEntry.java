package com.adisoftwares.bookreader.file_chooser;

import java.io.File;
import java.io.Serializable;

/**
 * Created by adityathanekar on 08/05/16.
 */
public class HistoryEntry implements Serializable {
    int scrollItem, scrollOffset;
    File dir;
    String title;
}