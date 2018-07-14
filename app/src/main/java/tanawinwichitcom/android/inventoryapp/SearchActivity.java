package tanawinwichitcom.android.inventoryapp;

import android.animation.Animator;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lapism.searchview.Search;
import com.lapism.searchview.database.SearchHistoryTable;
import com.lapism.searchview.widget.SearchAdapter;
import com.lapism.searchview.widget.SearchItem;

import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.RecyclerViewAdapters.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

public class SearchActivity extends AppCompatActivity{

    private RecyclerView resultsRecyclerView;
    private CardView filterCardView;
    private LinearLayout searchActivityLayoutParent;
    private ItemAdapter itemAdapter;
    private com.lapism.searchview.widget.SearchView searchView;

    private ItemViewModel itemViewModel;

    private Context context;

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
        Animator anim = ViewAnimationUtils.createCircularReveal(searchActivityLayoutParent, x, y, startRadius, endRadius);

        if(!isEnteringAnim){
            anim.addListener(animationListener);
        }

        searchActivityLayoutParent.setVisibility(View.VISIBLE);
        anim.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchActivityLayoutParent = findViewById(R.id.searchActivityLayoutParent);
        // searchActivityLayoutParent.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
        //     @Override
        //     public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom){
        //         v.removeOnLayoutChangeListener(this);
        //         animateCircularRevealForActivity(true, null);
        //     }
        // });

        searchActivityLayoutParent.post(new Runnable(){
            @Override
            public void run(){
                animateCircularRevealForActivity(true, null);
            }
        });

        resultsRecyclerView = findViewById(R.id.resultsRecyclerView);
        filterCardView = findViewById(R.id.filterCardView);
        searchView = findViewById(R.id.searchView);

        final SearchAdapter searchAdapter = new SearchAdapter(this);
        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(this);

        context = this;

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.itemAdapter = new ItemAdapter(ItemAdapter.NORMAL_CARD_LAYOUT, null, this);
        resultsRecyclerView.setAdapter(itemAdapter);
        resultsRecyclerView.setHasFixedSize(true);
        resultsRecyclerView.setVisibility(View.GONE);

        setUpSearchPrefFragment();

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        searchView.setAdapter(searchAdapter);
        searchView.setClearIcon(R.drawable.ic_close_black_24dp);
        searchView.setOnOpenCloseListener(new Search.OnOpenCloseListener(){
            @Override
            public void onOpen(){
                Toast.makeText(context, "Opening search", Toast.LENGTH_SHORT).show();
                searchAdapter.setSuggestionsList(mHistoryDatabase.getAllItems());
            }

            @Override
            public void onClose(){
                //finish();
            }
        });

