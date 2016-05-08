package com.adisoftwares.bookreader.wifi_sharing;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.adisoftwares.bookreader.NavigationViewActivity;
import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 29/04/16.
 */
public class WifiFragment extends Fragment{

    private HelloServer mServer;

    @BindView(R.id.ip)
    TextView ipAddressTextView;
    @BindView(R.id.enable_share)
    Button enableShare;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private boolean shareEnabled = false;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wifi, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);
        toolbar.setSubtitle(R.string.wifi_sharing);
        ((NavigationViewActivity)getActivity()).enableNavigationDrawer(true, toolbar);

        if(savedInstanceState != null) {
            shareEnabled = savedInstanceState.getBoolean(getString(R.string.wifi_sharing_share_button_state));
            if(shareEnabled)
                enableShare.setText(R.string.enable_share);
            else
                enableShare.setText(R.string.disable_share);
            mServer = (HelloServer)savedInstanceState.getSerializable(getString(R.string.wifi_sharing_server));
            ipAddressTextView.setText(savedInstanceState.getString(getString(R.string.wifi_sharing_ip)));
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
                    ipAddressTextView.setText("");
                }
                else {
                    enableShare.setText(R.string.disable_share);
                    shareEnabled = true;

                    try {
                        mServer = new HelloServer(getActivity());
                        mServer.start();
                        ipAddressTextView.setText(getString(R.string.wifi_sharing_ip_address_view, Utility.getIPAddress(true), String.valueOf(mServer.getListeningPort())));
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
        outState.putBoolean(getString(R.string.wifi_sharing_share_button_state), shareEnabled);
        if(mServer != null) {
            outState.putSerializable(getString(R.string.wifi_sharing_server), mServer);
        }
        outState.putString(getString(R.string.wifi_sharing_ip), ipAddressTextView.getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        if (mServer != null) {
//            mServer.stop();
//            mServer = null;
//        }
        unbinder.unbind();
    }
}
