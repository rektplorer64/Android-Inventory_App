package tanawinwichitcom.android.inventoryapp.RecycleViewAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.ViewHolder>{

    private List<Review> reviewArrayList;

    private SparseArray<User> userMap;
    private SparseArray<ArrayList<Review>> reviewMap;

    private int itemId;

    public UserReviewAdapter(List<Review> reviewArrayList, int itemId){
        this.reviewArrayList = reviewArrayList;
        userMap = new SparseArray<>();
        reviewMap = ItemViewModel.convertReviewListToSparseArray(reviewArrayList);
        this.itemId = itemId;
    }

    @NonNull
    @Override
    public UserReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reviews, parent, false);
        return new UserReviewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewAdapter.ViewHolder holder, int position){
        Review review = reviewMap.get(itemId).get(position);
        User user = userMap.get(review.getUserId());

        if(user != null){

            if(position == reviewArrayList.size() - 1){
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
                int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, holder.cardView.getContext().getResources().getDisplayMetrics());
                params.setMargins(pxMargin, pxMargin, pxMargin, pxMargin);
                holder.cardView.setLayoutParams(params);
            }

            holder.userNameTextView.setText(user.getName() + " " + user.getSurname() + " (" + user.getUsername() + ")");
            holder.userScoreTextView.setText(String.format("%.1f", review.getRating()));
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

    public void applyUserDataChange(List<User> userList){
        for(User u : userList){
            userMap.put(u.getId(), u);
        }
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView userNameTextView, userScoreTextView, userReviewCommentTextView;
        RatingBar scoreRatingBar;

        ViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.reviewCardView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            scoreRatingBar = itemView.findViewById(R.id.scoreRatingBar);
            userScoreTextView = itemView.findViewById(R.id.userScoreTextView);
            userReviewCommentTextView = itemView.findViewById(R.id.userReviewCommentTextView);
        }
    }
}
