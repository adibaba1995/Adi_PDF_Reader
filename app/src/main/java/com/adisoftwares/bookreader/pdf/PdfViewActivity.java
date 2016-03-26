package com.adisoftwares.bookreader.pdf;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.adisoftwares.bookreader.R;
import com.artifex.mupdfdemo.MuPDFReaderView;

public class PdfViewActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "com.adisoftwares.bookreader.pdf.FILE_PATH";

    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if ("application/pdf".equals(type)) {
                filePath = intent.getData().getPath();
            }
        } else {
            filePath = intent.getStringExtra(EXTRA_FILE_PATH);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment == null) {
            fragment = PdfViewFragment.newInstance(filePath);
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    }

}

