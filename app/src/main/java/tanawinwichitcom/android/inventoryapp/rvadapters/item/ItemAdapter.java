package tanawinwichitcom.android.inventoryapp.rvadapters.item;

import android.annotation.SuppressLint;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.SortingAsyncTaskLoader;
import tanawinwichitcom.android.inventoryapp.objectdiffutil.ItemDiffCallback;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.DatePreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference.SEARCH_ALL_ITEMS;

public class ItemAdapter extends PagedListAdapter<Item, ItemViewHolder> implements Filterable{

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_CARD_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;
    public static final int COMPACT_CARD_LAYOUT = 3;

    private List<ItemListWrapper> listElementWrappers;
    private List<Item> itemList;
    private int layoutMode;

    private Context context;

    private SearchPreference searchPref;
    private SortPreference sortPref;

    private ItemLoadFinishListener itemLoadFinishListener;
    private ItemSelectListener itemSelectListener;

    private static final DiffUtil.ItemCallback<Item> DIFF_CALLBACK = new DiffUtil.ItemCallback<Item>(){
        @Override
        public boolean areItemsTheSame(Item oldItem, Item newItem){
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Item oldItem, Item newItem){
            return oldItem.equals(newItem);
        }
    };

    /*
     * Private Field for basically a HashMap, but with a better memory efficiency
     * Binds like HashMap<Integer (itemId), Review (Review Class)>
     */
    private SparseArray<ArrayList<Review>> reviewHashMap;
    private ItemFilter itemFilter;

