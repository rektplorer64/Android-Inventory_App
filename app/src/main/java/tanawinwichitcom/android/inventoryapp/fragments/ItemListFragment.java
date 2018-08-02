package tanawinwichitcom.android.inventoryapp.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kennyc.view.MultiStateView;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil.ItemEntityDetailsLookup;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil.ItemEntityKeyProvider;

import static android.widget.Toast.LENGTH_LONG;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.COMPACT_LIST_LAYOUT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.FULL_CARD_LAYOUT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.NORMAL_LIST_LAYOUT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.SMALL_CARD_LAYOUT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.loadFromSharedPreference;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.saveToSharedPreference;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.ListLayoutPreference.setupRecyclerView;

public class ItemListFragment extends Fragment{

    private static final String RECYCLER_LAYOUT_MANAGER_INSTANCE = "RECYCLER_LAYOUT_MANAGER_INSTANCE";

    private TextView totalItemTextView;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private MultiStateView rvMultiViewState;
    private SelectionTracker selectionTracker;
    private ItemAdapter.ItemSelectListener itemSelectListener;


    private ActionMode mActionMode;
    private ActionMode.Callback actionModeCallback;

    private Parcelable savedRecyclerLayoutState;
    private Bundle mSavedInstanceState;

    public ItemListFragment(){
    }

    public static ItemListFragment newInstance(){
        return new ItemListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState){
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mSavedInstanceState = savedInstanceState;

        totalItemTextView = view.findViewById(R.id.totalItemTextView);
        recyclerView = view.findViewById(R.id.itemsList);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);

        rvMultiViewState = view.findViewById(R.id.rvMultiViewState);
        itemAdapter = new ItemAdapter(getContext(), loadFromSharedPreference(view.getContext()));
        itemAdapter.setItemSelectListener(itemSelectListener);

        setupRecyclerView(itemAdapter.getListViewModePreference(), recyclerView, itemAdapter);

        final ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(getActivity(), new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable final List<Item> items){
                if(items != null && !items.isEmpty()){
                    // setupSelectionContextMenu(itemViewModel, items);
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                    totalItemTextView.setText(new StringBuilder().append("TOTAL ").append(items.size()).append(" ITEMS").toString());
                }else{
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }
                itemAdapter.applyItemDataChanges(items, false);
            }
        });

        // After every children views are inflated, proceed the scroll movement
        // NOTE: It is required to use this observer because if the scroll are invoke too early, there won't be any effect
        //       This method works really well with LiveData.
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout(){
                if(savedInstanceState != null){
                    // Toast.makeText(getContext(), "Loading scroll position", Toast.LENGTH_SHORT).show();
                    recyclerView.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable(RECYCLER_LAYOUT_MANAGER_INSTANCE));
                }
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });


        itemViewModel.getAllReviews().observe(getActivity(), new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviews){
                if(reviews != null){
                    itemAdapter.applyReviewDataChanges(ItemViewModel.convertReviewListToSparseArray(reviews));
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(getActivity() instanceof MainActivity){
            // Tap toolbar to scroll to top
            ((MainActivity) getActivity()).setToolbarClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(recyclerView.getLayoutManager() instanceof LinearLayoutManager){
                        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
                                .getLayoutManager();
                        layoutManager.setSmoothScrollbarEnabled(true);
                        layoutManager.scrollToPosition(0);
                    }else{
                        StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
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

    private Integer[] listLayoutMenuItemIds = new Integer[]{R.id.view_option_normal,
                                                            R.id.view_option_small_card,
                                                            R.id.view_option_compact,
                                                            R.id.view_option_full};

    private Integer[] gridLayoutSettingMenuItemIds = new Integer[]{R.id.view_grid_col_1,
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
                setupRecyclerView(itemAdapter.getListViewModePreference(), recyclerView, itemAdapter);
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
                setupRecyclerView(itemAdapter.getListViewModePreference(), recyclerView, itemAdapter);
                saveToSharedPreference(getContext(), itemAdapter.getListViewModePreference());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable(RECYCLER_LAYOUT_MANAGER_INSTANCE, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    // TODO: fix this
    private void setupSelectionContextMenu(final ItemViewModel itemViewModel, final List<Item> items){
        if(selectionTracker == null){
            selectionTracker = new SelectionTracker.Builder<>("ITEM_SELECTION", recyclerView
                    , new ItemEntityKeyProvider(1, items), new ItemEntityDetailsLookup(recyclerView)
                    , StorageStrategy.createLongStorage()).withOnDragInitiatedListener(new OnDragInitiatedListener(){
                @Override
                public boolean onDragInitiated(MotionEvent e){
                    return true;
                }
            }).build();
        }

        selectionTracker.addObserver(new SelectionTracker.SelectionObserver(){
            @Override
            public void onSelectionChanged(){
                super.onSelectionChanged();
                if(selectionTracker.hasSelection()){
                    if(mActionMode == null){
                        mActionMode = getActivity().startActionMode(actionModeCallback);
                    }else{
                        mActionMode.setTitle("Selected " + selectionTracker.getSelection().size() + " items");
                    }
                }
            }
        });

        actionModeCallback = new ActionMode.Callback(){
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu){
                actionMode.getMenuInflater().inflate(R.menu.menu_item_selection, menu);
                actionMode.setTitle("Selected " + selectionTracker.getSelection().size() + " items");
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
                        for(Item item : items){
                            if(selectionTracker.getSelection().contains(item)){
                                selectionTracker.deselect(item);
                            }
                        }
                        // mActionMode.setTitle("No items selected");
                        break;
                    case R.id.action_select_delete:
                        new MaterialDialog.Builder(getContext()).title("Delete " + selectionTracker.getSelection().size() + " items?")
                                .positiveText("yes").positiveColor(Color.RED)
                                .negativeText("no")
                                .onPositive(new MaterialDialog.SingleButtonCallback(){
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which){
                                        MaterialDialog intermediate = new MaterialDialog.Builder(getContext())
                                                .progress(false, selectionTracker.getSelection().size(), true)
                                                .show();
                                        int i = 0;
                                        // TODO: make this async
                                        for(Item item : items){
                                            if(selectionTracker.getSelection().contains(item)){
                                                intermediate.setProgress(i++);
                                                itemViewModel.delete(item);
                                            }
                                        }
                                        intermediate.dismiss();
                                        // selectionCard.setVisibility(View.GONE);
                                        onDestroyActionMode(actionMode);
                                        // TODO: Fix the crash after selecting and delete, and reselect
                                    }
                                }).show();
                }
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode){
                selectionTracker.clearSelection();
                selectionTracker = null;
                actionMode.finish();
            }
        };

        itemAdapter.setSetSelectionTracker(selectionTracker);
    }

    public void setItemSelectListener(ItemAdapter.ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
    }
}
