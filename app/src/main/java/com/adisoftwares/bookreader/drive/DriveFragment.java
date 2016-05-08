package com.adisoftwares.bookreader.drive;

import android.Manifest;
import android.app.Fragment;

/**
 * Created by adityathanekar on 30/04/16.
 */

import com.adisoftwares.bookreader.BookReaderApplication;
import com.adisoftwares.bookreader.NavigationViewActivity;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.SimpleDividerItemDecoration;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.drive.DriveScopes;

import com.google.api.services.drive.model.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 02/05/16.
 */
//this is the google drive fragment. It displays the pdf files available in google drive.
public class DriveFragment extends Fragment implements DriveAdapter.DriveItemSelected{
    GoogleAccountCredential mCredential;
    //    private TextView mOutputText;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_READ_ACCOUNTS = 1003;
    private static final String[] SCOPES = { DriveScopes.DRIVE_READONLY };
    private ArrayList<File> fileList;

    @BindView(R.id.drive_recycler_view)
    RecyclerView driveRecyclerView;
    @BindView(R.id.drive_container)
    FrameLayout driveContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private View emptyView;

    private DriveAdapter adapter;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drive, container, false);

        fileList = new ArrayList<>();

        unbinder = ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.drive);

        adapter = new DriveAdapter(getActivity(), fileList);
        driveRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        driveRecyclerView.setAdapter(adapter);
        adapter.setDriveItemSelectedListener(this);
        driveRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // Initialize credentials and service object.
        SharedPreferences settings = BookReaderApplication.getContext().getSharedPreferences(getString(R.string.preference_book_info), Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                BookReaderApplication.getContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(getString(R.string.pref_account_name), null));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED) {
            emptyView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, null);
            ((TextView)emptyView.findViewById(R.id.error_text)).setText(R.string.account_permission_off);
            Button errorButton = (Button)emptyView.findViewById(R.id.error_button);
            errorButton.setText(R.string.grant_permission);
            errorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentCompat.requestPermissions(DriveFragment.this,
                            new String[]{Manifest.permission.GET_ACCOUNTS},
                            REQUEST_READ_ACCOUNTS);
                }
            });
            driveContainer.removeAllViews();
            driveContainer.addView(emptyView);
        }
        else if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null);
            ((TextView)emptyView.findViewById(R.id.empty_text)).setText(R.string.play_services_required);
            ((TextView)emptyView.findViewById(R.id.empty_text)).setTextColor(getResources().getColor(android.R.color.black));
            driveContainer.addView(emptyView);
        }

        return rootView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_ACCOUNTS:
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
                    refreshResults();
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != Activity.RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                BookReaderApplication.getContext().getSharedPreferences(getString(R.string.preference_book_info), Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(getString(R.string.pref_account_name), accountName);
                        editor.apply();
                    }
                    refreshResults();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    emptyView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, null);
                    ((TextView)emptyView.findViewById(R.id.error_text)).setText(R.string.account_unspecified);
                    Button errorButton = (Button)emptyView.findViewById(R.id.error_button);
                    errorButton.setText(R.string.try_again);
                    errorButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            refreshResults();
                        }
                    });
                    driveContainer.addView(emptyView);
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != Activity.RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Drive API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                new MakeRequestTask(mCredential).execute();
            } else {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.book_error_view, null);
                ((TextView)emptyView.findViewById(R.id.error_text)).setText(R.string.no_network_connection);
                Button errorButton = (Button)emptyView.findViewById(R.id.error_button);
                errorButton.setText(R.string.try_again);
                errorButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        refreshResults();
                    }
                });
                driveContainer.addView(emptyView);
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                getActivity(),
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Drive API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    private class MakeRequestTask extends AsyncTask<Void, Void, Void> {
        private com.google.api.services.drive.Drive mService = null;
        private Exception mLastError = null;

        public MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name))
                    .build();
        }

        /**
         * Background task to call Drive API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected Void doInBackground(Void... params) {
            try {
                getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
            return null;
        }

        /**
         * Fetch a list of up to 10 file names and IDs.
         * @return List of Strings describing files, or an empty list if no files
         *         found.
         * @throws IOException
         */
        private void getDataFromApi() throws IOException {
            // Get a list of up to 10 files.
            FileList result = mService.files().list()
                    .setQ(getString(R.string.drive_query))
                    .setSpaces(getString(R.string.drive_spaces))
                    .setFields(getString(R.string.drive_fields))
                    .execute();
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    fileList.add(file);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            driveContainer.removeAllViews();
            emptyView = getActivity().getLayoutInflater().inflate(R.layout.progress_bar, null);
            driveContainer.addView(emptyView);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            driveContainer.removeAllViews();
            driveContainer.addView(driveRecyclerView);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onCancelled() {
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    driveContainer.removeAllViews();
                    emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null);
                    TextView emptyTextView = (TextView)emptyView.findViewById(R.id.empty_text);
                    emptyTextView.setTextColor(getResources().getColor(android.R.color.black));
                    emptyTextView.setText(R.string.some_error);
                    driveContainer.addView(emptyView);
                }
            } else {
                emptyView = getActivity().getLayoutInflater().inflate(R.layout.empty_view, null);
                TextView emptyTextView = (TextView)emptyView.findViewById(R.id.empty_text);
                emptyTextView.setTextColor(getResources().getColor(android.R.color.black));
                emptyTextView.setText(R.string.request_cancelled);
                driveContainer.addView(emptyView);
//                mOutputText.setText("Request cancelled.");
            }
        }
    }

    @Override
    public void onDriveItemSelected(int position) {
        Intent intent = new Intent(getActivity(), DownloadService.class);
        intent.putExtra(getString(R.string.google_drive_file_name), fileList.get(position).getName());
        intent.putExtra(getString(R.string.google_drive_file_id), fileList.get(position).getId());
        getActivity().startService(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}

