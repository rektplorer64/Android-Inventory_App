package io.rektplorer.inventoryapp;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    NavigationView navigationView;
    DrawerLayout navDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_base_activity);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().findItem(R.id.nav_search).setCheckable(false);
        checkNavigationDrawerIcon(this);

        navDrawerLayout = findViewById(R.id.navDrawerLayout);
    }

    private void checkNavigationDrawerIcon(Activity activity){
        if(activity instanceof CollectionActivity){
            navigationView.setCheckedItem(R.id.nav_collection);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem){
        switch(menuItem.getItemId()){
            case R.id.nav_collection:
                if(!(this instanceof CollectionActivity)){
                    startActivity(new Intent(this, CollectionActivity.class));
                }
                break;
            case R.id.nav_search:
                startActivity(new Intent(this, SearchActivity.class));
                break;
        }
        navDrawerLayout.closeDrawers();
        return true;
    }
}
