package io.rektplorer.inventoryapp.rvadapters.item;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.text.PrecomputedTextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.selection.ItemKeyProvider;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.rektplorer.inventoryapp.AsyncSorter;
import io.rektplorer.inventoryapp.CollectionActivity;
import io.rektplorer.inventoryapp.ItemProfileContainerActivity;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.fragments.dialogfragment.ItemProfileDialogFragment;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.rvadapters.Detailable;
import io.rektplorer.inventoryapp.searchpreferencehelper.DatePreference;
import io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference;
import io.rektplorer.inventoryapp.searchpreferencehelper.ListLayoutPreference;
import io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference;
import io.rektplorer.inventoryapp.utility.ScreenUtility;
import io.rektplorer.inventoryapp.utility.StringUtility;

import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.DateType.DateCreated_From;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.DateType.DateCreated_To;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.DateType.DateModified_From;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.DateType.DateModified_To;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.SEARCH_ALL_ITEMS;

public class ItemAdapter extends ListAdapter<Item, ItemAdapter.ItemViewHolder> implements Filterable{

    private List<Item> itemList;

    private FilterPreference searchPref;
    private SortPreference sortPref;

    private ListLayoutPreference listViewModePref;

    private ItemLoadFinishListener itemLoadFinishListener;
    private ItemSelectListener itemSelectListener;

    private boolean isFiltering;

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
     * Private Field for basically a HashMap, but with better memory efficiency
     * Binds like HashMap<Integer (itemId), Review (Review Class)>
     */
    private SparseArray<ArrayList<Review>> reviewHashMap;
    private ItemFilter itemFilter;
    private SelectionTracker<Long> selectionTracker;
    private SparseArray<Image> heroImageSparseArray;

