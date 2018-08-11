package io.rektplorer.inventoryapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import io.rektplorer.inventoryapp.fragments.ItemListFragment;
import io.rektplorer.inventoryapp.fragments.ItemProfileFragment;
import io.rektplorer.inventoryapp.fragments.dialogfragment.CircularRevealFragment;
import io.rektplorer.inventoryapp.roomdatabase.DataRepository;
import io.rektplorer.inventoryapp.roomdatabase.Entities.User;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.rvadapters.item.ItemAdapter;
import io.rektplorer.inventoryapp.utility.HelperUtility;

public class CollectionActivity extends NavigationDrawerActivity{

    private ItemViewModel itemViewModel;
    private Toolbar toolbar;

    private FloatingActionButton fab;

    private boolean screenIsLargeOrPortrait;

    private CardView itemListFragmentCard;
    private FrameLayout itemProfileFragmentFrame;
    private FrameLayout itemListFragmentFrame;

    private CircularRevealFragment itemProfileFragment;
    private ItemListFragment itemListFragment;

    private View.OnClickListener toolbarClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_collection);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //inflate your activity layout here!
        @SuppressLint("InflateParams")
        View contentView = inflater.inflate(R.layout.activity_collection, null, false);
        navDrawerLayout.addView(contentView, 0);
        navigationView.setCheckedItem(R.id.nav_collection);

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        initializeViews();
        setupActionBar();
        HelperUtility.expandActionBarToFitStatusBar(toolbar, this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if(savedInstanceState != null){
            itemListFragment = (ItemListFragment) getSupportFragmentManager().getFragment(savedInstanceState, ConstantsHolder.FRAGMENT_ITEM_LIST);
        }else{
            itemListFragment = ItemListFragment.newInstance();
        }
        ft.replace(itemListFragmentFrame.getId(), itemListFragment);

        if(itemProfileFragmentFrame != null){
            itemProfileFragment = ItemProfileFragment.newInstance(R.layout.fragment_profile_item
                    , itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE, DataRepository.ITEM_FIELD_ID)
                    , 0, 0);

            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);

            itemListFragment.setItemSelectListener(new ItemAdapter.ItemSelectListener(){
                @Override
                public void onSelect(int itemId, int touchCoordinateY, ItemAdapter itemAdapter){
                    selectItemInLargeScreenLayout(itemId, touchCoordinateY, itemAdapter);
                }
            });
        }
        ft.commit();

        // if(!screenIsLargeOrPortrait){
        toolbar.post(new Runnable(){
            @Override
            public void run(){
                // System.out.println("toolbar's width " + toolbar.getWidth());
                if(toolbar.getWidth() <= 502){
                    int matchParent = LinearLayout.LayoutParams.MATCH_PARENT;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(matchParent, matchParent);

                    params.weight = 5;
                    itemListFragmentCard.setLayoutParams(params);

                    LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(matchParent, matchParent);
                    params1.weight = 10 - params.weight;
                    itemProfileFragmentFrame.setLayoutParams(params1);
                }
            }
        });
        // }
    }

    private void selectItemInLargeScreenLayout(int itemId, final int touchCoordinateY, final ItemAdapter itemAdapter){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        final ItemProfileFragment itemProfileFragment
                = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, itemId,
                0, touchCoordinateY);

        // itemAdapter.notifyDataSetChanged();

        itemProfileFragment.setItemChangeListener(new ItemProfileFragment.ItemChangeListener(){
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemNotFound(int itemId){
                int[] nearestIds = itemViewModel.getBothNearestIds(itemId);
                int newId = 0;
                if(nearestIds.length == 1){
                    // newId = nearestIds[0];
                }else if(nearestIds.length == 2){
                    newId = Integer.min(nearestIds[0], nearestIds[1]);
                    selectItemInLargeScreenLayout(newId, touchCoordinateY, itemAdapter);
                }
            }
        });

        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        // ft.setCustomAnimations(R.anim.enter, R.anim.exit);
        ft.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
        ft.commit();
    }

    private void initializeViews(){
        screenIsLargeOrPortrait = HelperUtility.isScreenLargeOrPortrait(this);
        toolbar = findViewById(R.id.toolbar);
        navDrawerLayout = findViewById(R.id.navDrawerLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // if(HelperUtility.getScreenSizeCategory(view.getContext()) >= HelperUtility.SCREENSIZE_LARGE){
                //     ItemEditingDialogFragment dialogFragment = ItemEditingDialogFragment.newInstance(0, false);
                //     dialogFragment.show(getSupportFragmentManager(), "itemEditingDialogFragment");
                // }else{
                    startActivity(new Intent(view.getContext(), ItemEditingContainerActivity.class));
                // }
            }
        });

        itemListFragmentCard = findViewById(R.id.itemListFragmentCard);

        itemListFragmentFrame = findViewById(R.id.itemListFragmentFrame);
        itemProfileFragmentFrame = findViewById(R.id.itemProfileFragmentFrame);
    }

    private void setupActionBar(){
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Collection");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        final boolean screenIsLargeOrPortrait = HelperUtility.isScreenLargeOrPortrait(this);

        switch(menuItem.getItemId()){
            case android.R.id.home:{
                navDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
            case R.id.launchSearchButton:{
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void simulateLoginSession(User user){
        SharedPreferences loginData = getSharedPreferences(ConstantsHolder.SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = loginData.edit();
        prefEditor.putInt(ConstantsHolder.SharedPrefKey_LOGIN_SESSION_USER_ID, user.getId());
        prefEditor.apply();

        int loggedID = getSharedPreferences(ConstantsHolder.SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE)
                .getInt(ConstantsHolder.SharedPrefKey_LOGIN_SESSION_USER_ID, -1);
        String toastMsg = "Logged in as: ID#" + loggedID;
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, ConstantsHolder.FRAGMENT_ITEM_LIST, itemListFragment);
    }

    public void setToolbarClickListener(View.OnClickListener toolbarClickListener){
        this.toolbarClickListener = toolbarClickListener;
    }
}
