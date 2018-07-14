package tanawinwichitcom.android.inventoryapp.RecyclerViewAdapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.Fragments.ItemProfileFragment;
import tanawinwichitcom.android.inventoryapp.HelperUtilities;
import tanawinwichitcom.android.inventoryapp.ItemProfileContainerActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> implements Filterable{

    private List<ItemListElementWrapper> listElementWrappers;
    private List<Item> itemList;

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_CARD_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;

    private int layoutMode;

    private Context context;

    /*
     * Private Field for basically a HashMap, but with a better memory efficiency
     * Binds like HashMap<Integer (itemId), Review (Review Class)>
     */
    private SparseArray<ArrayList<Review>> reviewHashMap;

    public ItemAdapter(int layoutMode, Comparator<Item> itemComparator, Context context){
        this.itemComparator = itemComparator;
        this.context = context;
        this.listElementWrappers = new ArrayList<>();
        this.reviewHashMap = new SparseArray<>();
        this.layoutMode = layoutMode;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = null;
        switch(layoutMode){
            case FULL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
                break;
            case NORMAL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_normal, parent, false);
                break;
            case SMALL_CARD_LAYOUT:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_normal, parent, false);
                break;
        }
        return new ItemAdapter.ViewHolder(itemView, layoutMode);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ItemAdapter.ViewHolder holder, final int position){
        final Item item = listElementWrappers.get(position).getItem();
        final ArrayList<Review> reviewArrayList = reviewHashMap.get(item.getId());

        final boolean screenIsLargeOrPortrait = HelperUtilities.isScreenLargeOrPortrait(holder.cardView.getContext());

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

        if(!screenIsLargeOrPortrait){
            changeCardState(holder, position);
        }


        if(item.getImageFile() != null){
            Glide.with(holder.cardView.getContext())
                    .load(item.getImageFile())
                    .apply(RequestOptions.centerCropTransform())
                    .thumbnail(0.25f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageView);
        }else{
            Glide.with(holder.cardView.getContext())
                    .load(R.drawable.md_wallpaper_placeholder)
                    .apply(RequestOptions.centerCropTransform())
                    .thumbnail(0.1f)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(holder.imageView);
        }

        holder.nameTextView.setText(item.getName());

        String rating = String.format(HelperUtilities.getCurrentLocale(context), "%.1f", item.getRating());
        String numbersOfReviews = NumberFormat.getNumberInstance(Locale.US).format(numberOfReviews);
        String shortenQuantityNumber = HelperUtilities.shortenNumber((long) item.getQuantity());

        holder.ratingTextView.setText(new StringBuilder().append(rating).append(" (").append(numbersOfReviews).append(")").toString());
        holder.quantityTextView.setText(new StringBuilder().append("QTY ").append(shortenQuantityNumber).toString());
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
                    Toast.makeText(v.getContext(), "Item #" + position + " is clicked...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(v.getContext(), ItemProfileContainerActivity.class);
                    intent.putExtra("itemId", item.getId());
                    v.getContext().startActivity(intent);
                }else{
                    if(!listElementWrappers.get(position).isShowing()){
                        listElementWrappers.get(position)
                                .setShowing(!listElementWrappers.get(position).isShowing());

                        ItemListElementWrapper
                                .clearOlderShowFlags(listElementWrappers, listElementWrappers.get(position).getItem());

                        notifyDataSetChanged();
                        changeCardState(holder, position);

                        FragmentTransaction fragmentTransaction
                                = ((AppCompatActivity) v.getContext())
                                .getSupportFragmentManager().beginTransaction();

                        System.out.println("X:" + touchCoordinate[0] + ", Y:" + touchCoordinate[1]);

                        ItemProfileFragment itemProfileFragment
                                = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, item.getId(),
                                0, touchCoordinate[1]);

                        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
                        // fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit);
                        fragmentTransaction.replace(R.id.itemProfileFragmentFrame, itemProfileFragment);
                        fragmentTransaction.commit();
                    }
                }

                // ItemProfileFragment itemProfileFragment = new ItemProfileFragment();
                // Bundle bundle = new Bundle();
                // bundle.putInt("itemId", item.getId());
                // itemProfileFragment.setArguments(bundle);
                // //fragmentTransaction.commit();
                // itemProfileFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogFragmentTheme);
                // itemProfileFragment.show(fragmentActivity.getSupportFragmentManager(), "shit");
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

    public void applyItemDataChanges(List<Item> itemArrayList){
        this.listElementWrappers.clear();
        this.itemList = itemArrayList;
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

    @Override
    public Filter getFilter(){
        return new Filter(){
            @Override
            protected FilterResults performFiltering(CharSequence constraint){
                FilterResults filterResults = new FilterResults();
                if(constraint == null || constraint.length() == 0){
                    filterResults.count = constraint.length();
                    filterResults.values = itemList;
                }else{
                    List<Item> itemList = new ArrayList<>();
                    for(Item item : itemList){
                        if(item.getName().toLowerCase().contains(constraint.toString().toLowerCase())){
                            itemList.add(item);
                        }
                    }
                    filterResults.values = itemList;
                    filterResults.count = itemList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results){
                itemList = (ArrayList<Item>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    /* Sorted List */

    private Comparator<Item> itemComparator;

    private final SortedList<Item> sortedList = new SortedList<>(Item.class, new SortedList.Callback<Item>(){
        @Override
        public void onInserted(int position, int count){
            notifyItemInserted(position);
        }

        @Override
        public void onRemoved(int position, int count){
            notifyItemRemoved(position);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition){
            notifyItemRemoved(fromPosition);
        }

        @Override
        public int compare(Item o1, Item o2){
            return itemComparator.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count){
            notifyItemChanged(position);
        }

        @Override
        public boolean areContentsTheSame(Item oldItem, Item newItem){
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Item item1, Item item2){
            return item1.getId() == item2.getId();
        }
    });

    public void add(Item item){
        sortedList.add(item);
    }

    public void remove(Item item){
        sortedList.remove(item);
    }

    public void add(List<Item> items){
        sortedList.addAll(items);
    }

    public void remove(List<Item> items){
        sortedList.beginBatchedUpdates();
        for(Item model : items){
            sortedList.remove(model);
        }
        sortedList.endBatchedUpdates();
    }

    public void replaceAll(List<Item> items){
        sortedList.beginBatchedUpdates();
        for(int i = sortedList.size() - 1; i >= 0; i--){
            final Item item = sortedList.get(i);
            if(!items.contains(item)){
                sortedList.remove(item);
            }
        }
        sortedList.addAll(items);
        sortedList.endBatchedUpdates();
    }
}

class ItemListElementWrapper{
    private Item item;
    private boolean isShowing;

    public ItemListElementWrapper(Item item){
        this.item = item;
        this.isShowing = false;
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

    public static void clearOlderShowFlags(List<ItemListElementWrapper> itemListElementWrappers, Item newlyFlaggedItem){
        for(ItemListElementWrapper itemListElementWrapper : itemListElementWrappers){
            if(newlyFlaggedItem != itemListElementWrapper.getItem()){
                itemListElementWrapper.setShowing(false);
            }
        }
    }
}