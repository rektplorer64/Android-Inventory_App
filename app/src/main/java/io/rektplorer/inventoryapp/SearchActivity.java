package io.rektplorer.inventoryapp;


import android.animation.Animator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kennyc.view.MultiStateView;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import io.rektplorer.inventoryapp.fragments.ItemListFragment;
import io.rektplorer.inventoryapp.fragments.SearchPreferenceFragment;
import io.rektplorer.inventoryapp.fragments.SortPreferenceFragment;
import io.rektplorer.inventoryapp.fragments.dialogfragment.SearchOptionDialogFragment;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.rvadapters.item.ItemAdapter;
import io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference;
import io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference;
import io.rektplorer.inventoryapp.utility.HelperUtility;

import static io.rektplorer.inventoryapp.ConstantsHolder.FRAGMENT_ITEM_LIST;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.SEARCH_ALL_ITEMS;

public class SearchActivity extends AppCompatActivity
        implements SearchPreferenceFragment.SearchPreferenceUpdateListener, SortPreferenceFragment.SortPreferenceUpdateListener{

    private View searchActivityLayoutParent;

    private FloatingActionButton searchDialogFab;

    private SearchView searchView;

    private Toolbar searchActivityToolbar;
    private CardView containerCardView;


    private Filter.FilterListener filterListener;

    private ItemListFragment itemListFragment;
    private ItemAdapter itemAdapter;

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
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }

            @Override
            public void onAnimationCancel(Animator animation){

            }

            @Override
            public void onAnimationRepeat(Animator animation){

            }
        }, searchActivityLayoutParent);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void animateCircularRevealForActivity(boolean isEnteringAnim, Animator.AnimatorListener animationListener, View rootView){
        int x = rootView.getRight();
        int y = rootView.getTop();
        int startRadius;
        int endRadius;
        if(isEnteringAnim){
            startRadius = 0;
            endRadius = (int) Math.hypot(rootView.getWidth(), rootView.getHeight());
        }else{
            startRadius = (int) Math.hypot(rootView.getWidth(), rootView.getHeight());
            endRadius = 0;
        }
        Animator anim = null;
        if(rootView.isAttachedToWindow()){
            anim = ViewAnimationUtils.createCircularReveal(rootView, x, y, startRadius, endRadius);
        }

        if(anim != null && !isEnteringAnim){
            anim.addListener(animationListener);
        }

        rootView.setVisibility(View.VISIBLE);
        if(anim != null){
            anim.start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initiateViews(){
        Window window = getWindow();
        // Gets the Window in order to change Status Bar's Color
        // // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryBlue_dark));

        if(findViewById(R.id.searchActivityLayoutParent_small) != null){
            searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent_small);
        }else{
            searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent);
        }

        searchActivityToolbar = findViewById(R.id.searchActivityToolbar);
        setSupportActionBar(searchActivityToolbar);

        // searchActivityToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryGreen));

        searchView = findViewById(R.id.searchView);
        searchView.setOnLogoClickListener(new Search.OnLogoClickListener(){
            @Override
            public void onLogoClick(){
                searchView.clearFocus();
                searchView.close();
                onBackPressed();
            }
        });

        searchView.clearFocus();
        searchView.close();
        searchView.setAdapter(null);
        searchView.clearAnimation();
        searchView.setShadow(false);

        filterListener = new Filter.FilterListener(){
            @Override
            public void onFilterComplete(int count){
                getSupportActionBar().setTitle(new StringBuilder().append("Total ").append(count).append((count <= 1) ? " item" : " items").toString());
                if(count == 0){
                    itemListFragment.getRvMultiViewState().setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }else{
                    itemListFragment.getRvMultiViewState().setViewState(MultiStateView.VIEW_STATE_CONTENT);
                }
            }
        };


        // itemAdapter = new ItemAdapter(this, ListLayoutPreference.loadFromSharedPreference(this));

        // itemAdapter.setItemLoadFinishListener(new ItemAdapter.ItemLoadFinishListener(){
        //     @Override
        //     public void onItemFinishUpdate(int size){
        //         // Toast.makeText(SearchActivity.this, "Finished update: " + size, Toast.LENGTH_SHORT).show();
        //         if(size == 0){
        //             itemListMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
        //         }else{
        //             itemListMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
        //         }
        //     }
        // });

        containerCardView = findViewById(R.id.containerCardView);
        /* Adjusts Layout According to the screen size */
        searchActivityLayoutParent.post(new Runnable(){
            @Override
            public void run(){
                adjustUiScales(searchActivityLayoutParent);
                animateCircularRevealForActivity(true, null, searchActivityLayoutParent);
            }
        });

    }

    private void adjustUiScales(View rootView){
        if(rootView.getWidth() <= 1676
                && HelperUtility.getScreenSizeCategory(getApplicationContext()) >= HelperUtility.SCREENSIZE_LARGE
                && HelperUtility.getScreenOrientation(getApplicationContext()) == HelperUtility.SCREENORIENTATION_LANDSCAPE){      // If the app takes the entire screen (too wide in landscape)
            // Sets the horizontal padding
            if(containerCardView != null){
                int margin = HelperUtility.dpToPx(0, rootView.getContext());
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) containerCardView.getLayoutParams();
                layoutParams.setMargins(margin, 0, margin, 0);
                containerCardView.requestLayout();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(ConstantsHolder.BUNDLE_PREFERENCE_FILTER, itemAdapter.getSearchPreference());
        outState.putParcelable(ConstantsHolder.BUNDLE_PREFERENCE_SORTING, itemAdapter.getSortPreference());

        getSupportFragmentManager().putFragment(outState, ConstantsHolder.FRAGMENT_ITEM_LIST, itemListFragment);
        // getSupportFragmentManager().putFragment(outState, "searchPreferenceFragment", searchPreferenceFragment);
        // getSupportFragmentManager().putFragment(outState, "sortPreferenceFragment", sortPreferenceFragment);
    }

    private void initiateFragments(Bundle savedInstanceState){
        searchDialogFab = findViewById(R.id.searchDialogFab);
        if(searchDialogFab != null){
            final SearchPreferenceFragment.SearchPreferenceUpdateListener s1 = this;
            final SortPreferenceFragment.SortPreferenceUpdateListener s2 = this;
            searchDialogFab.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    SearchOptionDialogFragment optionDialog = SearchOptionDialogFragment.newInstance(FilterPreference.loadFromSharedPreference(getApplicationContext()));
                    optionDialog.setSearchPreferenceUpdateListener(s1);
                    optionDialog.setSortPreferenceUpdateListener(s2);
                    optionDialog.show(ft, "searchOptionDialogFragment");
                }
            });
        }else{
            FragmentManager fm = getSupportFragmentManager();

            SearchPreferenceFragment searchPrefFragment = (SearchPreferenceFragment)
                    fm.findFragmentById(R.id.filterSettingFrame);

            SortPreferenceFragment sortPrefFragment = (SortPreferenceFragment)
                    fm.findFragmentById(R.id.sortSettingFrame);

            if(searchPrefFragment != null){
                searchPrefFragment.setSearchPreferenceUpdateListener(this);
            }
            if(sortPrefFragment != null){
                sortPrefFragment.setSortPreferenceUpdateListener(this);
            }
        }

        if(savedInstanceState != null){
            itemListFragment = (ItemListFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_ITEM_LIST);
        }else{
            itemListFragment = ItemListFragment.newInstance();
        }

        itemListFragment.setAdapterInitiationListener(new ItemListFragment.ItemAdapterInitiationListener(){
            @Override
            public void onInitialized(ItemAdapter itemAdapter){
                SearchActivity.this.itemAdapter = itemAdapter;
                itemAdapter.setSearchPreference(FilterPreference.loadFromSharedPreference(SearchActivity.this));
                itemAdapter.setSortPreference(SortPreference.loadFromSharedPreference(SearchActivity.this));
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.itemListFragmentFrame, itemListFragment).commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initiateFragments(savedInstanceState);
        initiateViews();

        if(savedInstanceState != null){
            itemAdapter = itemListFragment.getItemAdapter();
            itemAdapter.setSearchPreference((FilterPreference) savedInstanceState.getParcelable(ConstantsHolder.BUNDLE_PREFERENCE_FILTER));
            itemAdapter.setSortPreference((SortPreference) savedInstanceState.getParcelable(ConstantsHolder.BUNDLE_PREFERENCE_SORTING));
            if(itemAdapter.getSearchPreference() != null){
                itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
            }
            if(itemAdapter.getSortPreference() != null){
                itemAdapter.applySorting();
            }
        }

        final ItemViewModel itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                if(itemAdapter == null){
                    return;
                }

                getSupportActionBar().setTitle(new StringBuilder().append("Total ").append(items.size()).append((items.size() <= 1) ? " item" : " items").toString());
                itemAdapter.applyItemDataChanges(items, false);

                /* These lines of code below are required in order to preserve searching-state
                 when there are changes in database (Insertion, Editing And Deletion) */
                if(itemAdapter.getSearchPreference().getKeyword() != null
                        || (itemAdapter.getSearchPreference().getKeyword() != null
                        && !itemAdapter.getSearchPreference().getKeyword().isEmpty())){
                    // If the query is not empty (because there was a recently search input)
                    itemAdapter.getFilter().filter(searchView.getQuery().toString(), filterListener);       // Re-trigger search
                }else{
                    itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
                }
                itemAdapter.applySorting();
            }
        });

        itemViewModel.getAllReviews().observe(this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviewList){
                itemAdapter.applyReviewDataChanges(ItemViewModel.convertReviewListToSparseArray(reviewList));
            }
        });

        searchView.setOnQueryTextListener(new Search.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(CharSequence query){
                // searchView.close();
                // if(query == null || query.toString().isEmpty()){
                //     itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
                // }else{
                //     itemAdapter.getFilter().filter(query, filterListener);
                //     itemAdapter.getSearchPreference().setKeyword(query.toString());
                // }
                // itemAdapter.applySorting();
                return false;
            }

            @Override
            public void onQueryTextChange(CharSequence newText){
                itemListFragment.redrawListLayout();

                // Toasty.info(SearchActivity.this, "Removing all views").show();

                // if(itemListFragment.getRecyclerView().getLayoutManager() instanceof LinearLayoutManager){
                //     ((LinearLayoutManager) itemListFragment.getRecyclerView().getLayoutManager()).removeAndRecycleAllViews(;);
                // }

                // itemListFragment.getRecyclerView().getRecycledViewPool().clear();
                // // itemListFragment.getRecyclerView().getLayoutManager().removeAllViews();
                // itemListFragment.getRecyclerView().swapAdapter(itemAdapter, true);
                //
                // itemListFragment.getRecyclerView().setLayoutManager(itemListFragment.getRecyclerView().getLayoutManager());

                if(newText == null || newText.toString().isEmpty()){
                    itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
                }else{
                    itemAdapter.getSearchPreference().setKeyword(newText.toString());
                    itemAdapter.getFilter().filter(newText, filterListener);
                }
                itemAdapter.applySorting();
            }
        });
    }

    @Override
    public void onDateChange(FilterPreference.DateType dateType, Date date){
        // Toast.makeText(SearchActivity.this, "Date Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setDatePreference(dateType, date);
        refreshSearchResult();
    }

    @Override
    public void onDateSwitchChange(FilterPreference.DateType dateType, boolean isCheck){
        // Toast.makeText(SearchActivity.this, "Date Pref Switch Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().getDatePreference(dateType).setPreferenceEnabled(isCheck);
        refreshSearchResult();
    }

    @Override
    public void onSearchByDialogChange(FilterPreference.SearchBy searchBy){
        // Toast.makeText(SearchActivity.this, "Search by Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setSearchBy(searchBy);
        refreshSearchResult();
    }

    @Override
    public void onImageModePrefChange(int imageMode){
        // Toast.makeText(SearchActivity.this, "Contain Image Pref Changed!", Toast.LENGTH_SHORT).show();
        itemAdapter.getSearchPreference().setImageMode(imageMode);
        refreshSearchResult();
    }

    @Override
    public void onQuantitySwitchChange(boolean isChecked){
        itemAdapter.getSearchPreference().getQuantityPreference().setPreferenceEnabled(isChecked);
        refreshSearchResult();
    }

    @Override
    public void onQuantityRangeChange(int min, int max){
        // Toast.makeText(SearchActivity.this, "QTY_PREF: Min = " + min + ", max = " + max, Toast.LENGTH_SHORT).show();
        if(itemAdapter.getSearchPreference().getQuantityPreference().isPreferenceEnabled()){
            itemAdapter.getSearchPreference().getQuantityPreference().setMinRange(min);
            itemAdapter.getSearchPreference().getQuantityPreference().setMaxRange(max);
            refreshSearchResult();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        itemAdapter = itemListFragment.getItemAdapter();
        refreshSearchResult();
    }

    @Override
    public void onFragmentResumed(FilterPreference filterPreference){
        itemAdapter = itemListFragment.getItemAdapter();
        refreshSearchResult();
    }

    @Override
    public void onTagSelectionConfirm(List<String> tagSelections){
        itemAdapter.getSearchPreference().setTagList(tagSelections);
        refreshSearchResult();
    }

    @Override
    public void onSortByPrefChange(int field){
        itemAdapter.getSortPreference().setField(field);
        refreshSearchResult();
    }

    @Override
    public void onTextLengthSwitchChange(boolean isChecked){
        itemAdapter.getSortPreference().setStringLength(isChecked);
        refreshSearchResult();
    }

    @Override
    public void onSortOrderSwitchChange(boolean isChecked){
        itemAdapter.getSortPreference().setInAscendingOrder(isChecked);
        refreshSearchResult();
    }

    private void refreshSearchResult(){
        itemAdapter.getFilter().filter(itemAdapter.getSearchPreference().getKeyword(), filterListener);
        itemAdapter.applySorting();
    }
}
