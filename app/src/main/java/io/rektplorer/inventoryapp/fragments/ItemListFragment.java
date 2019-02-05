package io.rektplorer.inventoryapp.fragments;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.snackbar.Snackbar;
import com.kennyc.view.MultiStateView;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.selection.OnDragInitiatedListener;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import es.dmoral.toasty.Toasty;
import io.rektplorer.inventoryapp.CollectionActivity;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.rvadapters.item.ItemAdapter;
import io.rektplorer.inventoryapp.rvadapters.item.multiselectutil.MyItemDetailsLookup;

import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.COMPACT_LIST_LAYOUT;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.FULL_CARD_LAYOUT;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.NORMAL_LIST_LAYOUT;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.SMALL_CARD_LAYOUT;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.loadFromSharedPreference;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.saveToSharedPreference;
import static io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference.setupRecyclerView;

public class ItemListFragment extends Fragment{

    private static final String RECYCLER_LAYOUT_MANAGER_INSTANCE = "RECYCLER_LAYOUT_MANAGER_INSTANCE";

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private MultiStateView rvMultiViewState;
    private SelectionTracker<Long> selectionTracker;
    private ItemAdapter.ItemSelectListener itemSelectListener;

    private ActionMode mActionMode;
    private ActionMode.Callback actionModeCallback;

    private ItemAdapterInitiationListener adapterInitiationListener;

    public ItemListFragment(){
    }

