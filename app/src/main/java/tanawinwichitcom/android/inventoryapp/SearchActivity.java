package tanawinwichitcom.android.inventoryapp;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.fragments.SearchPreferenceFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class SearchActivity extends AppCompatActivity implements SearchPreferenceFragment.SearchPreferenceUpdateListener{


    private CharSequence savedQuery;
    private RelativeLayout searchActivityLayoutParent;
    private SearchView searchView;
    private TextView totalSearchTextView;
    private RecyclerView resultsRecyclerView;
    private CardView containerCardView;

    private Filter.FilterListener filterListener;

    private ItemAdapter itemAdapter;
    private CharSequence queryString;
    private Fragment searchPreferenceFragment;

    @Override
    public void onBackPressed(){
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        animateCircularRevealForActivity(false, new Animator.AnimatorListener(){
            @Override
            public void onAnimationStart(Animator animation){

            }

            @Override
            public void onAnimationEnd(Animator animation){
                searchActivityLayoutParent.setVisibility(View.GONE);
                SearchActivity.super.onBackPressed();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

            @Override
            public void onAnimationCancel(Animator animation){

            }

            @Override
            public void onAnimationRepeat(Animator animation){

            }
        });
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void animateCircularRevealForActivity(boolean isEnteringAnim, Animator.AnimatorListener animationListener){
        int x = searchActivityLayoutParent.getRight();
        int y = searchActivityLayoutParent.getTop();
        int startRadius;
        int endRadius;
        if(isEnteringAnim){
            startRadius = 0;
            endRadius = (int) Math.hypot(searchActivityLayoutParent.getWidth(), searchActivityLayoutParent.getHeight());
        }else{
            startRadius = (int) Math.hypot(searchActivityLayoutParent.getWidth(), searchActivityLayoutParent.getHeight());
            endRadius = 0;
        }
        Animator anim = null;
        if(searchActivityLayoutParent.isAttachedToWindow()){
            anim = ViewAnimationUtils.createCircularReveal(searchActivityLayoutParent, x, y, startRadius, endRadius);
        }

        if(anim != null && !isEnteringAnim){
            anim.addListener(animationListener);
        }

        searchActivityLayoutParent.setVisibility(View.VISIBLE);
        if(anim != null){
            anim.start();
        }
    }

    private void initiateViews(){
        Window window = getWindow();
        // Gets the Window in order to change Status Bar's Color
        // // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent);

        searchView = findViewById(R.id.searchView);
        searchView.setOnLogoClickListener(new Search.OnLogoClickListener(){
            @Override
            public void onLogoClick(){
                searchView.close();
                onBackPressed();
            }
        });

        searchView.clearFocus();
        searchView.close();
        searchView.setAdapter(null);
        searchView.clearAnimation();
        searchView.setShadow(false);

        totalSearchTextView = findViewById(R.id.totalSearchTextView);
        filterListener = new Filter.FilterListener(){
            @Override
            public void onFilterComplete(int count){
                totalSearchTextView.setText(new StringBuilder().append("Total Search Result: ").append(count).toString());
            }
        };

        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);

        itemAdapter = new ItemAdapter(ItemAdapter.FULL_CARD_LAYOUT, this, this);
        resultsRecyclerView.setHasFixedSize(true);
        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        resultsRecyclerView.setAdapter(itemAdapter);

        containerCardView = findViewById(R.id.containerCardView);
        /* Adjusts Layout According to the screen size */
        searchActivityLayoutParent.post(new Runnable(){
            @Override
            public void run(){
                // Toast.makeText(SearchActivity.this, "ItemProfile RootView's Width: " + searchActivityLayoutParent.getWidth(), Toast.LENGTH_LONG).show();
                if(searchActivityLayoutParent.getWidth() <= 1676
                        && HelperUtility.getScreenSizeCategory(getApplicationContext()) >= HelperUtility.SCREENSIZE_LARGE){      // If the app takes the entire screen (too wide in landscape)
                    // Sets the horizontal padding
                    if(containerCardView != null){
                        int margin = HelperUtility.dpToPx(0, searchActivityLayoutParent.getContext());
                        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) containerCardView.getLayoutParams();
                        layoutParams.setMargins(margin, 0, margin, 0);
                        containerCardView.requestLayout();
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "searchPreferenceFragment", searchPreferenceFragment);
    }

    private void initiateFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        searchPreferenceFragment = new SearchPreferenceFragment();
        ((SearchPreferenceFragment) searchPreferenceFragment).setSearchPreferenceUpdateListener(this);
        ft.replace(R.id.searchSettingFrame, searchPreferenceFragment);
        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initiateViews();

        if(savedInstanceState != null){
            //Restore the fragment's instance
            searchPreferenceFragment = getSupportFragmentManager().getFragment(savedInstanceState, "searchPreferenceFragment");
            ((SearchPreferenceFragment) searchPreferenceFragment).setSearchPreferenceUpdateListener(this);
            getSupportFragmentManager().beginTransaction().replace(R.id.searchSettingFrame, searchPreferenceFragment).commit();
        }else{
            initiateFragment();
        }
        searchActivityLayoutParent.post(new Runnable(){
            @Override
            public void run(){
                animateCircularRevealForActivity(true, null);
            }
        });

        ItemViewModel itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                // Toast.makeText(SearchActivity.this, "Database reinitialized", Toast.LENGTH_SHORT).show();
                totalSearchTextView.setText(new StringBuilder().append("Total Search Result: ").append(items.size()).toString());
                itemAdapter.applyItemDataChanges(items, false);

                /* These lines of code below are required in order to preserve searching-state when there are changes in database (Insertion, Editing And Deletion) */
                if(queryString != null){        // If the query is not empty (because there was a recently search input)
                    itemAdapter.getFilter().filter(searchView.getQuery().toString(), filterListener);       // Re-trigger search
                }
            }
        });

        itemViewModel.getAllReviews().observe(this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviewList){
                SparseArray<ArrayList<Review>> reviewSparseArray = ItemViewModel.convertReviewListToSparseArray(reviewList);
                itemAdapter.applyReviewDataChanges(reviewSparseArray);
            }
        });

        searchView.setOnQueryTextListener(new Search.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(CharSequence query){
                savedQuery = query;
                searchView.close();
                queryString = query;
                itemAdapter.getFilter().filter(query, filterListener);
                return false;
            }

            @Override
            public void onQueryTextChange(CharSequence newText){
                queryString = newText;
                itemAdapter.getFilter().filter(newText, filterListener);
            }
        });
    }

    @Override
    public void onDateChange(ItemAdapter.SearchPreference.DateType dateType, Date date){
        // Toast.makeText(SearchActivity.this, "Date Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setDatePreference(dateType, date);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }

    @Override
    public void onDateSwitchChange(ItemAdapter.SearchPreference.DateType dateType, boolean isCheck){
        // Toast.makeText(SearchActivity.this, "Date Pref Switch Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().getDatePreference(dateType).setPreferenceEnabled(isCheck);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }

    @Override
    public void onSearchByDialogChange(ItemAdapter.SearchPreference.SearchBy searchBy){
        // Toast.makeText(SearchActivity.this, "Search by Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setSearchBy(searchBy);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }

    @Override
    public void onContainImageSwitchChange(boolean b){
        // Toast.makeText(SearchActivity.this, "Contain Image Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setContainsImage(b);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }

    @Override
    public void onQuantitySwitchChange(boolean isChecked){
        itemAdapter.getSearchPreference().getQuantityPreference().setPreferenceEnabled(isChecked);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }

    @Override
    public void onQuantityRangeChange(int min, int max){
        // Toast.makeText(SearchActivity.this, "QTY_PREF: Min = " + min + ", max = " + max, Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().getQuantityPreference().setMinRange(min);
        itemAdapter.getSearchPreference().getQuantityPreference().setMaxRange(max);
        itemAdapter.getFilter().filter(savedQuery, filterListener);
    }
}
