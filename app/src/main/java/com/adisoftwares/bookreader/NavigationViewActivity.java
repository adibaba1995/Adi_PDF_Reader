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

import com.adisoftwares.bookreader.drive.DriveFragment;
import com.adisoftwares.bookreader.file_chooser.DirectoryFragment;
import com.adisoftwares.bookreader.wifi_sharing.WifiFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by adityathanekar on 03/02/16.
 */
//This is the class for the activity which starts as soon as the app starts.
//It contains the navigation drawer and the fragments are added or removed as needed.
public class NavigationViewActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BookFragment.ChangeListener {

    @BindView(R.id.navigationView)
    NavigationView navigationView;

    @BindView(R.id.DrawerLayout)
    DrawerLayout drawerLayout;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_view);

        unbinder = ButterKnife.bind(this);
        initNavigationView();

        FragmentManager fm = getFragmentManager();

        if (savedInstanceState == null) {
            BookFragment fragment = new BookFragment();
            fragment.setSearchViewTextSubmittedListener(this);
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    private void initNavigationView() {
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
    }

////    This method is called when the user submits the search button text.
//    @Subscribe
//    public void searchViewTextSubmitted(String title) {
//        FragmentManager fm = getFragmentManager();
//        BookFragment fragment = BookFragment.setSearchData(title);
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.remove(getFragmentManager().findFragmentById(R.id.fragment_container)).add(R.id.fragment_container, fragment);
////        fragmentTransaction.replace(R.id.fragment_container, fragment);
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.commit();
//    }

    @Override
    public void submitText(String text) {
        FragmentManager fm = getFragmentManager();
        BookFragment fragment = BookFragment.setSearchData(text).setSearchViewTextSubmittedListener(this);
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.remove(getFragmentManager().findFragmentById(R.id.fragment_container)).add(R.id.fragment_container, fragment);
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
            case R.id.documents:
                fragment = new BookFragment().setSearchViewTextSubmittedListener(this);;
                break;
            case R.id.recents:
                fragment = new RecentsFragment().setSearchViewTextSubmittedListener(this);
                break;
            case R.id.last_added:
                fragment = new LastAddedFragment().setSearchViewTextSubmittedListener(this);
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

    //This method is used to enable the hamburger icon in the fragment's toolbar.
    public void enableNavigationDrawer(boolean isEnabled, Toolbar toolbar) {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
    }
}
