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

    private static final int NOTIFICATION_ID = 0;

    private static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String FILE_ID = "com.adisoftwares.bookreader.file_id";
    public static final String FILE_NAME = "com.adisoftwares.bookreader.file_name";
    final static String GROUP_KEY_DOWNLOADS = "group_key_downloads";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences(Preference.PREFERENCE_NAME, Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        return super.onStartCommand(intent, flags, startId);
    }

    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, mCredential)
                .setApplicationName("Adi Book Reader")
                .build();

        try {
            publishResults("Downloading " + intent.getStringExtra(FILE_NAME), true);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/" + intent.getStringExtra(FILE_NAME)));
            mService.files().get(intent.getStringExtra(FILE_ID)).executeMediaAndDownloadTo(outputStream);
            String paths[] = {Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/" + intent.getStringExtra(FILE_NAME)};
            String mimeTypes[] = {"application/pdf"};
            MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, null);
            outputStream.close();
        } catch (Exception e) {
            Log.d("Aditya", e.toString());
            publishResults("Some error occured", false);
            return;
        }
        publishResults("Download completed", false);
//        publishResults(output.getAbsolutePath(), result);
    }

    private void publishResults(String message, boolean enableProgress) {
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setSmallIcon(R.drawable.book)
                .setGroup(GROUP_KEY_DOWNLOADS)
                .setContentText(message);
        mBuilder.setProgress(0, 0, enableProgress);
        // Issues the notification
        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}