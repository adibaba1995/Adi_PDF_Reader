package com.adisoftwares.bookreader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class HelloServer extends NanoHTTPD {

    Context context;

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(HelloServer.class.getName());

    public static void main(String[] args) {
        ServerRunner.run(HelloServer.class);
    }

    public HelloServer(Context context) {
        super(8080);
        this.context = context;
    }

    public static String readRawTextFile(Context ctx, int resId)
    {
        InputStream inputStream = ctx.getResources().openRawResource(resId);

        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while (( line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return text.toString();
    }

    private String getFileUploadPage() {
        Uri dburi = MediaStore.Files.getContentUri("external");

        String[] projection = null;

        String sortOrder = null;

        String selection = MediaStore.Files.FileColumns.DATA + " LIKE ?  OR " + MediaStore.Files.FileColumns.DATA + " LIKE ?";
        //String selection = Files.FileColumns.DATA + " LIKE ?";

        String[] selectionArgs = new String[]{"%.pdf", "%.epub"};
        //String[] selectionArgs = new String[]{"%.pdf"};
        Cursor data = context.getContentResolver().query(dburi, projection, selection, selectionArgs, sortOrder);

        StringBuilder fileList = new StringBuilder("");

        String html = readRawTextFile(context, R.raw.listdirectory);

        while (data.moveToNext()) {
            try {
                //Log.d("Aditya", "<td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)) + "</td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)) + "</td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)) + "</td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.SIZE)) + "</td>");
                if (data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(".pdf")) {
                    fileList.append("<tr><td><a href=\"" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)) +"\">"+ data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)) + "</a></td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.MEDIA_TYPE)) + "</td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)) + "</td><td>" + data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.SIZE)) + "</td></tr>");
                    //bookData = new PDFBookData(data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)), getActivity());
                }
                //else
                //bookData = new EpubBookData(data.getString(data.getColumnIndex(Files.FileColumns.DATA)), getActivity());
//                bookData.setId(data.getLong(data.getColumnIndex(MediaStore.Files.FileColumns._ID)));
//                booksList.add(bookData);
            } catch (Exception e) {
                Log.d("Aditya", e.toString());
            }
        }

        html = html.replace("%FOLDERLIST%", fileList.toString());

        return html;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Uri uri = Uri.parse(session.getUri());
        String path = uri.getPath();
        path = path.substring(path.indexOf("/", 0) + 1);
        if(path != null && path.length() != 0) {
            Log.d("Aditya", path);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return newChunkedResponse(Response.Status.OK, "application/pdf", fis);
        }
//        CursorLoader cursorLoader = new CursorLoader(context, dburi, projection, selection, selectionArgs, sortOrder);
//        String msg = "<html><body><h1>Hello server</h1>\n";
//        Map<String, String> parms = session.getParms();
//        if (parms.get("username") == null) {
//            msg += "<form action='?' method='get'>\n" + "  <p>Your name: <input type='text' name='username'></p>\n" + "</form>\n";
//        } else {
//            msg += "<p>Hello, " + parms.get("username") + "!</p>";
//        }
//
//        msg += "</body></html>\n";




        return newFixedLengthResponse(getFileUploadPage());
    }
}