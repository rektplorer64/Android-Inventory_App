package tanawinwichitcom.android.inventoryapp.rvadapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.ItemProfileContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.SortingAsyncTaskLoader;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileDialogFragment;
import tanawinwichitcom.android.inventoryapp.objectdiffutil.ItemDiffCallback;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.DatePreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference.SEARCH_ALL_ITEMS;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements Filterable{

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_CARD_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;
    public static final int COMPACT_CARD_LAYOUT = 3;
    private List<ItemListElementWrapper> listElementWrappers;
    private List<Item> itemList;
    private int layoutMode;

    private Context context;

    private SearchPreference searchPref;
    private SortPreference sortPref;

    private ItemLoadFinishListener itemLoadFinishListener;
    private ItemSelectListener itemSelectListener;

    /*
     * Private Field for basically a HashMap, but with a better memory efficiency
     * Binds like HashMap<Integer (itemId), Review (Review Class)>
     */
    private SparseArray<ArrayList<Review>> reviewHashMap;
    private ItemFilter itemFilter;
    private Activity activity;

    public ItemAdapter(int layoutMode, Context context, Activity activity){
        this.context = context;
        this.activity = activity;
        this.listElementWrappers = new ArrayList<>();
        this.reviewHashMap = new SparseArray<>();
        this.layoutMode = layoutMode;

        this.searchPref = new SearchPreference();
        this.sortPref = new SortPreference();
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
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
        return new ItemAdapter.ViewHolder(itemView, layoutMode);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemAdapter.ViewHolder holder, final int position){
        final Item item = listElementWrappers.get(position).getItem();
        final ArrayList<Review> reviewArrayList = reviewHashMap.get(item.getId());

        final boolean screenIsLargeOrPortrait = HelperUtility.isScreenLargeOrPortrait(holder.cardView.getContext());

        double averageRating = Review.calculateAverage(reviewArrayList);
        item.setRating(averageRating);
        int numberOfReviews = (reviewArrayList == null) ? 0 : reviewArrayList.size();

        // if(position == 0){
        //     ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
        //     int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, holder.cardView.getContext().getResources().getDisplayMetrics());
        //     int pxMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, holder.cardView.getContext().getResources().getDisplayMetrics());
        //     params.setMargins(pxMargin, pxMargin, pxMargin, pxMargin2);
        //     holder.cardView.setLayoutParams(params);
        // }else if(position == listElementWrappers.size() - 1){
        //     ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
        //     int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, holder.cardView.getContext().getResources().getDisplayMetrics());
        //     int pxMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, holder.cardView.getContext().getResources().getDisplayMetrics());
        //     params.setMargins(pxMargin, pxMargin2, pxMargin, pxMargin);
        //     holder.cardView.setLayoutParams(params);
        // }

        if(!screenIsLargeOrPortrait && activity instanceof MainActivity){
            changeCardState(holder, position);
        }

        if(item.getImageFile() != null){
            Glide.with(holder.cardView.getContext())
                    .load(item.getImageFile())
                    .apply(RequestOptions.centerCropTransform())
                    .thumbnail(0.01f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageView);
        }else{
            Glide.with(holder.cardView.getContext())
                    .load(R.drawable.md_wallpaper_placeholder)
                    .apply(RequestOptions.centerCropTransform())
                    .thumbnail(0.01f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageView);
        }

        holder.nameTextView.setText(item.getName());

        String rating = String.format(HelperUtility.getCurrentLocale(context), "%.1f", item.getRating());
        String numbersOfReviews = NumberFormat.getNumberInstance(Locale.US).format(numberOfReviews);
        String shortenQuantityNumber = HelperUtility.shortenNumber((long) item.getQuantity());

        holder.ratingTextView.setText(new StringBuilder().append(rating).append(" (").append(numbersOfReviews).append(")").toString());
        holder.quantityTextView.setText(new StringBuilder().append(shortenQuantityNumber).toString());
        if(layoutMode == FULL_CARD_LAYOUT){
            holder.descriptionTextView.setText(item.getDescription());
        }
        if(item.getRating() != null){
            holder.ratingBar.setRating(Float.valueOf(String.valueOf(item.getRating())));
        }else{
            holder.ratingBar.setRating((float) 0.0);
        }

        final int touchCoordinate[] = new int[2];

        holder.cardView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                // save the X,Y coordinates
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    v.getLocationOnScreen(touchCoordinate);
                    // touchCoordinate[0] = event.getX();
                    // touchCoordinate[1] = event.getY();
                }
                return false;
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v){
                if(screenIsLargeOrPortrait){
                    // Toast.makeText(v.getContext(), "Item #" + position + " is clicked...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), ItemProfileContainerActivity.class);
                    intent.putExtra("itemId", item.getId());
                    v.getContext().startActivity(intent);
                }else{
                    FragmentTransaction fragmentTransaction
                            = ((AppCompatActivity) v.getContext())
                            .getSupportFragmentManager().beginTransaction();
                    if(activity instanceof MainActivity){
                        if(!listElementWrappers.get(position).isShowing()){
                            listElementWrappers.get(position).setShowing(!listElementWrappers.get(position).isShowing());
                            ItemListElementWrapper
                                    .clearOlderShowFlags(listElementWrappers, listElementWrappers.get(position).getItem());

                            notifyDataSetChanged();
                            changeCardState(holder, position);

                            if(itemSelectListener != null){
                                itemSelectListener.onSelect(item.getId(), touchCoordinate[1]);
                            }
                        }
                    }else{
                        ItemProfileDialogFragment itemProfileDialog = ItemProfileDialogFragment.newInstance(item.getId());
                        itemProfileDialog.show(fragmentTransaction, "itemProfileDialog");
                    }
                }
            }
        });
    }

    private void changeCardState(ViewHolder holder, int position){
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
        if(listElementWrappers != null){
            return listElementWrappers.size();
        }else{
            return 0;
        }
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

        listElementWrappers.clear();
        notifyDataSetChanged();
        for(Item item : itemArrayList){
            this.listElementWrappers.add(new ItemListElementWrapper(item));
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
        ((AppCompatActivity) context).getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<List<ItemListElementWrapper>>(){
            @NonNull
            @Override
            public Loader<List<ItemListElementWrapper>> onCreateLoader(int id, @Nullable Bundle args){
                return new SortingAsyncTaskLoader(context, listElementWrappers, sortPref);
            }

            @Override
            public void onLoadFinished(@NonNull Loader<List<ItemListElementWrapper>> loader, List<ItemListElementWrapper> data){
                listElementWrappers = data;
                notifyDataSetChanged();
            }

            @Override
            public void onLoaderReset(@NonNull Loader<List<ItemListElementWrapper>> loader){

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

    public void invokeItemPressing(int num, final RecyclerView recyclerView, boolean isItemId){
        int index = num;
        if(isItemId){
            for(int i = 0; i < listElementWrappers.size(); i++){
                if(num == listElementWrappers.get(i).getItem().getId()){
                    index = i;
                    break;
                }
            }
        }

        final int finalIndex = index;
        // new Handler().postDelayed(new Runnable(){
        //     @Override
        //     public void run(){
        //         recyclerView.findViewHolderForAdapterPosition(finalIndex).itemView.performClick();
        //     }
        // }, 1);
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw(){
                recyclerView.findViewHolderForAdapterPosition(finalIndex).itemView.performClick();
                recyclerView.getViewTreeObserver().addOnPreDrawListener(this);
                return true;
            }
        });
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

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView nameTextView, ratingTextView, quantityTextView, descriptionTextView;
        RatingBar ratingBar;
        ImageView imageView;

        ViewHolder(View itemView, int layoutMode){
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCardView);
            nameTextView = itemView.findViewById(R.id.itemTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            imageView = itemView.findViewById(R.id.imageView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);

            if(layoutMode == ItemAdapter.FULL_CARD_LAYOUT){
                descriptionTextView = itemView.findViewById(R.id.shortDescriptionTextView);
            }

            ratingBar = itemView.findViewById(R.id.ratingBarView);
        }
    }

    public static class ItemListElementWrapper{
        private Item item;
        private boolean isShowing;

        public ItemListElementWrapper(Item item){
            this.item = item;
            this.isShowing = false;
        }

        public static void clearOlderShowFlags(List<ItemListElementWrapper> itemListElementWrappers, Item newlyFlaggedItem){
            for(int i = 0; i < itemListElementWrappers.size(); i++){
                ItemListElementWrapper itemListElementWrapper = itemListElementWrappers.get(i);
                if(newlyFlaggedItem != itemListElementWrapper.getItem()){
                    itemListElementWrapper.setShowing(false);
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
        void onSelect(int itemId, int touchCoordinateY);
    }

}

