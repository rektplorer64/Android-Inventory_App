package io.rektplorer.inventoryapp.rvadapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.utility.ScreenUtility;
import io.rektplorer.inventoryapp.utility.UserInterfaceUtility;

public class DetailedScoreAdapter extends RecyclerView.Adapter<DetailedScoreAdapter.ViewHolder>{

    private int starNumberList[] = new int[]{1, 2, 3, 4, 5};
    private String colorStrings[] = new String[]{"#28cc13", "#20b90c", "#1eac0c", "#1b9c0a",
                                                 "#178908"};
    private List<Float> scalePercentage;
    private int totalReview;

    public DetailedScoreAdapter(){
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.score_list_item, parent, false);
        return new DetailedScoreAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        int starNumber = starNumberList[position];
        float percentage = scalePercentage.get(position);

        int totalUser = (int) (totalReview * percentage / 100);

        Context context = holder.detailedScoreTextView.getContext();

        holder.starNumberTextView.setText(String.valueOf(starNumber));
        holder.detailedScoreTextView.setText(new StringBuilder().append(String.format(
                ScreenUtility.getCurrentLocale(context), "%.2f", percentage)).append(" % (").append(totalUser).append(")").toString());
        holder.scoreRatioBarCardView.setLayoutParams(new LinearLayout.LayoutParams(0
                , ScreenUtility.dpToPx(30, context), percentage));

        holder.scoreRatioBarView.setBackgroundColor(Color.parseColor(colorStrings[position]));
    }

    @Override
    public int getItemCount(){
        return scalePercentage.size();
    }

    public void applyReviewsDataChanges(List<Review> reviewList){
        totalReview = reviewList.size();
        this.scalePercentage = UserInterfaceUtility.calculateScalePercentage(reviewList);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView starNumberTextView;
        TextView detailedScoreTextView;
        CardView scoreRatioBarCardView;
        View scoreRatioBarView;

        ViewHolder(View itemView){
            super(itemView);
            starNumberTextView = itemView.findViewById(R.id.starNumberTextView);
            detailedScoreTextView = itemView.findViewById(R.id.detailedScoreTextView);
            scoreRatioBarCardView = itemView.findViewById(R.id.scoreRatioBarCardView);
            scoreRatioBarView = itemView.findViewById(R.id.scoreRatioBarView);
        }
    }
}
