package tanawinwichitcom.android.inventoryapp;

import android.animation.Animator;
import android.annotation.TargetApi;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.TextView;

import com.kennyc.view.MultiStateView;
import com.lapism.searchview.Search;
import com.lapism.searchview.widget.SearchView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.fragments.dialogfragment.SearchOptionDialogFragment;
import tanawinwichitcom.android.inventoryapp.fragments.SearchPreferenceFragment;
import tanawinwichitcom.android.inventoryapp.fragments.SortPreferenceFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.FilterPreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.FilterPreference.SEARCH_ALL_ITEMS;

public class SearchActivity extends AppCompatActivity
        implements SearchPreferenceFragment.SearchPreferenceUpdateListener, SortPreferenceFragment.SortPreferenceUpdateListener{

    public static final String TAG_FILTER_PREF_FRAGMENT = "filterPrefFragment";
    public static final String TAG_SORT_PREF_FRAGMENT = "sortPrefFragment";

    private View searchActivityLayoutParent;

    private FloatingActionButton searchDialogButton;

    private SearchView searchView;
    private TextView totalSearchTextView;
    private RecyclerView resultsRecyclerView;
    private CardView containerCardView;

    private MultiStateView itemListMultiStateView;

    private Filter.FilterListener filterListener;

    private ItemAdapter itemAdapter;

    public static final String BUNDLE_PREFERENCE_FILTER = "preference_filter";
    public static final String BUNDLE_PREFERENCE_SORTING = "preference_sorting";

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

    @Override
    protected void onResume(){
        super.onResume();
        itemAdapter.setSortPreference(SortPreference.loadFromSharedPreference(getApplication()));
        itemAdapter.getFilter().filter(itemAdapter.getSearchPreference().getKeyword(), filterListener);
        itemAdapter.applySorting();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initiateViews(){
        Window window = getWindow();
        // Gets the Window in order to change Status Bar's Color
        // // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        if(findViewById(R.id.searchActivityLayoutParent_small) != null){
            searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent_small);
        }else{
            searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent);
        }

        searchDialogButton = findViewById(R.id.searchDialogButton);

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
        resultsRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        itemAdapter = new ItemAdapter(this, ListLayoutPreference.loadFromSharedPreference(this));

        itemListMultiStateView = findViewById(R.id.itemListMultistateView);
        itemAdapter.setItemLoadFinishListener(new ItemAdapter.ItemLoadFinishListener(){
            @Override
            public void onItemFinishUpdate(int size){
                // Toast.makeText(SearchActivity.this, "Finished update: " + size, Toast.LENGTH_SHORT).show();
                if(size == 0){
                    itemListMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }else{
                    itemListMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                }
            }
        });

        resultsRecyclerView.setHasFixedSize(true);
        resultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        resultsRecyclerView.setAdapter(itemAdapter);

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
        outState.putParcelable(BUNDLE_PREFERENCE_FILTER, itemAdapter.getSearchPreference());
        outState.putParcelable(BUNDLE_PREFERENCE_SORTING, itemAdapter.getSortPreference());
        // getSupportFragmentManager().putFragment(outState, "searchPreferenceFragment", searchPreferenceFragment);
        // getSupportFragmentManager().putFragment(outState, "sortPreferenceFragment", sortPreferenceFragment);
    }

    private void initiateFragments(){
        if(findViewById(R.id.searchActivityLayoutParent_small) != null){
            final SearchPreferenceFragment.SearchPreferenceUpdateListener s1 = this;
            final SortPreferenceFragment.SortPreferenceUpdateListener s2 = this;
            searchDialogButton.setOnClickListener(new View.OnClickListener(){
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

            searchPrefFragment.setSearchPreferenceUpdateListener(this);
            sortPrefFragment.setSortPreferenceUpdateListener(this);

        }
        // try{
        //     ((SearchPreferenceFragment) searchPreferenceFragment).setSearchPreferenceUpdateListener(this);
        //     ((SortPreferenceFragment) sortPreferenceFragment).setSortPreferenceUpdateListener(this);
        // }catch(NullPointerException e){
        //     e.printStackTrace();
        // }
        // ft.commitAllowingStateLoss();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initiateViews();
        initiateFragments();

        if(savedInstanceState != null){
            itemAdapter.setSearchPreference((FilterPreference) savedInstanceState.getParcelable(BUNDLE_PREFERENCE_FILTER));
            itemAdapter.setSortPreference((SortPreference) savedInstanceState.getParcelable(BUNDLE_PREFERENCE_SORTING));

            if(itemAdapter.getSearchPreference() != null){
                itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
            }

            if(itemAdapter.getSortPreference() != null){
                itemAdapter.applySorting();
            }
        }

        totalSearchTextView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                resultsRecyclerView.scrollToPosition(0);
                Toasty.info(v.getContext(), "Scrolled to top!").show();
            }
        });

        ItemViewModel itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                // Toast.makeText(SearchActivity.this, "Database reinitialized", Toast.LENGTH_SHORT).show();
                totalSearchTextView.setText(new StringBuilder().append("Total Search Result: ")
                        .append(items.size()).toString());
                itemAdapter.applyItemDataChanges(items, false);
                itemAdapter.submitList(items);

                /* These lines of code below are required in order to preserve searching-state when there are changes in database (Insertion, Editing And Deletion) */
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
                SparseArray<ArrayList<Review>> reviewSparseArray = ItemViewModel
                        .convertReviewListToSparseArray(reviewList);
                itemAdapter.applyReviewDataChanges(reviewSparseArray);
            }
        });

        searchView.setOnQueryTextListener(new Search.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(CharSequence query){
                searchView.close();
                if(query == null || query.toString().isEmpty()){
                    itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
                }else{
                    itemAdapter.getFilter().filter(query, filterListener);
                    itemAdapter.getSearchPreference().setKeyword(query.toString());
                }
                itemAdapter.applySorting();
                return false;
            }

            @Override
            public void onQueryTextChange(CharSequence newText){
                if(newText == null || newText.toString().isEmpty()){
                    itemAdapter.getFilter().filter(SEARCH_ALL_ITEMS, filterListener);
                }else{
                    itemAdapter.getFilter().filter(newText, filterListener);
                    itemAdapter.getSearchPreference().setKeyword(newText.toString());
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
    public void onFragmentResume(FilterPreference filterPreference){
        // Toasty.info(getApplicationContext(), "onResuming...").show();
        itemAdapter.setSearchPreference(filterPreference);
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

        resultsRecyclerView.scrollToPosition(0);
    }
}
