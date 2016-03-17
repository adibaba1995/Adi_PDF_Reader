/*
 * WebViewActivity.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-10.
 */
//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification,
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice,
//  this list of conditions and the following disclaimer in the documentation and/or
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be
//  used to endorse or promote products derived from this software without specific
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
//  OF THE POSSIBILITY OF SUCH DAMAGE

package com.adisoftwares.bookreader.epub;

import android.content.Intent;

import android.os.Bundle;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.adisoftwares.bookreader.R;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.launcher.ContainerHolder;

public class EpubActivity extends AppCompatActivity {

    public static final String EXTRA_FILE_PATH = "com.adisoftwares.bookreader.epub.FILE_PATH";

    private String filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_container);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_VIEW.equals(action) && type != null) {
            if ("application/epub+zip".equals(type)) {
                filePath = intent.getData().getPath();
                Log.d("Aditya", filePath);
            }
        } else {
            filePath = intent.getStringExtra(EXTRA_FILE_PATH);
        }

        Container container = EPub3.openBook(filePath);
        EPub3.setSdkErrorHandler(null);

        ContainerHolder.getInstance().put(container.getNativePtr(), container);

        FragmentManager fm = getSupportFragmentManager();

        if(savedInstanceState == null) {
            Fragment fragment = EpubFragment.newInstance(filePath, container.getNativePtr());
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

    }

}
