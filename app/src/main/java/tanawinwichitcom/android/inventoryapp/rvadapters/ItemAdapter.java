package tanawinwichitcom.android.inventoryapp.rvadapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.ItemProfileContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileDialogFragment;
import tanawinwichitcom.android.inventoryapp.fragments.ItemProfileFragment;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

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
            public void onClick(View v){
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
                            listElementWrappers.get(position)
                                    .setShowing(!listElementWrappers.get(position).isShowing());

                            ItemListElementWrapper
                                    .clearOlderShowFlags(listElementWrappers, listElementWrappers.get(position).getItem());

                            notifyDataSetChanged();
                            changeCardState(holder, position);


                            System.out.println("X:" + touchCoordinate[0] + ", Y:" + touchCoordinate[1]);

                            ItemProfileFragment itemProfileFragment
                                    = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, item.getId(),
                                    0, touchCoordinate[1]);

                            fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                            // fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                            fragmentTransaction.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
                            fragmentTransaction.commit();
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
        this.listElementWrappers.clear();
        notifyDataSetChanged();     // Notifies data changes after clearance to prevent java.lang.IndexOutOfBoundsException: Inconsistency detected.
        if(itemArrayList == null){
            return;
        }

        if(itemArrayList.size() == 0){
            return;
        }

        if(!isFiltering){
            itemList = itemArrayList;
        }

        for(Item item : itemArrayList){
            this.listElementWrappers.add(new ItemListElementWrapper(item));
        }

        if(!this.listElementWrappers.isEmpty() && this.listElementWrappers.get(0) != null){
            this.listElementWrappers.get(0).setShowing(true);
        }
        notifyDataSetChanged();
    }

    public void applyReviewDataChanges(SparseArray<ArrayList<Review>> reviewHashMap){
        this.reviewHashMap = reviewHashMap;
        notifyDataSetChanged();
    }

    public SearchPreference getSearchPreference(){
        return searchPref;
    }

    @Override
    public Filter getFilter(){
        if(itemFilter == null){
            itemFilter = new ItemFilter();
        }
        return itemFilter;
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

    static class ItemListElementWrapper{
        private Item item;
        private boolean isShowing;

        public ItemListElementWrapper(Item item){
            this.item = item;
            this.isShowing = false;
        }

        public static void clearOlderShowFlags(List<ItemListElementWrapper> itemListElementWrappers, Item newlyFlaggedItem){
            for(ItemListElementWrapper itemListElementWrapper : itemListElementWrappers){
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

    public static class SearchPreference implements Parcelable{
        private SearchBy searchByPref;
        private boolean containsImage;
        private HashMap<DateType, DatePreference> datePrefHashMap = new HashMap<>();
        private QuantityPreference quantityPreference;
        private String keyword;

        public SearchPreference(){
            // Set Default Preferences
            Date currentTime = Calendar.getInstance().getTime();
            for(DateType dateType : DateType.values()){
                this.datePrefHashMap.put(dateType, new DatePreference(currentTime));
            }
            searchByPref = SearchBy.ItemName;
            containsImage = false;
            quantityPreference = new QuantityPreference();
        }

        public void setDatePreference(DateType dateType, Date date){
            if(!datePrefHashMap.containsKey(dateType)){
                datePrefHashMap.put(dateType, new DatePreference(date));
            }else{
                datePrefHashMap.get(dateType).setDate(date);
            }
        }

        public DatePreference getDatePreference(DateType dateType){
            return datePrefHashMap.get(dateType);
        }

        public SearchBy getSearchBy(){
            return searchByPref;
        }

        public void setSearchBy(SearchBy searchBy){
            if(searchBy != null){
                this.searchByPref = searchBy;
            }else{
                this.searchByPref = SearchBy.ItemName;
            }
        }

        public boolean isContainsImage(){
            return containsImage;
        }

        public void setContainsImage(boolean containsImage){
            this.containsImage = containsImage;
        }

        public String getKeyword(){
            return keyword;
        }

        public void setKeyword(String keyword){
            this.keyword = keyword;
        }

        public QuantityPreference getQuantityPreference(){
            return quantityPreference;
        }

        @Override
        public String toString(){
            StringBuilder stringBuilder = new StringBuilder();

            for(DateType dateType : datePrefHashMap.keySet()){
                stringBuilder.append("\t\t" + dateType.toString() + ": " + datePrefHashMap.get(dateType).isPreferenceEnabled() + ", " + datePrefHashMap.get(dateType).getDate().getTime() + "\n");
            }

            return "SearchPreference{" +
                    "searchByPref = " + searchByPref.toString() +
                    ", containsImage = " + containsImage +
                    ", datePrefHashMap = \n" + stringBuilder +
                    ", keyword = '" + keyword + '\'' +
                    '}';
        }

        public enum SearchBy{ItemName, ItemId, ItemDescription;}

        public enum DateType{DateCreated_From, DateCreated_To, DateModified_From, DateModified_To}

        public static class SwitchablePreference{
            private boolean isPreferenceEnabled;

            public boolean isPreferenceEnabled(){
                return isPreferenceEnabled;
            }

            public void setPreferenceEnabled(boolean preferenceEnabled){
                isPreferenceEnabled = preferenceEnabled;
            }
        }

        public static class DatePreference extends SwitchablePreference implements Parcelable{
            private Date date;

            public DatePreference(Date date){
                this.date = date;
            }

            public Date getDate(){
                return date;
            }

            public void setDate(Date date){
                this.date = date;
            }

            @Override
            public String toString(){
                final StringBuilder sb = new StringBuilder("DatePreference{");
                sb.append("date = ").append(date.getTime());
                sb.append(", isPreferenceEnabled = ").append(super.isPreferenceEnabled);
                sb.append('}');
                return sb.toString();
            }

            @Override
            public int describeContents(){
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags){
                dest.writeLong(this.date != null ? this.date.getTime() : -1);
            }

            protected DatePreference(Parcel in){
                long tmpDate = in.readLong();
                this.date = tmpDate == -1 ? null : new Date(tmpDate);
            }

            public static final Creator<DatePreference> CREATOR = new Creator<DatePreference>(){
                @Override
                public DatePreference createFromParcel(Parcel source){
                    return new DatePreference(source);
                }

                @Override
                public DatePreference[] newArray(int size){
                    return new DatePreference[size];
                }
            };
        }

        public static class QuantityPreference extends SwitchablePreference implements Parcelable{
            private int maxRange;
            private int minRange;

            public QuantityPreference(){
            }

            public int getMaxRange(){
                return maxRange;
            }

            public void setMaxRange(int maxRange){
                this.maxRange = maxRange;
            }

            public int getMinRange(){
                return minRange;
            }

            public void setMinRange(int minRange){
                this.minRange = minRange;
            }

            @Override
            public int describeContents(){
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags){
                dest.writeInt(this.maxRange);
                dest.writeInt(this.minRange);
            }

            protected QuantityPreference(Parcel in){
                this.maxRange = in.readInt();
                this.minRange = in.readInt();
            }

            public static final Creator<QuantityPreference> CREATOR = new Creator<QuantityPreference>(){
                @Override
                public QuantityPreference createFromParcel(Parcel source){
                    return new QuantityPreference(source);
                }

                @Override
                public QuantityPreference[] newArray(int size){
                    return new QuantityPreference[size];
                }
            };
        }

        @Override
        public int describeContents(){
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags){
            dest.writeInt(this.searchByPref == null ? -1 : this.searchByPref.ordinal());
            dest.writeByte(this.containsImage ? (byte) 1 : (byte) 0);
            dest.writeSerializable(this.datePrefHashMap);
            dest.writeParcelable(this.quantityPreference, flags);
            dest.writeString(this.keyword);
        }

        protected SearchPreference(Parcel in){
            int tmpSearchByPref = in.readInt();
            this.searchByPref = tmpSearchByPref == -1 ? null : SearchBy.values()[tmpSearchByPref];
            this.containsImage = in.readByte() != 0;
            this.datePrefHashMap = (HashMap<DateType, DatePreference>) in.readSerializable();
            this.quantityPreference = in.readParcelable(QuantityPreference.class.getClassLoader());
            this.keyword = in.readString();
        }

        public static final Parcelable.Creator<SearchPreference> CREATOR = new Parcelable.Creator<SearchPreference>(){
            @Override
            public SearchPreference createFromParcel(Parcel source){
                return new SearchPreference(source);
            }

            @Override
            public SearchPreference[] newArray(int size){
                return new SearchPreference[size];
            }
        };
    }

    private class ItemFilter extends Filter{

        public ItemFilter(){
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint){
            FilterResults filterResults = new FilterResults();

            List<Item> resultList = new ArrayList<>();

            String query;
            if(constraint != null){
                query = constraint.toString();
            }else{
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
            SearchPreference.DatePreference dateCreatedFromPref = preference.getDatePreference(SearchPreference.DateType.DateCreated_From);
            SearchPreference.DatePreference dateCreatedToPref = preference.getDatePreference(SearchPreference.DateType.DateCreated_To);
            SearchPreference.DatePreference dateModifiedFromPref = preference.getDatePreference(SearchPreference.DateType.DateModified_From);
            SearchPreference.DatePreference dateModifiedToPref = preference.getDatePreference(SearchPreference.DateType.DateModified_To);

            System.out.println("dateCreatedFromPref: \n" + dateCreatedFromPref.toString());
            System.out.println("dateCreatedToPref: \n" + dateCreatedToPref.toString());
            System.out.println("dateModifiedFromPref: \n" + dateModifiedFromPref.toString());
            System.out.println("dateModifiedToPref: \n" + dateModifiedToPref.toString());

            if(dateCreatedFromPref.isPreferenceEnabled()
                    && !(dateCreatedFromPref.getDate().getTime() <= item.getDateCreated().getTime())){
                System.out.println("Removed Item State #1");
                return;
            }

            if(dateCreatedToPref.isPreferenceEnabled()
                    && !(dateCreatedToPref.getDate().getTime() >= item.getDateCreated().getTime())){
                System.out.println("Removed Item State #2");
                return;
            }

            if(dateModifiedFromPref.isPreferenceEnabled()
                    && !(dateModifiedFromPref.getDate().getTime() <= item.getDateModified().getTime())){
                System.out.println("Removed Item State #3");
                return;
            }

            if(dateModifiedToPref.isPreferenceEnabled()
                    && !(dateModifiedToPref.getDate().getTime() >= item.getDateModified().getTime())){
                System.out.println("Removed Item State #4");
                return;
            }

            if(preference.getQuantityPreference().isPreferenceEnabled()
                    && !(preference.getQuantityPreference().getMaxRange() >= item.getQuantity() && preference.getQuantityPreference().getMinRange() <= item.getQuantity())){
                return;
            }

            // System.out.println("itemImageFile: " + item.getImageFile());
            if(!((searchPref.isContainsImage()) ? item.getImageFile() != null
                    : item.getImageFile() == null)){
                return;
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
}

