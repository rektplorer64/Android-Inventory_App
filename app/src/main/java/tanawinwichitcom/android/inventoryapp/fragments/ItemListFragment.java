package tanawinwichitcom.android.inventoryapp.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;

import java.util.List;

import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.ItemEditingContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class ItemListFragment extends Fragment{

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private MultiStateView rvMultiViewState;

    private FloatingActionButton fab;

    private ItemAdapter.ItemSelectListener itemSelectListener;

    public ItemListFragment(){
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    // TODO: Add Empty State for the list
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

        ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(getActivity(), new Observer<PagedList<Item>>(){
            @Override
            public void onChanged(@Nullable PagedList<Item> items){
                if(items != null && !items.isEmpty()){
                    // Toasty.success(getContext(), "Total Items: " + items.size()).show();
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                    // itemAdapter.applyItemDataChanges(items, false);
                    itemAdapter.submitList(items);
                    // itemAdapter.notifyDataSetChanged();
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

    public void setItemSelectListener(ItemAdapter.ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
        itemAdapter.setItemSelectListener(itemSelectListener);
    }
}
