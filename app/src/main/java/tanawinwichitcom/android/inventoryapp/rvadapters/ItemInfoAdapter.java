package tanawinwichitcom.android.inventoryapp.rvadapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class ItemInfoAdapter extends RecyclerView.Adapter<ItemInfoAdapter.InfoViewHolder>{

    private HashMap<String, String> infoHashMap;
    private String infoNameArray[] = new String[]{"Item Name", "Item ID", "Quantity"
            , "Average Rating", "Total Number of Ratings", "Date Created"
            , "Date Modified", "Image Size", "Image Resolution"};
    private Context context;

    public ItemInfoAdapter(Context context){
        this.context = context;
        infoHashMap = new HashMap<>();
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_list_item, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position){
        String keyInfoName = infoNameArray[position];
        Locale locale = HelperUtility.getCurrentLocale(holder.infoTopicTextView.getContext());

        // System.out.println(keyInfoName);
        holder.infoTopicTextView.setText(keyInfoName);

        holder.ratingBar.setVisibility(View.GONE);

        switch(keyInfoName){
            case "Item Name":
            case "Item ID":
                holder.infoValueTextView.setText(infoHashMap.get(keyInfoName));
                break;
            case "Quantity":
                int quantityValue = Integer.valueOf(infoHashMap.get(keyInfoName));
                String number = NumberFormat.getNumberInstance(locale).format(quantityValue);
                holder.infoValueTextView.setText(number);
                break;
            case "Date Created":
            case "Date Modified":
                if(infoHashMap.get(keyInfoName) != null){
                    holder.infoValueTextView.setText(new Date(Long.valueOf(infoHashMap.get(keyInfoName))).toString());
                }else{
                    holder.infoValueTextView.setText("N/A");
                }
                break;
            case "Average Rating":
                holder.ratingBar.setMax(5);
                holder.ratingBar.setIsIndicator(true);
                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.ratingBar.setStepSize(0.5f);
                if(infoHashMap.containsKey(keyInfoName)){
                    float rating = Float.valueOf(infoHashMap.get(keyInfoName));
                    holder.ratingBar.setRating(rating);
                    holder.infoValueTextView.setText(infoHashMap.get(keyInfoName));
                }else{
                    holder.infoValueTextView.setText("0");
                }
                break;
            case "Total Number of Ratings":
                int totalRatingsInt = 0;
                if(infoHashMap.containsKey(keyInfoName)){
                    totalRatingsInt = Integer.valueOf(infoHashMap.get(keyInfoName));
                }
                holder.infoValueTextView.setText(NumberFormat.getNumberInstance(locale).format(totalRatingsInt));
                break;
            case "Image Resolution":
            case "Image Size":
                if(infoHashMap.get(keyInfoName) != null){
                    File imageFile = new File(infoHashMap.get(keyInfoName));
                    if(keyInfoName.contains("Size")){
                        long fileSizeBytes = imageFile.length();       /* Gets Image File size in bytes */
                        double fileSizeMB = fileSizeBytes / Math.pow(2, 20);    /* Converts Image File size to Megabytes*/
                        holder.infoValueTextView.setText(new StringBuilder()
                                .append(String.format(HelperUtility.getCurrentLocale(context), "%.2f", fileSizeMB))
                                .append(" MB").toString());
                    }else if(keyInfoName.contains("Resolution")){
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());      /* Gets Bitmap from file */
                        holder.infoValueTextView.setText((bitmap.getWidth() + " × " + bitmap.getHeight()));     /* Gets Image Resolution */
                    }
                }else{
                    holder.infoValueTextView.setText("N/A");
                }
                break;
        }

        // if(keyInfoName.contains("Date")){
        //     if(infoHashMap.get(keyInfoName) != null){
        //         holder.infoValueTextView.setText(new Date(Long.valueOf(infoHashMap.get(keyInfoName))).toString());
        //     }else{
        //         holder.infoValueTextView.setText("N/A");
        //     }
        // }else if(keyInfoName.equals("Average Rating")){
        //     holder.ratingBar.setStepSize(0.5f);
        //     holder.ratingBar.setMax(5);
        //     holder.ratingBar.setIsIndicator(true);
        //     holder.ratingBar.setVisibility(View.VISIBLE);
        //     if(infoHashMap.containsKey(keyInfoName)){
        //         holder.ratingBar.setRating(Float.valueOf(infoHashMap.get(keyInfoName)));
        //         holder.infoValueTextView.setText(infoHashMap.get(keyInfoName));
        //     }else{
        //         holder.infoValueTextView.setText("N/A");
        //     }
        // }else if(keyInfoName.equals("Total Number of Ratings")){
        //     if(infoHashMap.containsKey(keyInfoName)){
        //         int totalRatingsInt = Integer.valueOf(infoHashMap.get(keyInfoName));
        //         String number = NumberFormat.getNumberInstance(locale).format(totalRatingsInt);
        //         holder.infoValueTextView.setText(number);
        //     }else{
        //         holder.infoValueTextView.setText("0");
        //     }
        // }else if(keyInfoName.contains("Image")){
        //     if(infoHashMap.get(keyInfoName) != null){
        //         File imageFile = new File(infoHashMap.get(keyInfoName));
        //         if(keyInfoName.contains("Size")){
        //             long fileSizeBytes = imageFile.length();       /* Gets Image File size in bytes */
        //             double fileSizeMB = fileSizeBytes / Math.pow(2, 20);    /* Converts Image File size to Megabytes*/
        //             holder.infoValueTextView.setText(new StringBuilder()
        //                     .append(String.format(HelperUtility.getCurrentLocale(context), "%.2f", fileSizeMB))
        //                     .append(" MB").toString());
        //         }else if(keyInfoName.contains("Resolution")){
        //             Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath());      /* Gets Bitmap from file */
        //             holder.infoValueTextView.setText((bitmap.getWidth() + " × " + bitmap.getHeight()));     /* Gets Image Resolution */
        //         }
        //     }else{
        //         holder.infoValueTextView.setText("N/A");
        //     }
        // }else if(keyInfoName.equals("Quantity")){
        //     int quantityValue = Integer.valueOf(infoHashMap.get(keyInfoName));
        //     String number = NumberFormat.getNumberInstance(locale).format(quantityValue);
        //     holder.infoValueTextView.setText(number);
        // }else{
        //     holder.infoValueTextView.setText(infoHashMap.get(keyInfoName));
        // }


    }

    @Override
    public int getItemCount(){
        return infoNameArray[0].length();
    }

    public void applyInfoDataChanges(Item item){
        infoHashMap.clear();
        infoHashMap.put("Item Name", item.getName());
        infoHashMap.put("Item ID", String.valueOf(item.getId()));
        infoHashMap.put("Quantity", String.valueOf(item.getQuantity()));
        // infoHashMap.put("Total Number of Ratings", null);
        // infoHashMap.put("Average Rating", null);
        infoHashMap.put("Date Created", (item.getDateCreated() != null) ? String.valueOf(item.getDateCreated().getTime()) : null);
        infoHashMap.put("Date Modified", (item.getDateModified() != null) ? String.valueOf(item.getDateModified().getTime()) : null);
        infoHashMap.put("Image Size", (item.getImageFile() != null) ? item.getImageFile().getPath() : null);
        infoHashMap.put("Image Resolution", (item.getImageFile() != null) ? item.getImageFile().getPath() : null);
        notifyDataSetChanged();
    }

    public void applyReviewsChanges(String totalReview, double averageRating){
        System.out.println("Applying Reviews Changes " + averageRating + " (" + totalReview + ")");
        infoHashMap.put("Total Number of Ratings", totalReview);
        infoHashMap.put("Average Rating", String.format(HelperUtility.getCurrentLocale(context), "%.2f", averageRating));
        notifyDataSetChanged();
    }

    class InfoViewHolder extends RecyclerView.ViewHolder{

        TextView infoTopicTextView;
        TextView infoValueTextView;
        RatingBar ratingBar;

        InfoViewHolder(View itemView){
            super(itemView);
            infoTopicTextView = itemView.findViewById(R.id.infoTopicText);
            infoValueTextView = itemView.findViewById(R.id.infoValueText);
            ratingBar = itemView.findViewById(R.id.infoRatingBar);
        }
    }
}
