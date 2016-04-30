package com.adisoftwares.bookreader;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 29/04/16.
 */
public class WifiFragment extends Fragment{

    private static final String SHARE_BUTTON_STATE = "com.adisoftwares.bookreader.share_button_state";

    private HelloServer mServer;

    @Bind(R.id.ip)
    TextView ipAddressTextView;
    @Bind(R.id.enable_share)
    Button enableShare;

    private boolean shareEnabled = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi, container, false);
        ButterKnife.bind(this, rootView);

        if(savedInstanceState != null) {
            shareEnabled = savedInstanceState.getBoolean(SHARE_BUTTON_STATE);
            if(shareEnabled)
                enableShare.setText(R.string.enable_share);
            else
                enableShare.setText(R.string.disable_share);
        }

        enableShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shareEnabled) {
                    enableShare.setText(R.string.enable_share);
                    shareEnabled = false;

                    if (mServer != null) {
                        mServer.stop();
                        mServer = null;
                    }
                }
                else {
                    enableShare.setText(R.string.disable_share);
                    shareEnabled = true;

                    try {
                        mServer = new HelloServer(getActivity());
                        mServer.start();
                        ipAddressTextView.setText("http://" + Utility.getIPAddress(true) + ":" + mServer.getListeningPort());
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
    }
}