    public ItemAdapter(int layoutMode, Context context){
        super(DIFF_CALLBACK);
        this.context = context;
        this.listElementWrappers = new ArrayList<>();
        this.reviewHashMap = new SparseArray<>();
        this.layoutMode = layoutMode;

        this.searchPref = new SearchPreference();
        this.sortPref = new SortPreference();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = null;
        switch(layoutMode){
            case FULL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_full, parent, false);
                break;
            case NORMAL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_normal, parent, false);
                break;
            case SMALL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_small, parent, false);
                break;
            case COMPACT_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_compact, parent, false);
                break;
        }
        return new ItemViewHolder(itemView, layoutMode);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position){
        final Item item = getItem(position);
        if(item != null){
            final ArrayList<Review> reviewArrayList = reviewHashMap.get(item.getId());

            holder.bindDataToView(reviewArrayList, this, layoutMode, item, position);
            holder.setElementClickListener(listElementWrappers, itemSelectListener, this
                    , item, position);
        }

    }

    public void changeCardState(ItemViewHolder holder, int position){
        if(listElementWrappers.get(position).isShowing()){
            //holder.cardView.setCardBackgroundColor(Color.parseColor("#dbe8ff"));
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F6F7F2"));
            holder.cardView.setElevation(5);
        }else{
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.cardView.setElevation(2);
        }
    }

    @Override
    public int getItemCount(){
        // if(listElementWrappers != null){
        //     return listElementWrappers.size();
        // }else{
        //     return 0;
        // }
        return super.getItemCount();
    }

    public void applyItemDataChanges(List<Item> itemArrayList, boolean isFiltering){
        // this.listElementWrappers.clear();
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ItemDiffCallback(itemList, itemArrayList));
        diffResult.dispatchUpdatesTo(this);

        // notifyDataSetChanged();     // Notifies data changes after clearance to prevent java.lang.IndexOutOfBoundsException: Inconsistency detected.
        if(itemLoadFinishListener != null){
            int size;
            if(itemArrayList != null){
                size = itemArrayList.size();
            }else{
                size = 0;
            }
            itemLoadFinishListener.onItemFinishUpdate(size);
        }

        if(itemArrayList == null || itemArrayList.size() == 0){
            return;
        }

        if(!isFiltering){
            itemList = itemArrayList;
        }

        // Find old selected item id
        int oldSelectedItemId = 1;
        for(int i = 0; i < listElementWrappers.size(); i++){
            if(listElementWrappers.get(i).isShowing()){
                oldSelectedItemId = i;
                break;
            }
        }

        listElementWrappers.clear();
        notifyDataSetChanged();

        for(int i = 0; i < itemArrayList.size(); i++){
            this.listElementWrappers.add(new ItemListWrapper(itemArrayList.get(i)));
            if(oldSelectedItemId == listElementWrappers.get(i).getItem().getId()){
                listElementWrappers.get(i).setShowing(true);
                notifyDataSetChanged();
            }
        }

        // if(this.listElementWrappers.size() != 0 && this.listElementWrappers.get(0) != null){
        //     this.listElementWrappers.get(0).setShowing(true);
        // }
        // notifyDataSetChanged();
    }

    public void applySorting(){
        if(((AppCompatActivity) context).getSupportLoaderManager().hasRunningLoaders()){
            ((AppCompatActivity) context).getSupportLoaderManager().destroyLoader(1);
        }
        ((AppCompatActivity) context).getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<List<ItemListWrapper>>(){
            @NonNull
            @Override
            public Loader<List<ItemListWrapper>> onCreateLoader(int id, @Nullable Bundle args){
                return new SortingAsyncTaskLoader(context, listElementWrappers, sortPref);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<List<ItemListWrapper>> loader, List<ItemListWrapper> data){
                listElementWrappers = data;
                notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(@NonNull Loader<List<ItemListWrapper>> loader){

            }
        }).forceLoad();

        notifyDataSetChanged();
    }

    public void applyReviewDataChanges(SparseArray<ArrayList<Review>> reviewHashMap){
        this.reviewHashMap = reviewHashMap;
        notifyDataSetChanged();
    }

    public SearchPreference getSearchPreference(){
        return searchPref;
    }

    public void setSearchPreference(SearchPreference searchPref){
        this.searchPref = searchPref;
    }

    public void setSortPreference(SortPreference sortPref){
        this.sortPref = sortPref;
    }

    @Override
    public Filter getFilter(){
        if(itemFilter == null){
            itemFilter = new ItemFilter();
        }
        return itemFilter;
    }

    public void setSelected(int itemId){
        for(int i = 0; i < listElementWrappers.size(); i++){
            if(listElementWrappers.get(i).getItem().getId() == itemId){
                listElementWrappers.get(i).setShowing(true);
            }else{
                listElementWrappers.get(i).setShowing(false);
            }
        }
    }

    public SortPreference getSortPref(){
        return sortPref;
    }

    public void setItemLoadFinishListener(ItemLoadFinishListener itemLoadFinishListener){
        this.itemLoadFinishListener = itemLoadFinishListener;
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
    }

    public static class ItemListWrapper{
        private Item item;
        private boolean isShowing;

        public ItemListWrapper(Item item){
            this.item = item;
            this.isShowing = false;
        }

        public static void clearOlderShowFlags(List<ItemListWrapper> itemListWrappers, Item newlyFlaggedItem){
            for(int i = 0; i < itemListWrappers.size(); i++){
                ItemListWrapper itemListWrapper = itemListWrappers.get(i);
                if(newlyFlaggedItem != itemListWrapper.getItem()){
                    itemListWrapper.setShowing(false);
                }
            }
        }

        public Item getItem(){
            return item;
        }

        public void setItem(Item item){
            this.item = item;
        }

        public boolean isShowing(){
            return isShowing;
        }

        public void setShowing(boolean showing){
            isShowing = showing;
        }
    }

    private class ItemFilter extends Filter{

        public ItemFilter(){
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            FilterResults filterResults = new FilterResults();

            List<Item> resultList = new ArrayList<>();

            String query = null;
            if(constraint != null && !constraint.toString().equals(SEARCH_ALL_ITEMS)){
                query = constraint.toString();
            }else if(constraint == null){
                query = null;
            }else if(constraint.toString().equals(SEARCH_ALL_ITEMS)){
                query = null;
            }
            System.out.println(searchPref.toString());
            for(Item item : itemList){
                // System.out.println("ID: " + item.getId() + ", "+ item.getName());

                String fieldValue = item.getName();
                if(searchPref.getSearchBy() == SearchPreference.SearchBy.ItemName){
                    fieldValue = item.getName();
                }else if(searchPref.getSearchBy() == SearchPreference.SearchBy.ItemId){
                    fieldValue = String.valueOf(item.getId());
                }else if(searchPref.getSearchBy() == SearchPreference.SearchBy.ItemDescription){
                    fieldValue = item.getDescription();
                }

                if(query != null){
                    if(!constraint.toString().isEmpty()){
                        if(fieldValue.toLowerCase().contains(query.toLowerCase()) || fieldValue.equalsIgnoreCase(query)){
                            filterList(searchPref, item, resultList);
                        }
                    }else{
                        // resultList.add(item);
                        filterList(searchPref, item, resultList);
                    }
                }else{
                    // resultList.add(item);
                    filterList(searchPref, item, resultList);
                }
            }
            filterResults.values = resultList;
            filterResults.count = resultList.size();

            // System.out.println("Result Size " + resultList.size());
            return filterResults;
        }

        private void filterList(SearchPreference preference, Item item, List<Item> resultList){
            // TODO: Fix this shit
            DatePreference dateCreatedFromPref = preference.getDatePreference(SearchPreference.DateType.DateCreated_From);
            DatePreference dateCreatedToPref = preference.getDatePreference(SearchPreference.DateType.DateCreated_To);
            DatePreference dateModifiedFromPref = preference.getDatePreference(SearchPreference.DateType.DateModified_From);
            DatePreference dateModifiedToPref = preference.getDatePreference(SearchPreference.DateType.DateModified_To);

            // System.out.println("dateCreatedFromPref: \n" + dateCreatedFromPref.toString());
            // System.out.println("dateCreatedToPref: \n" + dateCreatedToPref.toString());
            // System.out.println("dateModifiedFromPref: \n" + dateModifiedFromPref.toString());
            // System.out.println("dateModifiedToPref: \n" + dateModifiedToPref.toString());

            if(dateCreatedFromPref.isPreferenceEnabled()
                    && !(dateCreatedFromPref.getDate().getTime() <= item.getDateCreated().getTime())){
                // System.out.println("Removed Item State #1");
                return;
            }

            if(dateCreatedToPref.isPreferenceEnabled()
                    && !(dateCreatedToPref.getDate().getTime() >= item.getDateCreated().getTime())){
                // System.out.println("Removed Item State #2");
                return;
            }

            if(dateModifiedFromPref.isPreferenceEnabled()
                    && !(dateModifiedFromPref.getDate().getTime() <= item.getDateModified().getTime())){
                // System.out.println("Removed Item State #3");
                return;
            }

            if(dateModifiedToPref.isPreferenceEnabled()
                    && !(dateModifiedToPref.getDate().getTime() >= item.getDateModified().getTime())){
                // System.out.println("Removed Item State #4");
                return;
            }

            if(preference.getQuantityPreference().isPreferenceEnabled()
                    && !(preference.getQuantityPreference().getMaxRange() >= item.getQuantity() && preference.getQuantityPreference().getMinRange() <= item.getQuantity())){
                return;
            }

            // System.out.println("itemImageFile: " + item.getImageFile());
            if(searchPref.getImageMode() != SearchPreference.ANY_IMAGE){
                if(!((searchPref.getImageMode() == SearchPreference.CONTAINS_IMAGE) ? item.getImageFile() != null
                        : item.getImageFile() == null)){
                    return;
                }
            }
            resultList.add(item);
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results){
            List<Item> itemList = (List<Item>) results.values;
            if(itemList != null){
                if(!itemList.isEmpty()){
                    applyItemDataChanges(itemList, true);
                }
            }else{
                applyItemDataChanges(null, true);
                return;
            }

            if(itemList.isEmpty()){
                applyItemDataChanges(null, true);
            }
        }
    }

    public interface ItemLoadFinishListener{
        void onItemFinishUpdate(int size);
    }

    public interface ItemSelectListener{
        void onSelect(int itemId, int touchCoordinateY, ItemAdapter itemAdapter);
    }

}

