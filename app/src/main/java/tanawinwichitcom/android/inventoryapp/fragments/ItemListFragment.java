package tanawinwichitcom.android.inventoryapp.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.kennyc.view.MultiStateView;

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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.ItemEditingContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.fragments.dialogfragment.ItemEditingDialogFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil.ItemEntityDetailsLookup;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil.ItemEntityKeyProvider;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class ItemListFragment extends Fragment{

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private MultiStateView rvMultiViewState;
    private SelectionTracker selectionTracker;

    private FloatingActionButton fab;

    private ItemAdapter.ItemSelectListener itemSelectListener;

    private ActionMode mActionMode;
    private ActionMode.Callback actionModeCallback;

    public ItemListFragment(){
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                if(HelperUtility.getScreenSizeCategory(getContext()) >= HelperUtility.SCREENSIZE_LARGE){
                    ItemEditingDialogFragment dialogFragment = ItemEditingDialogFragment.newInstance(0, false);
                    dialogFragment.show(getFragmentManager(), "itemEditingDialogFragment");
                }else{
                    startActivity(new Intent(getActivity(), ItemEditingContainerActivity.class));
                }
            }
        });

        recyclerView = view.findViewById(R.id.itemsList);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);

        rvMultiViewState = view.findViewById(R.id.rvMultiViewState);
        itemAdapter = new ItemAdapter(ItemAdapter.COMPACT_CARD_LAYOUT, getContext());

        recyclerView.setAdapter(itemAdapter);

        final ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(getActivity(), new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable final List<Item> items){
                if(items != null && !items.isEmpty()){
                    // setupSelectionContextMenu(itemViewModel, items);
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                    itemAdapter.submitList(items);
                }else{
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }
            }
        });

        if(getActivity() instanceof MainActivity){
            // Tap toolbar to scroll to top
            ((MainActivity) getActivity()).setToolbarClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView
                            .getLayoutManager();
                    layoutManager.setSmoothScrollbarEnabled(true);
                    layoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), 0);
                    Toasty.normal(v.getContext(), "Scrolled to top!").show();
                }
            });
        }

        itemViewModel.getAllReviews().observe(getActivity(), new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviews){
                if(reviews != null){
                    itemAdapter.applyReviewDataChanges(ItemViewModel.convertReviewListToSparseArray(reviews));
                }
            }
        });
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
        itemAdapter.setItemSelectListener(itemSelectListener);
    }
}