    public static ItemListFragment newInstance(){
        return new ItemListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_list_item, container, false);
    }


    @Override
    public void onAttach(Context context){
        super.onAttach(context);

    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState){
        setHasOptionsMenu(true);
        setRetainInstance(true);

        recyclerView = view.findViewById(R.id.itemsList);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);

        recyclerView.setItemViewCacheSize(30);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);

        rvMultiViewState = view.findViewById(R.id.rvMultiViewState);
        itemAdapter = new ItemAdapter(loadFromSharedPreference(view.getContext()));
        itemAdapter.setHasStableIds(true);
        itemAdapter.setItemSelectListener(itemSelectListener);

        if(adapterInitiationListener != null){
            adapterInitiationListener.onInitialized(itemAdapter);
        }

        setupRecyclerView(itemAdapter.getListViewModePreference(), recyclerView, itemAdapter);
        setupSelection();

        final ItemViewModel itemViewModel = ViewModelProviders.of(getActivity())
                                                              .get(ItemViewModel.class);

        itemViewModel.getAllItems().observe(getActivity(), new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable final List<Item> items){
                if(items != null && !items.isEmpty()){
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                }else{
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }

                initializeActionCallback(items, itemViewModel);

                if(getActivity() instanceof CollectionActivity && ((CollectionActivity) getActivity())
                        .getSupportActionBar() != null){
                //     String totalItemStr;
                //     if(items != null && items.size() >= 1){
                //         totalItemStr = NumberFormat
                //                 .getInstance(ScreenUtility.getCurrentLocale(getContext()))
                //                 .format(items.size()) + " Item" + ((items.size() == 1) ? "" : "s");
                //     }else{
                //         totalItemStr = "No Item Added";
                //     }
                //     ((CollectionActivity) getActivity()).getSupportActionBar()
                //                                         .setSubtitle(totalItemStr);
                }
                itemAdapter.applyItemDataChanges(items, false);
            }
        });

        itemViewModel.getAllHeroImage().observe(this, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                itemAdapter.applyHeroImageDataChanges(imageList);
            }
        });

        // After every children views are inflated, proceed the scroll movement
        // NOTE: It is required to use this observer because if the scroll are invoke too early, there won't be any effect
        //       This method works really well with LiveData.
        recyclerView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
                        @Override
                        public void onGlobalLayout(){
                            if(savedInstanceState != null){
                                // Toast.makeText(getContext(), "Loading scroll position", Toast.LENGTH_SHORT).show();
                                recyclerView.getLayoutManager().onRestoreInstanceState(
                                        savedInstanceState
                                                .getParcelable(RECYCLER_LAYOUT_MANAGER_INSTANCE));
                            }
                            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });

        itemViewModel.getAllReviews().observe(getActivity(), new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviews){
                if(reviews != null){
                    itemAdapter.applyReviewDataChanges(
                            ItemViewModel.convertReviewListToSparseArray(reviews));
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(getActivity() instanceof CollectionActivity){
            // Tap toolbar to scroll to top
            ((CollectionActivity) getActivity()).setToolbarClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
                                .getLayoutManager();
                        layoutManager.setSmoothScrollbarEnabled(true);
                        layoutManager.scrollToPosition(0);
                    }else{
                        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView
                                .getLayoutManager();
                        layoutManager.scrollToPosition(0);
                    }
                    Toasty.normal(v.getContext(), "Scrolled to top!").show();
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.list_options, menu);

        // Set initial checked list layout option
        int targetLayoutMenuItem = 0;
        switch(itemAdapter.getListViewModePreference().getListLayoutMode()){
            case FULL_CARD_LAYOUT:
                targetLayoutMenuItem = R.id.view_option_full;
                break;
            case NORMAL_LIST_LAYOUT:
                targetLayoutMenuItem = R.id.view_option_normal;
                break;
            case SMALL_CARD_LAYOUT:
                targetLayoutMenuItem = R.id.view_option_small_card;
                break;
            case COMPACT_LIST_LAYOUT:
                targetLayoutMenuItem = R.id.view_option_compact;
                break;
        }
        if(targetLayoutMenuItem != 0){
            menu.findItem(targetLayoutMenuItem).setChecked(true);
        }

        // Set initial checked grid option
        int gridLayoutMenuItem = 0;
        switch(itemAdapter.getListViewModePreference().getTotalColumn()){
            case 1:
                gridLayoutMenuItem = R.id.view_grid_col_1;
                break;
            case 2:
                gridLayoutMenuItem = R.id.view_grid_col_2;
                break;
            case 3:
                gridLayoutMenuItem = R.id.view_grid_col_3;
                break;
            case 4:
                gridLayoutMenuItem = R.id.view_grid_col_4;
                break;
        }
        if(gridLayoutMenuItem != 0 && menu.findItem(gridLayoutMenuItem) != null){
            menu.findItem(gridLayoutMenuItem).setChecked(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private Integer[] listLayoutMenuItemIds = new Integer[]{
            R.id.view_option_normal,
            R.id.view_option_small_card,
            R.id.view_option_compact,
            R.id.view_option_full};

    private Integer[] gridLayoutSettingMenuItemIds = new Integer[]{
            R.id.view_grid_col_1,
            R.id.view_grid_col_2,
            R.id.view_grid_col_3,
            R.id.view_grid_col_4};

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(Arrays.asList(listLayoutMenuItemIds).contains(item.getItemId())){
            if(!item.isChecked()){
                item.setChecked(!item.isChecked());
                int layoutMode = 0;
                switch(item.getItemId()){
                    case R.id.view_option_full:
                        layoutMode = FULL_CARD_LAYOUT;
                        break;
                    case R.id.view_option_normal:
                        layoutMode = NORMAL_LIST_LAYOUT;
                        break;
                    case R.id.view_option_small_card:
                        layoutMode = SMALL_CARD_LAYOUT;
                        break;
                    case R.id.view_option_compact:
                        layoutMode = COMPACT_LIST_LAYOUT;
                        break;
                }
                itemAdapter.getListViewModePreference().setListLayoutMode(layoutMode);
                redrawListLayout();
                saveToSharedPreference(getContext(), itemAdapter.getListViewModePreference());
                return true;
            }
        }
        if(Arrays.asList(gridLayoutSettingMenuItemIds).contains(item.getItemId())){
            if(!item.isChecked()){
                item.setChecked(!item.isChecked());
                int gridColumn = 1;
                boolean gridMode = false;
                switch(item.getItemId()){
                    case R.id.group_view_grid_col:
                        gridColumn = 1;
                        gridMode = false;
                        break;
                    case R.id.view_grid_col_2:
                        gridColumn = 2;
                        gridMode = true;
                        break;
                    case R.id.view_grid_col_3:
                        gridColumn = 3;
                        gridMode = true;
                        break;
                    case R.id.view_grid_col_4:
                        gridColumn = 4;
                        gridMode = true;
                        break;
                }
                itemAdapter.getListViewModePreference().setGridMode(gridMode);
                itemAdapter.getListViewModePreference().setTotalColumn(gridColumn);
                redrawListLayout();
                saveToSharedPreference(getContext(), itemAdapter.getListViewModePreference());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void redrawListLayout(){
        setupRecyclerView(itemAdapter.getListViewModePreference(), recyclerView, itemAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLER_LAYOUT_MANAGER_INSTANCE,
                               recyclerView.getLayoutManager().onSaveInstanceState());
    }

    // TODO: fix this
    private void setupSelection(){
        selectionTracker = new SelectionTracker.Builder<>("ITEM_SELECTION"
                , recyclerView
                , new ItemAdapter.ItemEntityKeyProvider(itemAdapter)
                , new MyItemDetailsLookup(recyclerView)
                , StorageStrategy.createLongStorage())
                .withOnDragInitiatedListener(new OnDragInitiatedListener(){
                    @Override
                    public boolean onDragInitiated(MotionEvent e){
                        return true;
                    }
                }).build();

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver(){
            @Override
            public void onSelectionChanged(){
                super.onSelectionChanged();
                if(selectionTracker.hasSelection()){
                    if(mActionMode == null){
                        if(getActivity() != null){
                            mActionMode = ((AppCompatActivity) getActivity())
                                    .startSupportActionMode(actionModeCallback);
                        }
                    }else{
                        mActionMode.setTitle(
                                "Selected " + selectionTracker.getSelection().size() + " items");
                    }
                }
            }
        });
        itemAdapter.setSetSelectionTracker(selectionTracker);
    }

    private void initializeActionCallback(final List<Item> itemList,
                                          final ItemViewModel itemViewModel){
        actionModeCallback = new androidx.appcompat.view.ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu){
                actionMode.getMenuInflater().inflate(R.menu.menu_item_selection, menu);
                actionMode
                        .setTitle("Selected " + selectionTracker.getSelection().size() + " items");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu){
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem){
                switch(menuItem.getItemId()){
                    case R.id.action_select_clear:
                        selectionTracker.clearSelection();
                        // mActionMode.setTitle("No items selected");
                        actionMode.finish();
                        break;
                    case R.id.action_select_delete:
                        new MaterialDialog.Builder(getContext())
                                .title("Delete " + selectionTracker.getSelection()
                                                                   .size() + " items?")
                                .positiveText(android.R.string.ok).positiveColor(Color.RED)
                                .negativeText(android.R.string.no)
                                .onPositive(new MaterialDialog.SingleButtonCallback(){
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which){
                                        MaterialDialog intermediate = new MaterialDialog.Builder(
                                                getContext())
                                                .progress(true,
                                                          selectionTracker.getSelection().size(),
                                                          true)
                                                .show();
                                        int i = 0;
                                        // TODO: make this async
                                        for(Item item : itemList){
                                            if(selectionTracker.getSelection()
                                                               .contains((long) item.getId())){
                                                intermediate.setProgress(i++);
                                                itemViewModel.delete(item);
                                            }
                                        }
                                        intermediate.dismiss();
                                        // selectionCard.setVisibility(View.GONE);
                                        actionMode.finish();
                                        // TODO: Fix the crash after selecting and delete, and reselect
                                    }
                                }).show();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode){
                if(selectionTracker != null){
                    selectionTracker.clearSelection();
                }
                mActionMode = null;
            }
        };
    }

    public void setItemSelectListener(ItemAdapter.ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
    }

    public void setAdapterInitiationListener(
            ItemAdapterInitiationListener adapterInitiationListener){
        this.adapterInitiationListener = adapterInitiationListener;
    }

    public ItemAdapter getItemAdapter(){
        return itemAdapter;
    }

    public MultiStateView getRvMultiViewState(){
        return rvMultiViewState;
    }

    public RecyclerView getRecyclerView(){
        return recyclerView;
    }

    public interface ItemAdapterInitiationListener{
        void onInitialized(ItemAdapter itemAdapter);
    }
}
