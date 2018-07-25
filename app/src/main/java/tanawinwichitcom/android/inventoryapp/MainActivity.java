package tanawinwichitcom.android.inventoryapp;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.fragments.CircularRevealFragment;
import tanawinwichitcom.android.inventoryapp.fragments.ItemListFragment;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DataRepository;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.User;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class MainActivity extends AppCompatActivity{

    // KEY for SHARED_PREFERENCE (ID of the User who currently login)
    public static final String SharedPref_LOGIN_SESSION_DATA = "SharedPref_LOGIN_SESSION_DATA";
    public static final String SharedPrefKey_LOGIN_SESSION_USER_ID = "SharedPrefKey_LOGIN_SESSION_USER_ID";

    private ItemViewModel itemViewModel;
    private Toolbar toolbar;

    private DrawerLayout navDrawerLayout;

    private boolean screenIsLargeOrPortrait;

    private CardView itemListFragmentCard;
    private FrameLayout itemProfileFragmentFrame;
    private ItemListFragment itemListFragment;

    private CircularRevealFragment itemProfileFragment;
    private View.OnClickListener toolbarClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setUpActionBar();
        HelperUtility.expandActionBarToFitStatusBar(toolbar, this);

        // Toasty.info(this, "Your screen size is " + HelperUtility.getScreenSizeCategory(this)).show();

        itemViewModel = new ItemViewModel(getApplication());
        itemListFragment = new ItemListFragment();

        if(itemProfileFragmentFrame != null){
            // itemProfileFragment = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE, DataRepository.ITEM_FIELD_ID), 0, 0);
            // TODO: Re-route the interactions between ItemListFragment and ItemProfileFragment to make them independent from MainActivity
            // ((ItemProfileFragment) itemProfileFragment).setItemChangeListener(changeListener);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            itemProfileFragment = ItemProfileFragment.newInstance(R.layout.fragment_profile_item
                    , itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE, DataRepository.ITEM_FIELD_ID)
                    , 0, 0);
            // itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            //     @Override
            //     public void onChanged(@Nullable List<Item> itemList){
            //         initialize(itemList, itemProfileFragment);
            //     }
            // });
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
            ft.commit();

            ItemListFragment itemListFragment = (ItemListFragment) getSupportFragmentManager().findFragmentById(R.id.itemListFragment);
            itemListFragment.setItemSelectListener(new ItemAdapter.ItemSelectListener(){
                @Override
                public void onSelect(int itemId, int touchCoordinateY, ItemAdapter itemAdapter){
                    selectItemInLargeScreenLayout(itemId, touchCoordinateY, itemAdapter);
                }
            });

        }

        toolbar.setOnClickListener(toolbarClickListener);

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
                    newId = nearestIds[0];
                }else if(nearestIds.length == 2){
                    newId = Integer.min(nearestIds[0], nearestIds[1]);
                }
                selectItemInLargeScreenLayout(newId, touchCoordinateY, itemAdapter);
            }
        });

        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        // ft.setCustomAnimations(R.anim.enter, R.anim.exit);
        ft.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
        ft.commit();
    }

    private void initialize(final List<Item> itemList, CircularRevealFragment itemProfileFragment){

        itemProfileFragment.setOnFragmentTouchedListener(new CircularRevealFragment.OnFragmentTouched(){
            @Override
            public void onFragmentTouched(Fragment fragment, float x, float y){
                if(fragment instanceof ItemProfileFragment){
                    final ItemProfileFragment theFragment = (ItemProfileFragment) fragment;
                    Animator unreveal = theFragment.prepareUnrevealAnimator(x, y);
                    unreveal.addListener(new Animator.AnimatorListener(){
                        @Override
                        public void onAnimationStart(Animator animation){
                        }

                        @Override
                        public void onAnimationEnd(Animator animation){
                            // remove the fragment only when the animation finishes
                            getSupportFragmentManager().beginTransaction().remove((Fragment) theFragment).commit();
                            //to prevent flashing the fragment before removing it, execute pending transactions immediately
                            getSupportFragmentManager().executePendingTransactions();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation){
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation){
                        }
                    });
                    unreveal.start();
                }
            }
        });

        // Note: To fix "commit() already called exception", FragmentTransaction must be instantiate every time it needed to be used in an interface
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

        ft.replace(R.id.itemProfileFragmentFrame, itemProfileFragment).commit();

    }

    private void initializeViews(){
        screenIsLargeOrPortrait = HelperUtility.isScreenLargeOrPortrait(this);
        toolbar = findViewById(R.id.toolbar);
        navDrawerLayout = findViewById(R.id.navDrawerLayout);
        itemListFragmentCard = findViewById(R.id.itemListFragmentCard);
        itemProfileFragmentFrame = findViewById(R.id.itemProfileFragmentFrame);
    }

    private void setUpActionBar(){
        setSupportActionBar(toolbar);
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
        SharedPreferences loginData = getSharedPreferences(SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = loginData.edit();
        prefEditor.putInt(SharedPrefKey_LOGIN_SESSION_USER_ID, user.getId());
        prefEditor.apply();

        int loggedID = getSharedPreferences(MainActivity.SharedPref_LOGIN_SESSION_DATA, Context.MODE_PRIVATE)
                .getInt(SharedPrefKey_LOGIN_SESSION_USER_ID, -1);
        String toastMsg = "Logged in as: ID#" + loggedID;
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
    }

    public void setToolbarClickListener(View.OnClickListener toolbarClickListener){
        this.toolbarClickListener = toolbarClickListener;
    }
}
