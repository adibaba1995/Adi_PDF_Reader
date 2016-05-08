package com.adisoftwares.bookreader.wifi_sharing;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.adisoftwares.bookreader.BookData;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.adisoftwares.bookreader.pdf.PDFBookData;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.util.ServerRunner;

/**
 * Created by adityathanekar on 29/04/16.
 */
//This class is used to help the android device act as a web server for some time so that it can show the pdf files available in the device.
//I am beginner in web designing. So please forgive me.
public class HelloServer extends NanoHTTPD implements Serializable {

    Context context;

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
        Uri dburi = MediaStore.Files.getContentUri(context.getString(R.string.files_content_uri_external));

        String[] projection = null;

        String sortOrder = null;

        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";
        //String selection = Files.FileColumns.DATA + " LIKE ?";

        String[] selectionArgs = new String[]{context.getString(R.string.pdf_mime_type)};
        //String[] selectionArgs = new String[]{"%.pdf"};
        Cursor data = context.getContentResolver().query(dburi, projection, selection, selectionArgs, sortOrder);

        StringBuilder fileList = new StringBuilder("");

        String html = readRawTextFile(context, R.raw.listdirectory);

        while (data.moveToNext()) {
            try {
                if (data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)).endsWith(context.getString(R.string.pdf_extension))) {
                    String filePath = data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                    fileList.append(context.getString(R.string.nanohttpd_html, filePath, filePath.substring(0, filePath.lastIndexOf(context.getString(R.string.pdf_extension))) + context.getString(R.string.jpg_extension), Utility.getFileNameFromUrl(data.getString(data.getColumnIndex(MediaStore.Files.FileColumns.DATA)))));
                }
            } catch (Exception e) {
            }
        }

        html = html.replace(context.getString(R.string.nanohttpd_replace_text_string), fileList.toString());

        return html;
    }

    //This method is called when the user requests any resource from the server
    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        Uri uri = Uri.parse(session.getUri());
        String path = uri.getPath();
        path = path.substring(path.indexOf("/", 0) + 1);

        if (session.getMethod() == Method.POST) {
            Map<String, String> files = new HashMap<String, String>();

            try { session.parseBody(files); }
            catch (IOException e1) { e1.printStackTrace(); }
            catch (ResponseException e1) { e1.printStackTrace(); }
            File file = new File(files.get(context.getString(R.string.nanohttpd_file_query_parameter)));
            File originalFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() +"/" + session.getParms().get(context.getString(R.string.nanohttpd_file_query_parameter)));
            try {
                Utility.copyFileUsingStream(file, originalFile);
                String mimeTypes[] = {context.getString(R.string.pdf_mime_type)};
                String paths[] = {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() +"/" + session.getParms().get(context.getString(R.string.nanohttpd_file_query_parameter))};
                MediaScannerConnection.scanFile(context, paths, mimeTypes, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(path.endsWith(context.getString(R.string.jpg_extension))) {
            try {
                path = path.substring(0, path.lastIndexOf(context.getString(R.string.jpg_extension))) + context.getString(R.string.pdf_extension);
                BookData bookData = new PDFBookData(path);
                //Convert bitmap to byte array
                Bitmap bitmap = bookData.getThumbnail(150, 200);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(bitmapdata);

//write the bytes in file
                return newChunkedResponse(Response.Status.OK, context.getString(R.string.bitmap_mime_type), inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(path != null && path.length() != 0) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return newChunkedResponse(Response.Status.OK, context.getString(R.string.pdf_mime_type), fis);
        }
        return newFixedLengthResponse(getFileUploadPage());
    }
}