    public ItemAdapter(ListLayoutPreference listViewModePref){
        super(DIFF_CALLBACK);
        this.listViewModePref = listViewModePref;

        this.reviewHashMap = new SparseArray<>();
        this.searchPref = new FilterPreference();
        this.sortPref = new SortPreference();
        heroImageSparseArray = new SparseArray<>();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = null;
        switch(listViewModePref.getListLayoutMode()){
            case ListLayoutPreference.FULL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext())
                                         .inflate(R.layout.card_item_full, parent, false);
                break;
            case ListLayoutPreference.NORMAL_LIST_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext())
                                         .inflate(R.layout.card_item_normal, parent, false);
                break;
            case ListLayoutPreference.SMALL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext())
                                         .inflate(R.layout.card_item_small_card, parent, false);
                break;
            case ListLayoutPreference.COMPACT_LIST_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext())
                                         .inflate(R.layout.card_item_compact, parent, false);
                break;
        }
        return new ItemViewHolder(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position){
        final Item item = getItem(position);
        if(item != null){
            final ArrayList<Review> reviewArrayList = reviewHashMap.get(item.getId());
            holder.bindDataToView(reviewArrayList, item);

            boolean isSelected = (selectionTracker != null) && selectionTracker
                    .isSelected((long) item.getId());
            holder.setElementClickListener(itemSelectListener, this, item, isSelected);
        }
    }

    @Override
    public long getItemId(int position){
        return getItem(position).getId();
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

    public ListLayoutPreference getListViewModePreference(){
        return listViewModePref;
    }

    public void applySorting(){
        // if(((AppCompatActivity) context).getSupportLoaderManager().hasRunningLoaders()){
        //     ((AppCompatActivity) context).getSupportLoaderManager().destroyLoader(1);
        // }
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

    public void applyItemDataChanges(List<Item> itemArrayList, boolean isFiltering){
        // notifyDataSetChanged();     // Notifies data changes after clearance to prevent java.lang.IndexOutOfBoundsException: Inconsistency detected.
        if(itemLoadFinishListener != null){
            int size = (itemArrayList != null) ? itemArrayList.size() : 0;
            itemLoadFinishListener.onItemFinishUpdate(size);
        }

        if(itemArrayList == null || itemArrayList.size() == 0){
            return;
        }

        if(!isFiltering){
            itemList = itemArrayList;
        }
        this.isFiltering = isFiltering;

        submitList(new ArrayList<Item>());
        submitList(itemArrayList);
        // if(this.listElementWrappers.size() != 0 && this.listElementWrappers.get(0) != null){
        //     this.listElementWrappers.get(0).setShowing(true);
        // }
        // notifyDataSetChanged();
    }

    public void applyHeroImageDataChanges(List<Image> heroImageList){
        for(int i = 0; i < heroImageList.size(); i++){
            heroImageSparseArray.put(heroImageList.get(i).getItemId(), heroImageList.get(i));
        }
        notifyDataSetChanged();
    }

    public void applyReviewDataChanges(SparseArray<ArrayList<Review>> reviewHashMap){
        this.reviewHashMap = reviewHashMap;
        notifyDataSetChanged();
    }

    public FilterPreference getSearchPreference(){
        return searchPref;
    }

    public void setSearchPreference(FilterPreference searchPref){
        this.searchPref = searchPref;
    }

    public void setLayoutMode(int layoutMode, RecyclerView recyclerView){
        this.listViewModePref.setListLayoutMode(layoutMode);

        recyclerView.setVisibility(View.INVISIBLE);
        // Save the selected mode to SharedPreference
        // FilterPreference preference =

        // As far as I tested, I don't know how does this works, but do not change (order).
        recyclerView.swapAdapter(this, true);
        recyclerView.getRecycledViewPool().clear();
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public Filter getFilter(){
        if(itemFilter == null){
            itemFilter = new ItemFilter();
        }
        return itemFilter;
    }

    public SortPreference getSortPreference(){
        return sortPref;
    }

    public void setSortPreference(SortPreference sortPref){
        this.sortPref = sortPref;
    }

    public void setItemLoadFinishListener(ItemLoadFinishListener itemLoadFinishListener){
        this.itemLoadFinishListener = itemLoadFinishListener;
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener){
        this.itemSelectListener = itemSelectListener;
    }

    public void setSetSelectionTracker(SelectionTracker<Long> selectionTracker){
        this.selectionTracker = selectionTracker;
    }

    public interface ItemLoadFinishListener{
        void onItemFinishUpdate(int size);
    }

    public interface ItemSelectListener{
        void onSelect(int itemId, int touchCoordinateY, ItemAdapter itemAdapter);
    }

    private class ItemFilter extends Filter{

        ItemFilter(){
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            FilterResults filterResults = new FilterResults();
            List<Item> resultList = new ArrayList<>();
            Set<String> tagPreferences = new HashSet<>(searchPref.getTagList());

            String query = null;
            if(constraint != null && !constraint.toString().equals(SEARCH_ALL_ITEMS)){
                query = constraint.toString();
            }else if(constraint == null || constraint.toString().equals(SEARCH_ALL_ITEMS)){
                query = null;
            }

            // System.out.println(searchPref.toString());
            for(Item item : itemList){
                // System.out.println("ID: " + item.getId() + ", "+ item.getName());
                String fieldValue = item.getName().toLowerCase();
                if(searchPref.getSearchBy() == FilterPreference.SearchBy.ItemName){
                    fieldValue = item.getName().toLowerCase();
                }else if(searchPref.getSearchBy() == FilterPreference.SearchBy.ItemId){
                    fieldValue = String.valueOf(item.getId());
                }else if(searchPref.getSearchBy() == FilterPreference.SearchBy.ItemDescription){
                    fieldValue = item.getDescription().toLowerCase();
                }

                if(query != null){
                    if(!constraint.toString().isEmpty()){
                        if(fieldValue.contains(query.toLowerCase()) || fieldValue
                                .equalsIgnoreCase(query)){
                            filterList(searchPref, item, resultList, tagPreferences);
                        }
                    }else{
                        // resultList.add(item);
                        filterList(searchPref, item, resultList, tagPreferences);
                    }
                }else{
                    // resultList.add(item);
                    filterList(searchPref, item, resultList, tagPreferences);
                }
            }
            filterResults.values = resultList;
            filterResults.count = resultList.size();

            // System.out.println("Result Size " + resultList.size());
            return filterResults;
        }

        private void filterList(FilterPreference pref, Item item, List<Item> resultList,
                                Set<String> tagPreferences){
            DatePreference dateCreatedFromPref = pref.getDatePreference(DateCreated_From);
            DatePreference dateCreatedToPref = pref.getDatePreference(DateCreated_To);
            DatePreference dateModifiedFromPref = pref.getDatePreference(DateModified_From);
            DatePreference dateModifiedToPref = pref.getDatePreference(DateModified_To);

            if(!tagPreferences.isEmpty() && !item.getTags().containsAll(tagPreferences)){
                return;
            }

            if(dateCreatedFromPref.isPreferenceEnabled()
                    && !(dateCreatedFromPref.getDate().getTime() <= item.getDateCreated()
                                                                        .getTime())){
                // System.out.println("Removed Item State #1");
                return;
            }

            if(dateCreatedToPref.isPreferenceEnabled()
                    && !(dateCreatedToPref.getDate().getTime() >= item.getDateCreated().getTime())){
                // System.out.println("Removed Item State #2");
                return;
            }

            if(dateModifiedFromPref.isPreferenceEnabled()
                    && !(dateModifiedFromPref.getDate().getTime() <= item.getDateModified()
                                                                         .getTime())){
                // System.out.println("Removed Item State #3");
                return;
            }

            if(dateModifiedToPref.isPreferenceEnabled()
                    && !(dateModifiedToPref.getDate().getTime() >= item.getDateModified()
                                                                       .getTime())){
                // System.out.println("Removed Item State #4");
                return;
            }

            if(pref.getQuantityPreference().isPreferenceEnabled()
                    && !(pref.getQuantityPreference().getMaxRange() >= item.getQuantity() && pref
                    .getQuantityPreference().getMinRange() <= item.getQuantity())){
                return;
            }

            // System.out.println("itemImageFile: " + item.getHeroImage());
            if(searchPref.getImageMode() != FilterPreference.ANY_IMAGE){
                if(!((searchPref
                        .getImageMode() == FilterPreference.CONTAINS_IMAGE) == (heroImageSparseArray
                        .get(item.getId()) != null))){
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

    public class ItemViewHolder extends RecyclerView.ViewHolder implements Detailable{
        CardView cardView;

        private ConstraintLayout imageConstraintLayout;

        private AppCompatTextView nameTextView, ratingTextView, quantityTextView, descriptionTextView;
        private RatingBar ratingBar;
        private ImageView imageView;
        private ChipGroup tagChipGroup;

        ItemViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCardView);

            imageConstraintLayout = itemView.findViewById(R.id.imageConstraintLayout);

            nameTextView = itemView.findViewById(R.id.itemTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            imageView = itemView.findViewById(R.id.imageView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);

            ratingBar = itemView.findViewById(R.id.ratingBarView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            tagChipGroup = itemView.findViewById(R.id.tagChipGroup);
        }

        void bindDataToView(List<Review> reviewArrayList, final Item item){
            final Context context = cardView.getContext();

            double averageRating = Review.calculateAverage(reviewArrayList);
            item.setRating(averageRating);
            int numberOfReviews = (reviewArrayList != null) ? reviewArrayList.size() : 0;

            RequestManager requestBuilder = Glide.with(context);    // Glide initializes
            RequestBuilder<Drawable> requestBuilder1 = null;
            if(heroImageSparseArray != null && heroImageSparseArray
                    .get(item.getId()) != null){        // If there is image file of current item
                if(imageConstraintLayout != null){
                    imageConstraintLayout.setVisibility(View.VISIBLE);
                }
                imageView.setVisibility(View.VISIBLE);

                requestBuilder1 = requestBuilder
                        .load(heroImageSparseArray.get(item.getId()).getImageFile())
                        .thumbnail(0.01f);
            }else{
                if(listViewModePref.getListLayoutMode() != ListLayoutPreference.FULL_CARD_LAYOUT){
                    if(imageConstraintLayout != null){
                        imageConstraintLayout.setVisibility(View.VISIBLE);
                    }
                    imageView.setVisibility(View.VISIBLE);

                    requestBuilder1 = requestBuilder
                            .load(R.drawable.md_wallpaper_placeholder)
                            .apply(RequestOptions.centerCropTransform())
                            .thumbnail(0.01f);
                }else{
                    if(imageConstraintLayout != null){
                        imageConstraintLayout.setVisibility(View.GONE);
                    }
                    imageView.setVisibility(View.INVISIBLE);
                }
            }

            if(requestBuilder1 != null){
                if(!(imageView instanceof CircleImageView)){
                    requestBuilder1
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(imageView);
                }else{
                    requestBuilder1
                            .into(imageView);
                }
            }

            if(imageConstraintLayout != null
                    && imageConstraintLayout.getVisibility() == View.VISIBLE
                    && listViewModePref
                    .getListLayoutMode() == ListLayoutPreference.FULL_CARD_LAYOUT){
                // if(heroImageSparseArray.get(item.getId()) != null){
                //     final ConstraintSet cs = new ConstraintSet();
                //     cs.clone(imageConstraintLayout);
                //
                //     // Asynchronously Calculate image aspect ratio
                //     new ImageViewSizeAsyncCalculator(cs, imageConstraintLayout, imageView)
                //             .execute(heroImageSparseArray.get(item.getId()).getImageFile());
                // }else{
                ConstraintSet cs = new ConstraintSet();
                cs.clone(imageConstraintLayout);
                cs.setDimensionRatio(
                        imageConstraintLayout.getViewById(imageView.getId()).getId(),
                        heroImageSparseArray.get(item.getId()).getAspectRatio());
                cs.applyTo(imageConstraintLayout);
                imageConstraintLayout.setConstraintSet(cs);
                // }
            }

            if(!isFiltering){
                PrecomputedTextCompat textCompat = PrecomputedTextCompat
                        .create(item.getName(), TextViewCompat.getTextMetricsParams(nameTextView));
                TextViewCompat.setPrecomputedText(nameTextView, textCompat);
            }else{
                if(searchPref.getKeyword() != null && !searchPref.getKeyword().isEmpty()){
                    String keyword = searchPref.getKeyword().toLowerCase();
                    if(item.getName().toLowerCase().contains(keyword)){
                        // Replacing all query occurrences without ruining original Capitalization
                        String formattedString = item.getName().replaceAll("(?i)(" + keyword + ")",
                                                                           "<b>$0</b>");
                        Spanned finalTagString;
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            finalTagString = Html
                                    .fromHtml(formattedString, Html.FROM_HTML_MODE_COMPACT);
                        }else{
                            finalTagString = Html.fromHtml(formattedString);
                        }
                        nameTextView.setText(finalTagString);
                    }else{
                        nameTextView.setText(item.getName());
                    }
                }else{
                    nameTextView.setText(item.getName());
                }
            }

            String rating = String
                    .format(ScreenUtility.getCurrentLocale(context), "%.1f", item.getRating());
            String numbersOfReviews = NumberFormat.getNumberInstance(Locale.US)
                                                  .format(numberOfReviews);
            String shortenQuantityNumber = StringUtility.shortenNumber(item.getQuantity());

            ratingTextView.setText(
                    new StringBuilder().append(rating).append(" (").append(numbersOfReviews)
                                       .append(")").toString());
            quantityTextView.setText(Html.fromHtml(
                    new StringBuilder().append("<font color=\"#93928E\">quantity</font><br>")
                                       .append(shortenQuantityNumber).toString()));

            if(descriptionTextView != null){
                int shortDecTextLength = Math.min(item.getDescription().length(), 100);
                String shortenDesc = item.getDescription()
                                         .substring(0,
                                                    shortDecTextLength) + ((shortDecTextLength <= item
                        .getDescription().length()) ? "" : "...");
                // descriptionTextView.setText(shortenDesc);

                PrecomputedTextCompat textCompat = PrecomputedTextCompat.create(shortenDesc,
                                                                                TextViewCompat
                                                                                        .getTextMetricsParams(
                                                                                                descriptionTextView));
                TextViewCompat.setPrecomputedText(descriptionTextView, textCompat);
            }

            if(tagChipGroup != null){
                tagChipGroup.removeAllViews();
                new TagChipAsyncBuilder(tagChipGroup)
                        .execute(item.getTags().toArray(new String[0]));
            }
            ratingBar.setRating((item.getRating() != null) ? Float
                    .valueOf(String.valueOf(item.getRating())) : 0f);

        }

        @SuppressLint("ClickableViewAccessibility")
        void setElementClickListener(final ItemSelectListener itemSelectListener,
                                     final ItemAdapter itemAdapter
                , final Item item, final boolean isFocused){
            final int touchCoordinate[] = new int[2];

            cardView.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event){
                    // save the X,Y coordinates
                    // v.getLocationOnScreen(touchCoordinate);
                    touchCoordinate[0] = (int) event.getRawX();
                    touchCoordinate[1] = (int) event.getRawY();
                    return false;
                }
            });

            // if(isFocused){
            //     cardView.setCardBackgroundColor(Color.parseColor("#E5E1E0"));
            //     // cardView.setCardBackgroundColor(Color.RED);
            // }else{
            //     cardView.setCardBackgroundColor(Color.WHITE);
            // }
            cardView.setSelected(isFocused);

            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(final View v){
                    if(ScreenUtility.isScreenLargeOrPortrait(v.getContext())){
                        // Toast.makeText(v.getContext(), "Item #" + position + " is clicked...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(v.getContext(),
                                                   ItemProfileContainerActivity.class);
                        intent.putExtra("itemId", item.getId());
                        v.getContext().startActivity(intent);
                    }else{
                        FragmentTransaction fragmentTransaction = ((AppCompatActivity) v
                                .getContext()).getSupportFragmentManager().beginTransaction();
                        if(v.getContext() instanceof CollectionActivity){
                            if(itemSelectListener != null && !isFocused){
                                itemSelectListener
                                        .onSelect(item.getId(), touchCoordinate[1], itemAdapter);
                            }
                        }else{
                            ItemProfileDialogFragment itemProfileDialog = ItemProfileDialogFragment
                                    .newInstance(item.getId());
                            itemProfileDialog.show(fragmentTransaction, "itemProfileDialog");
                        }
                    }
                }
            });
        }

        @Override
        public ItemDetailsLookup.ItemDetails<Long> getItemDetails(){
            return new ItemDetailsLookup.ItemDetails<Long>(){
                @Override
                public int getPosition(){
                    return getAdapterPosition();
                }

                @Override
                public Long getSelectionKey(){
                    return (long) getItem(getAdapterPosition()).getId();
                }
            };
        }
    }

    public static class TagChipAsyncBuilder extends AsyncTask<String, Chip, Void>{

        private WeakReference<ChipGroup> chipGroupWeakReference;

        TagChipAsyncBuilder(ChipGroup chipGroup){
            chipGroupWeakReference = new WeakReference<>(chipGroup);
        }

        @Override
        protected Void doInBackground(String... strings){
            Chip tagChip;
            for(String tag : strings){
                tagChip = new Chip(chipGroupWeakReference.get().getContext());
                tagChip.setText(tag);
                publishProgress(tagChip);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Chip... chips){
            super.onProgressUpdate(chips);
            if(chipGroupWeakReference.get() != null){
                chipGroupWeakReference.get().addView(chips[0]);
            }
        }
    }

    public static class ItemEntityKeyProvider extends ItemKeyProvider<Long>{

        private final ItemAdapter adapter;

        public ItemEntityKeyProvider(ItemAdapter adapter){
            super(ItemKeyProvider.SCOPE_CACHED);
            this.adapter = adapter;
        }

        @Override
        public Long getKey(int position){
            return (long) adapter.getItem(position).getId();
        }

        @Override
        public int getPosition(@NonNull Long key){
            for(int i = 0; i < adapter.getItemCount(); i++){
                Item item = adapter.getItem(i);
                if(item.getId() == key){
                    return i;
                }
            }
            return 0;
        }
    }
}

