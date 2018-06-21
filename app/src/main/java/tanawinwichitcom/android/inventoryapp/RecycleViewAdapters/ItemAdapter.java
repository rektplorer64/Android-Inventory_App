package tanawinwichitcom.android.inventoryapp.RecycleViewAdapters;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.ItemProfileActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>{

    private List<Item> itemArrayList;

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_CARD_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;

    /*
     * Private Field for basically a HashMap, but with a better memory efficiency
     * Binds like HashMap<Integer (itemId), Review (Review Class)>
     */
    private SparseArray<ArrayList<Review>> reviewHashMap;

    public ItemAdapter(){
        this.itemArrayList = new ArrayList<>();
        this.reviewHashMap = new SparseArray<>();
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new ItemAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder holder, final int position){
        final Item item = itemArrayList.get(position);
        final ArrayList<Review> reviewArrayList = reviewHashMap.get(item.getId());

        double averageRating = Review.calculateAverage(reviewArrayList);
        item.setRating(averageRating);
        int numberOfReviews = (reviewArrayList == null) ? 0 : reviewArrayList.size();

        if(position == 0){
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, holder.cardView.getContext().getResources().getDisplayMetrics());
            int pxMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, holder.cardView.getContext().getResources().getDisplayMetrics());
            params.setMargins(pxMargin, pxMargin, pxMargin, pxMargin2);
            holder.cardView.setLayoutParams(params);
        }else if(position == itemArrayList.size() - 1){
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, holder.cardView.getContext().getResources().getDisplayMetrics());
            int pxMargin2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, holder.cardView.getContext().getResources().getDisplayMetrics());
            params.setMargins(pxMargin, pxMargin2, pxMargin, pxMargin);
            holder.cardView.setLayoutParams(params);
        }

        if(item.getImageFile() != null){
            Glide.with(holder.imageView.getContext()).load(item.getImageFile()).into(holder.imageView);
        }

        holder.nameTextView.setText(item.getName());
        holder.ratingTextView.setText(String.format("%.1f", item.getRating()) + " (" + NumberFormat.getNumberInstance(Locale.US).format(numberOfReviews) + ")");
        holder.quantityTextView.setText("QTY " + item.getQuantity());
        holder.descriptionTextView.setText(item.getDescription());
        if(item.getRating() != null){
            holder.ratingBar.setRating(Float.valueOf(String.valueOf(item.getRating())));
        }else{
            holder.ratingBar.setRating((float) 0.0);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(v.getContext(), "Item #" + position + " is clicked...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), ItemProfileActivity.class);

                intent.putExtra("itemId", item.getId());

                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        if(itemArrayList != null){
            return itemArrayList.size();
        }else{
            return 0;
        }
    }

    public void applyItemDataChanges(List<Item> itemArrayList){
        this.itemArrayList = itemArrayList;
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

        ViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.itemCardView);
            nameTextView = itemView.findViewById(R.id.itemTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            descriptionTextView = itemView.findViewById(R.id.shortDescriptionTextView);
            ratingBar = itemView.findViewById(R.id.ratingBarView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
