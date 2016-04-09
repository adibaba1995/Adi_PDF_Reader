package com.adisoftwares.bookreader;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.adisoftwares.bookreader.file_chooser.DirectoryFragment;

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

        FragmentManager fm = getSupportFragmentManager();

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
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment;
        switch (itemId) {
            case R.id.folder:
                fragment = new DirectoryFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                break;
            case R.id.books:
                fragment = new BookGridFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
