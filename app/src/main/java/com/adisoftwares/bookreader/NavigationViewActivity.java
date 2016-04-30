package com.adisoftwares.bookreader;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.adisoftwares.bookreader.file_chooser.DirectoryFragment;
import com.squareup.otto.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by adityathanekar on 03/02/16.
 */
public class NavigationViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Bind(R.id.navigationView)
    NavigationView navigationView;

    @Bind(R.id.DrawerLayout)
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_view);

        ButterKnife.bind(this);
        initNavigationView();

        FragmentManager fm = getFragmentManager();

        if (savedInstanceState == null) {
            Fragment fragment = new BookGridFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    private void initNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusStation.getBus().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusStation.getBus().unregister(this);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

    @Subscribe
    public void searchViewTextSubmitted(String title) {
        FragmentManager fm = getFragmentManager();
        Fragment fragment = SearchResultFragment.newInstance(title);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = null;
        switch (itemId) {
            case R.id.folder:
                fragment = new DirectoryFragment();
                break;
            case R.id.books:
                fragment = new BookGridFragment();
                break;
            case R.id.wifi:
                fragment = new WifiFragment();
                break;
            case R.id.drive:
                fragment = new DriveFragment();
                break;
        }
        if (fragment != null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.remove(getFragmentManager().findFragmentById(R.id.fragment_container)).add(R.id.fragment_container, fragment).commit();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void enableNavigationDrawer(boolean isEnabled, Toolbar toolbar) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }
}
