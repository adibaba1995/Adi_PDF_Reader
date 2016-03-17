package com.adisoftwares.bookreader;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.adisoftwares.bookreader.file_chooser.FileChooserFragment;

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
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_navigation_view);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.hamburger);
        initNavigationView();

        FragmentManager fm = getSupportFragmentManager();

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer);
        drawerLayout.setDrawerListener(drawerToggle);

        if (savedInstanceState == null) {
            Fragment fragment = new BookGridFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }
    }

    private void initNavigationView() {
//        Menu navigationMenu = navigationView.getMenu();
//        navigationMenu.findItem(R.id.folder).setIcon(MrVector.inflate(getResources(), R.drawable.directory));
//        navigationMenu.findItem(R.id.books).setIcon(MrVector.inflate(getResources(), R.drawable.book));

        navigationView.setNavigationItemSelectedListener(this);


        //View headerView = getLayoutInflater().inflate(R.layout.navigation_view_header, null);
        //Picasso.with(this).load(R.drawable.material_wallpaper).into((ImageView)headerView.findViewById(R.id.navigation_header_image));
        //navigationView.addHeaderView(headerView);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.folder:
                FileChooserFragment fragment = new FileChooserFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                return true;
        }
        return false;
    }
}
