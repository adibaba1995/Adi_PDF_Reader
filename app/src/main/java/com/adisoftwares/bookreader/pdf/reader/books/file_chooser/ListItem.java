package com.adisoftwares.bookreader.pdf.reader.books.file_chooser;

import java.io.File;
import java.io.Serializable;

/**
 * Created by adityathanekar on 08/05/16.
 */
public class ListItem implements Serializable {
    int icon;
    String title;
    String subtitle = "";
    String ext = "";
    String thumb;
    File file;
}