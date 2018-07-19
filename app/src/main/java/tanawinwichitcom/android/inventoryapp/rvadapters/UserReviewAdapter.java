package tanawinwichitcom.android.inventoryapp.rvadapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.User;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class UserReviewAdapter extends RecyclerView.Adapter<UserReviewAdapter.ViewHolder>{

    private List<Review> reviewArrayList;

    private SparseArray<User> userMap;

    private int itemId;
    private Context context;

    public UserReviewAdapter(int itemId, Context context){
        this.context = context;
        this.itemId = itemId;
        userMap = new SparseArray<>();
        reviewArrayList = new ArrayList<>();
    }

    @NonNull
    @Override
    public UserReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reviews, parent, false);
        return new UserReviewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UserReviewAdapter.ViewHolder holder, int position){
        Review review = reviewArrayList.get(position);
        User user = userMap.get(review.getUserId());

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", HelperUtility.getCurrentLocale(holder.cardView.getContext()));
        if(user != null){
            // if(position == reviewArrayList.size() - 1){
            //     ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.cardView.getLayoutParams();
            //     int pxMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, holder.cardView.getContext().getResources().getDisplayMetrics());
            //     params.setMargins(pxMargin, pxMargin, pxMargin, pxMargin);
            //     holder.cardView.setLayoutParams(params);
            // }
            holder.userNameTextView.setText(new StringBuilder().append(user.getName()).append(" ").append(user.getSurname()).append(" (").append(user.getUsername()).append(")").toString());

            holder.userReviewDateTextView.setText(dateFormat.format(review.getTimeStamp()));
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
        TextView userNameTextView, userReviewDateTextView, userReviewCommentTextView;
        RatingBar scoreRatingBar;

        ViewHolder(View itemView){
            super(itemView);
            cardView = itemView.findViewById(R.id.reviewCardView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            scoreRatingBar = itemView.findViewById(R.id.scoreRatingBar);
            userReviewDateTextView = itemView.findViewById(R.id.userReviewDateTextView);
            userReviewCommentTextView = itemView.findViewById(R.id.userReviewCommentTextView);
        }
    }

}
