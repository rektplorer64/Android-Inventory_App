package io.rektplorer.inventoryapp.rvadapters;


import android.content.Context;
import android.text.format.DateUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.Entities.User;
import io.rektplorer.inventoryapp.utility.ScreenUtility;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.ViewHolder>{

    private List<Review> reviewArrayList;

    private SparseArray<User> userMap;

    private int itemId;

    public UserReviewAdapter(int itemId){
        this.itemId = itemId;
        userMap = new SparseArray<>();
        reviewArrayList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                                      .inflate(R.layout.card_reviews, parent, false);
        return new UserReviewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewAdapter.ViewHolder holder, int position){
        Review review = reviewArrayList.get(position);
        User user = userMap.get(review.getUserId());

        Context context = holder.cardView.getContext();

        // DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", ScreenUtility
        //         .getCurrentLocale(holder.cardView.getContext()));
        if(user != null){
            holder.realNameTextView.setText(
                    new StringBuilder().append(user.getName()).append(" ")
                                       .append(user.getSurname()));
            holder.userNameTextView.setText(user.getUsername());

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format(
                    ScreenUtility.getCurrentLocale(context), "%.1f",
                    review.getRating())).append(" · ")
                         .append(DateUtils.getRelativeTimeSpanString(
                                 review.getTimeStamp().getTime(),
                                 Calendar.getInstance().getTimeInMillis(),
                                 DateUtils.MINUTE_IN_MILLIS));
            holder.userReviewDateTextView.setText(stringBuilder.toString());
            // holder.userReviewDateTextView.setText(dateFormat.format(review.getTimeStamp()));
            holder.userReviewCommentTextView.setText(review.getComment());
            holder.scoreRatingBar.setRating((float) review.getRating());
            holder.scoreRatingBar.setStepSize(0.5f);
        }
    }

    @Override
    public int getItemCount(){
        if(reviewArrayList != null){
            return reviewArrayList.size();
        }else{
            return 0;
        }
    }

    public void applyUserDataChanges(List<User> userList){
        for(User u : userList){
            userMap.put(u.getId(), u);
        }
        notifyDataSetChanged();
    }

    public void applyReviewDataChanges(List<Review> reviewList){
        if(reviewList != null){
            reviewArrayList.clear();
            for(Review review : reviewList){
                if(itemId == review.getItemId()){
                    reviewArrayList.add(review);
                }
            }
            notifyDataSetChanged();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView userNameTextView, userReviewDateTextView, userReviewCommentTextView, realNameTextView;
        RatingBar scoreRatingBar;

        ViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.reviewCardView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            realNameTextView = itemView.findViewById(R.id.realNameTextView);
            scoreRatingBar = itemView.findViewById(R.id.scoreRatingBar);
            userReviewDateTextView = itemView.findViewById(R.id.userReviewDateTextView);
            userReviewCommentTextView = itemView.findViewById(R.id.userReviewCommentTextView);
        }
    }

}
