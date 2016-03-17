package com.adisoftwares.bookreader.pdf;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.artifex.mupdfdemo.MuPDFCore;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 16/01/16.
 */
public class OutlineActivity extends AppCompatActivity implements OutlineItemSelected{

    public static final String CORE_OBJECT = "com.adisoftwares.bookreader.pdf.core";
    public static final String FILE_PATH = "com.adisoftwares.bookreader.pdf.file_path";
    public static final String PAGE_NO = "com.adisoftwares.bookreader.pdf.page_no";
    public static final String PAGE_NO_SELECTED = "com.adisoftwares.bookreader.pdf.page_no";
    public static final int OUTLINE_ACTIVITY_RESULT_CODE = 0;

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.tabs)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outline);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utility.getFileNameFromUrl(getIntent().getStringExtra(FILE_PATH)));

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setResult(-1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        MuPDFCore muPDFCore = (MuPDFCore)getIntent().getSerializableExtra(CORE_OBJECT);
        ThumbnailFragment thumbnailFragment = new ThumbnailFragment();
        thumbnailFragment.setData(muPDFCore, getIntent().getStringExtra(FILE_PATH), getIntent().getIntExtra(PAGE_NO, 0));
        thumbnailFragment.setActivityCallbacks(this);
        TOCFragment tocFragment = TOCFragment.newInstance(muPDFCore);
        tocFragment.setActivityCallbacks(this);
        adapter.addFragment(tocFragment, "Outline");
        adapter.addFragment(thumbnailFragment, "Thumbnails");
        viewPager.setAdapter(adapter);
    }

    @Override
    public void outlineItemSelected(int position) {
        Intent data = new Intent();
        data.putExtra(PAGE_NO_SELECTED, position);
        setResult(OUTLINE_ACTIVITY_RESULT_CODE, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