        searchView.setOnMenuClickListener(new Search.OnMenuClickListener(){
            @Override
            public void onMenuClick(){
                Toast.makeText(context, "Add Filter Dialog", Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnLogoClickListener(new Search.OnLogoClickListener(){
            @Override
            public void onLogoClick(){
                onBackPressed();
            }
        });

        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable final List<Item> itemList){
                searchView.setOnQueryTextListener(new Search.OnQueryTextListener(){
                    @Override
                    public boolean onQueryTextSubmit(final CharSequence query){
                        SearchItem searchItem = new SearchItem(context);
                        searchItem.setTitle(query.toString());

                        // If pressing enter and the query is not contained in the database before, stores it to the history database.
                        if(!mHistoryDatabase.getAllItems().contains(searchItem)){
                            mHistoryDatabase.addItem(searchItem);
                        }

                        // If the result list is not empty
                        if(!searchAdapter.getResultsList().isEmpty()){
                            // If the first item of the list contains itemId (which means it was created in populateItem())
                            // and it contains the query in its title string
                            if(searchAdapter.getResultsList().get(0) instanceof SearchItemWrapper
                                    && searchAdapter.getResultsList().get(0).getTitle().toString().contains(query.toString())){
                                // Launches the ItemProfileActivity
                                enterProfileActivity((SearchItemWrapper) searchAdapter.getResultsList().get(0));
                            }else{
                                int count = 0;
                                for(SearchItem searchItem1 : searchAdapter.getResultsList()){
                                    if(searchItem1 instanceof SearchItemWrapper){
                                        count++;
                                        enterProfileActivity((SearchItemWrapper) searchItem1);
                                        break;
                                    }
                                }
                                if(count == 0){
                                    Toast.makeText(context, "No result! lol", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }else{
                            Toast.makeText(context, "No result!", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }

                    @Override
                    public void onQueryTextChange(CharSequence newText){
                        if(!searchAdapter.getSuggestionsList().isEmpty()){
                            searchAdapter.getSuggestionsList().clear();
                        }
                        searchAdapter.setSuggestionsList(populateItems(itemList));
                    }
                });

                searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener(){
                    @Override
                    public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle){
                        // If the result list is not empty
                        // if(!searchAdapter.getResultsList().isEmpty()){
                        //     SearchItemWrapper searchItemWrapper = null;
                        //     for(SearchItem searchItem : searchAdapter.getResultsList()){
                        //         // System.out.println(searchItem.getTitle());
                        //         if(searchItem instanceof SearchItemWrapper
                        //                 && searchItem.getTitle().toString().equals(title.toString())){
                        //             searchItemWrapper = (SearchItemWrapper) searchItem;
                        //         }
                        //     }
                        //
                        //     if(searchItemWrapper != null){
                        //         enterProfileActivity(searchItemWrapper);
                        //     }else{
                        //         searchView.setText(title.toString());
                        //     }
                        // }

                        SearchItem searchItem = new SearchItem(context);
                        searchItem.setTitle(title.toString());

                        // If pressing an item and the item's title is not contained in the database before, stores it to the history database.
                        if(!mHistoryDatabase.getAllItems().contains(searchItem)){
                            mHistoryDatabase.addItem(searchItem);
                        }

                        showResultsInRecyclerView(itemList, title);
                        searchView.close();
                        searchView.setText(title.toString());
                    }
                });
            }
        });

        // SearchAdapter searchAdapter = new SearchAdapter(this);
        // searchAdapter.setSuggestionsList(suggestions);
        // searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener(){
        //     @Override
        //     public void onSearchItemClick(int position, CharSequence title, CharSequence subtitle){
        //         SearchItem item = new SearchItem(getApplicationContext());
        //         item.setTitle(title);
        //         item.setSubtitle(subtitle);
        //
        //         mHistoryDatabase.addItem(item);
        //     }
        // });
        //
        // searchView.setOnQueryTextListener(new Search.OnQueryTextListener(){
        //     @Override
        //     public boolean onQueryTextSubmit(CharSequence query){
        //         SearchItem item = new SearchItem(getApplicationContext());
        //         item.setTitle(query);
        //
        //         mHistoryDatabase.addItem(item);
        //         return true;
        //     }
        //
        //     @Override
        //     public void onQueryTextChange(CharSequence newText){
        //         System.out.println("printShit");
        //     }
        //
        // });
    }

    private void showResultsInRecyclerView(List<Item> itemList, CharSequence query){
        resultsRecyclerView.setVisibility(View.VISIBLE);
        List<Item> filteredList = new ArrayList<>();
        for(Item item : itemList){
            if(item.getName().toLowerCase().contains(query.toString().toLowerCase().trim())
                    || item.getName().equalsIgnoreCase(query.toString().trim())){
                filteredList.add(item);
            }
        }
        itemAdapter.applyItemDataChanges(filteredList);
    }

    private List<SearchItem> populateItems(List<Item> itemList){
        List<SearchItem> suggestions = new ArrayList<>();
        for(Item item : itemList){
            SearchItem searchItem = new SearchItemWrapper(getApplicationContext());
            searchItem.setIcon1Resource(R.drawable.ic_edit_black_24dp);
            searchItem.setIcon2Resource(R.drawable.ic_call_made_black_24dp);
            searchItem.setTitle(item.getName());
            //searchItem.setSubtitle(item.getDescription());
            ((SearchItemWrapper) searchItem).setItemId(item.getId());

            if(!suggestions.contains(searchItem)){
                suggestions.add(searchItem);
            }
        }
        return suggestions;
    }

    private void enterProfileActivity(SearchItemWrapper searchItem){
        int itemId = searchItem.getItemId();
        //if(screenIsLargeOrPortrait)
        Intent intent = new Intent(context, ItemProfileContainerActivity.class);
        intent.putExtra("itemId", itemId);
        context.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return super.onOptionsItemSelected(item);
    }

    private void setUpSearchPrefFragment(){
        if(HelperUtilities.getScreenSizeCategory(this) >= HelperUtilities.SCREENSIZE_LARGE){
            ViewGroup.MarginLayoutParams cardViewLayoutParam = (ViewGroup.MarginLayoutParams) filterCardView.getLayoutParams();
            int marginInPx;
            if(HelperUtilities.getScreenOrientation(this) == HelperUtilities.SCREENORIENTATION_LANDSCAPE){
                marginInPx = HelperUtilities.dpToPx(24, this);
            }else{
                marginInPx = HelperUtilities.dpToPx(16, this);
            }
            cardViewLayoutParam.setMargins(marginInPx, 0, marginInPx, 0);
        }

        AdvancedSearchSettingFragment advancedSearchSettingFragment = new AdvancedSearchSettingFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.searchSettingFrame, advancedSearchSettingFragment);
        fragmentTransaction.commit();
    }

    private class SearchItemWrapper extends SearchItem{

        private int itemId;

        public SearchItemWrapper(Context context){
            super(context);
        }

        public int getItemId(){
            return itemId;
        }

        public void setItemId(int itemId){
            this.itemId = itemId;
        }
    }
}
