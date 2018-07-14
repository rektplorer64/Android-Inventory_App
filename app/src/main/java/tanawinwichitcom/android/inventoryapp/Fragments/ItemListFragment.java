package tanawinwichitcom.android.inventoryapp.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kennyc.view.MultiStateView;

import java.util.Comparator;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.AddItemActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RecyclerViewAdapters.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

public class ItemListFragment extends Fragment{

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private MultiStateView rvMultiViewState;

    private FloatingActionButton fab;

    private static final Comparator<Item> ALPHABETICAL_COMPARATOR = new Comparator<Item>(){
        @Override
        public int compare(Item o1, Item o2){
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

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
                startActivity(new Intent(getActivity(), AddItemActivity.class));
            }
        });

        recyclerView = view.findViewById(R.id.itemsList);
        rvMultiViewState = view.findViewById(R.id.rvMultiViewState);

        itemAdapter = new ItemAdapter(ItemAdapter.SMALL_CARD_LAYOUT, ALPHABETICAL_COMPARATOR, getContext());


        ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(getActivity(), new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                if(items != null && !items.isEmpty()){
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(itemAdapter);
                    itemAdapter.applyItemDataChanges(items);
                }else{
                    rvMultiViewState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }
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
}
