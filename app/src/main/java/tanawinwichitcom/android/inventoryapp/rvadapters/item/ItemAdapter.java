package tanawinwichitcom.android.inventoryapp.rvadapters.item;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.AsyncSorter;
import tanawinwichitcom.android.inventoryapp.ItemProfileContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileDialogFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.DatePreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference.SEARCH_ALL_ITEMS;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.COLOR_ACCENT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DATE_CREATED;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DATE_MODIFIED;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DESCRIPTION;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.ID;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.NAME;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.QUANTITY;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.RATING;

public class ItemAdapter extends ListAdapter<Item, ItemAdapter.ItemViewHolder> implements Filterable{

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_CARD_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;
    public static final int COMPACT_CARD_LAYOUT = 3;

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

            holder.bindDataToView(reviewArrayList, layoutMode, item);
            holder.setElementClickListener(itemSelectListener, this, item, position);
        }

    }

    public void changeCardState(ItemViewHolder holder, int position){
        if(getItem(position).showing){
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

        notifyDataSetChanged();

        submitList(itemArrayList);
        // if(this.listElementWrappers.size() != 0 && this.listElementWrappers.get(0) != null){
        //     this.listElementWrappers.get(0).setShowing(true);
        // }
        // notifyDataSetChanged();
    }

    public void applySorting(){
        if(((AppCompatActivity) context).getSupportLoaderManager().hasRunningLoaders()){
            ((AppCompatActivity) context).getSupportLoaderManager().destroyLoader(1);
        }
        // ((AppCompatActivity) context).getSupportLoaderManager().initLoader(1, null, new LoaderManager.LoaderCallbacks<List<Item>>(){
        //     @NonNull
        //     @Override
        //     public Loader<List<Item>> onCreateLoader(int id, @Nullable Bundle args){
        //         return new AsyncSorter(context, itemList, sortPref);
        //     }
        //
        //     @Override
        //     public void onLoadFinished(@NonNull Loader<List<Item>> loader, List<Item> data){
        //         itemList = data;
        //         submitList(itemList);
        //         // notifyDataSetChanged();
        //     }
        //
        //     @Override
        //     public void onLoaderReset(@NonNull Loader<List<Item>> loader){
        //
        //     }
        // }).forceLoad();

        if(itemList == null){
            return;
        }

        itemList = AsyncSorter.sort(itemList, sortPref);
        applyItemDataChanges(itemList, true);
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
        // TODO: Fix selection issue
        for(int i = 0; i < getItemCount(); i++){
            if(getItem(i).getId() == itemId){
                getItem(i).showing = true;
            }else{
                getItem(i).showing = false;
            }
        }
    }

    public SortPreference getSortPreference(){
        return sortPref;
    }

    public void setItemLoadFinishListener(ItemLoadFinishListener itemLoadFinishListener){
        this.itemLoadFinishListener = itemLoadFinishListener;
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
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
            // System.out.println(searchPref.toString());
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
            applyItemDataChanges(itemList, true);
            // if(itemList != null){
            //     if(!itemList.isEmpty()){
            //         applyItemDataChanges(itemList, true);
            //     }
            // }else{
            //     applyItemDataChanges(null, true);
            //     return;
            // }
            //
            // if(itemList.isEmpty()){
            //     applyItemDataChanges(null, true);
            // }
        }
    }

    public interface ItemLoadFinishListener{
        void onItemFinishUpdate(int size);
    }

    public interface ItemSelectListener{
        void onSelect(int itemId, int touchCoordinateY, ItemAdapter itemAdapter);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        private TextView nameTextView, ratingTextView, quantityTextView, descriptionTextView;
        private RatingBar ratingBar;
        private ImageView imageView;

        ItemViewHolder(View itemView, int layoutMode){
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCardView);
            nameTextView = itemView.findViewById(R.id.itemTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            imageView = itemView.findViewById(R.id.imageView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);

            if(layoutMode == FULL_CARD_LAYOUT){
                descriptionTextView = itemView.findViewById(R.id.shortDescriptionTextView);
            }

            ratingBar = itemView.findViewById(R.id.ratingBarView);
        }

        public void bindDataToView(List<Review> reviewArrayList, int layoutMode, Item item){
            final Context context = cardView.getContext();

            final boolean screenIsLargeOrPortrait = HelperUtility.isScreenLargeOrPortrait(context);

            double averageRating = Review.calculateAverage(reviewArrayList);
            item.setRating(averageRating);
            int numberOfReviews = (reviewArrayList == null) ? 0 : reviewArrayList.size();


            // if(!screenIsLargeOrPortrait && context instanceof MainActivity){
            //     itemAdapter.changeCardState(this, position);
            // }

            if(item.getImageFile() != null){
                Glide.with(cardView.getContext())
                        .load(item.getImageFile())
                        .apply(RequestOptions.centerCropTransform())
                        .thumbnail(0.01f)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
            }else{
                Glide.with(cardView.getContext())
                        .load(R.drawable.md_wallpaper_placeholder)
                        .apply(RequestOptions.centerCropTransform())
                        .thumbnail(0.01f)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
            }

            nameTextView.setText(item.getName());

            String rating = String.format(HelperUtility.getCurrentLocale(context), "%.1f", item.getRating());
            String numbersOfReviews = NumberFormat.getNumberInstance(Locale.US).format(numberOfReviews);
            String shortenQuantityNumber = HelperUtility.shortenNumber((long) item.getQuantity());

            ratingTextView.setText(new StringBuilder().append(rating).append(" (").append(numbersOfReviews).append(")").toString());
            quantityTextView.setText(new StringBuilder().append(shortenQuantityNumber).toString());
            if(layoutMode == FULL_CARD_LAYOUT){
                descriptionTextView.setText(item.getDescription());
            }
            if(item.getRating() != null){
                ratingBar.setRating(Float.valueOf(String.valueOf(item.getRating())));
            }else{
                ratingBar.setRating((float) 0.0);
            }


        }

        @SuppressLint("ClickableViewAccessibility")
        public void setElementClickListener(final ItemSelectListener itemSelectListener, final ItemAdapter itemAdapter
                , final Item item, final int position){
            final int touchCoordinate[] = new int[2];

            cardView.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    // save the X,Y coordinates
                    if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                        v.getLocationOnScreen(touchCoordinate);
                        // touchCoordinate[0] = event.getX();
                        // touchCoordinate[1] = (int) event.getY();
                    }
                    return false;
                }
            });

            final ItemViewHolder viewHolder = this;
            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    if(HelperUtility.isScreenLargeOrPortrait(v.getContext())){
                        // Toast.makeText(v.getContext(), "Item #" + position + " is clicked...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(v.getContext(), ItemProfileContainerActivity.class);
                        intent.putExtra("itemId", item.getId());
                        v.getContext().startActivity(intent);
                    }else{
                        FragmentTransaction fragmentTransaction = ((AppCompatActivity) v.getContext()).getSupportFragmentManager().beginTransaction();
                        if(v.getContext() instanceof MainActivity){
                            if(!item.showing){
                                itemAdapter.changeCardState(viewHolder, position);
                                if(itemSelectListener != null){
                                    itemSelectListener.onSelect(item.getId(), touchCoordinate[1], itemAdapter);
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
    }
}

