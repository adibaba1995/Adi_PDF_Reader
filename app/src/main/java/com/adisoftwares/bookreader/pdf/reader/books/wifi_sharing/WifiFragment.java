package com.adisoftwares.bookreader.pdf.reader.books.wifi_sharing;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.adisoftwares.bookreader.pdf.reader.books.NavigationViewActivity;
import com.adisoftwares.bookreader.pdf.reader.books.R;
import com.adisoftwares.bookreader.pdf.reader.books.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 29/04/16.
 */
//This is the wifi sharing class.
public class WifiFragment extends Fragment {

    private static final String SHARE_BUTTON_STATE = "com.adisoftwares.bookreader.pdf.reader.books.wifi_sharing.share_button_state";
    private static final String SHARING_SERVER = "com.adisoftwares.bookreader.pdf.reader.books.wifi_sharing.server";
    private static final String IP_ADDRESS = "com.adisoftwares.bookreader.pdf.reader.books.wifi_sharing.ip";
    private static final String QR_CODE_IMAGE = "com.adisoftwares.bookreader.qr_code_image";

    private HelloServer mServer;

    @BindView(R.id.ip)
    TextView ipAddressTextView;
    @BindView(R.id.enable_share)
    Button enableShare;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.qrcode)
    ImageView qrcode;

    private boolean shareEnabled = false;

    private Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.wifi_sharing);
        ((NavigationViewActivity) getActivity()).enableNavigationDrawer(true, toolbar);

        if (savedInstanceState != null) {
            shareEnabled = savedInstanceState.getBoolean(SHARE_BUTTON_STATE);
            if (shareEnabled)
                enableShare.setText(R.string.enable_share);
            else
                enableShare.setText(R.string.disable_share);
            mServer = (HelloServer) savedInstanceState.getSerializable(SHARING_SERVER);
            ipAddressTextView.setText(savedInstanceState.getString(IP_ADDRESS));
            qrcode.setImageBitmap((Bitmap) savedInstanceState.getParcelable(QR_CODE_IMAGE));
        }

        enableShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shareEnabled) {
                    enableShare.setText(R.string.enable_share);
                    shareEnabled = false;

                    if (mServer != null) {
                        mServer.stop();
                        mServer = null;
                    }
                    ipAddressTextView.setText("");
                    qrcode.setImageResource(R.drawable.wifi_direct);
                } else {
                    enableShare.setText(R.string.disable_share);
                    shareEnabled = true;

                    try {
                        mServer = new HelloServer(getActivity());
                        mServer.start();
                        String ipAddress = getString(R.string.wifi_sharing_ip_address_view, Utility.getIPAddress(true), String.valueOf(mServer.getListeningPort()));
                        ipAddressTextView.setText(ipAddress);
                        new QrcodeAsyncTask(qrcode, qrcode.getHeight(), qrcode.getWidth()).execute(ipAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SHARE_BUTTON_STATE, shareEnabled);
        if (mServer != null) {
            outState.putSerializable(SHARING_SERVER, mServer);
        }
        outState.putString(IP_ADDRESS, ipAddressTextView.getText().toString());
        if (shareEnabled && qrcode != null) {
            qrcode.setDrawingCacheEnabled(true);
            qrcode.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            qrcode.layout(0, 0,
                    qrcode.getMeasuredWidth(), qrcode.getMeasuredHeight());
            qrcode.buildDrawingCache(true);
            Bitmap qrCodeBitmap = qrcode.getDrawingCache();
            if (qrCodeBitmap != null) {
                Bitmap bitmap = Bitmap.createBitmap(qrCodeBitmap);
                outState.putParcelable(QR_CODE_IMAGE, bitmap);
            }
            qrcode.setDrawingCacheEnabled(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
