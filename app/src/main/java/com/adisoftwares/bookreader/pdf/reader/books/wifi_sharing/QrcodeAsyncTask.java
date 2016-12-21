package com.adisoftwares.bookreader.pdf.reader.books.wifi_sharing;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.adisoftwares.bookreader.pdf.reader.books.BarcodeUtility;
import com.artifex.mupdfdemo.AsyncTask;
import com.google.zxing.BarcodeFormat;

import java.lang.ref.WeakReference;

/**
 * Created by adityathanekar on 15/05/16.
 */
public class QrcodeAsyncTask extends AsyncTask<String, Void, Bitmap> {

    private WeakReference<ImageView> qrcode;
    private int width, height;

    public QrcodeAsyncTask(ImageView qrcode, int height, int width) {
        this.qrcode = new WeakReference<ImageView>(qrcode);
        this.width = width;
        this.height = height;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = BarcodeUtility.createBarCode(params[0], BarcodeFormat.QR_CODE, height, width);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        ImageView imageView = qrcode.get();
        if(imageView != null) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
