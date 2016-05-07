package com.adisoftwares.bookreader.drive;

/**
 * Created by adityathanekar on 02/05/16.
 */
public class DriveData {
    private String filename;
    private String id;
    private String filepath;
    private String iconpath;

    public DriveData(String filename, String id, String filepath, String iconpath) {
        this.filename = filename;
        this.id = id;
        this.filepath = filepath;
        this.iconpath = iconpath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getIconpath() {
        return iconpath;
    }

    public void setIconpath(String iconpath) {
        this.iconpath = iconpath;
    }

}
