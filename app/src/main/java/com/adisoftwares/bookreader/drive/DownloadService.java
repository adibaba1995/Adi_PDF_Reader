package com.adisoftwares.bookreader.drive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.Preference;
import com.adisoftwares.bookreader.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;

/**
 * Created by adityathanekar on 03/05/16.
 */

public class DownloadService extends IntentService {

    private com.google.api.services.drive.Drive mService;
    private GoogleAccountCredential mCredential;
    private Exception mLastError;

    private static final String[] SCOPES = { DriveScopes.DRIVE_READONLY };

    private static int notificationid = 0;

    public DownloadService() {
        super(BookReaderApplication.getContext().getString(R.string.download_service_name));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(getApplicationContext().getString(R.string.pref_account_name), null));
        return super.onStartCommand(intent, flags, startId);
    }

    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {
        notificationid++;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName(getApplicationContext().getString(R.string.app_name))
                .build();

        try {
            publishResults(getApplicationContext().getString(R.string.downloading) + intent.getStringExtra(getApplicationContext().getString(R.string.google_drive_file_name)), true);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + intent.getStringExtra(getApplicationContext().getString(R.string.google_drive_file_name))));
            mService.files().get(intent.getStringExtra(getApplicationContext().getString(R.string.google_drive_file_id))).executeMediaAndDownloadTo(outputStream);
            String paths[] = {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + intent.getStringExtra(getApplicationContext().getString(R.string.google_drive_file_name))};
            String mimeTypes[] = {getApplicationContext().getString(R.string.pdf_mime_type)};
            MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, null);
            outputStream.close();
        } catch (Exception e) {
            publishResults(getString(R.string.download_error), false);
            notificationid--;
            return;
        }
        publishResults(getString(R.string.download_success), false);
        notificationid--;
//        publishResults(output.getAbsolutePath(), result);
    }

    private void publishResults(String message, boolean enableProgress) {
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setSmallIcon(R.drawable.book)
                .setContentText(message);
        mBuilder.setProgress(0, 0, enableProgress);
        // Issues the notification
        mNotifyManager.notify(notificationid, mBuilder.build());
    }
}