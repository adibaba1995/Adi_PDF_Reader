package com.adisoftwares.bookreader.pdf;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.adisoftwares.bookreader.R;
import com.adisoftwares.bookreader.Utility;
import com.artifex.mupdfdemo.MuPDFCore;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 16/01/16.
 */
public class OutlineActivity extends AppCompatActivity implements OutlineItemSelected{

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private Unbinder unbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outline);

        unbinder = ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(Utility.getFileNameFromUrl(getIntent().getStringExtra(getString(R.string.file_path))));

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
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
        MuPDFCore muPDFCore = (MuPDFCore)getIntent().getSerializableExtra(getString(R.string.core_object));
        ThumbnailFragment thumbnailFragment = new ThumbnailFragment();
        thumbnailFragment.setData(muPDFCore, getIntent().getStringExtra(getString(R.string.file_path)), getIntent().getIntExtra(getString(R.string.page_no), 0));
        thumbnailFragment.setActivityCallbacks(this);
        TOCFragment tocFragment = TOCFragment.newInstance(muPDFCore);
        tocFragment.setActivityCallbacks(this);
        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        bookmarkFragment.setActivityCallbacks(this);
        bookmarkFragment.setData(muPDFCore, getIntent().getStringExtra(getString(R.string.file_path)));
        adapter.addFragment(tocFragment, getString(R.string.outline));
        adapter.addFragment(bookmarkFragment, getString(R.string.bookmarks));
        adapter.addFragment(thumbnailFragment, getString(R.string.thumbnails));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void outlineItemSelected(int position) {
        Intent data = new Intent();
        data.putExtra(getString(R.string.page_no_selected), position);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
