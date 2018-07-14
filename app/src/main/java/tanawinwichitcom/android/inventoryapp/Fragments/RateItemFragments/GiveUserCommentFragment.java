package tanawinwichitcom.android.inventoryapp.Fragments.RateItemFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import tanawinwichitcom.android.inventoryapp.GiveRatingPagerAdapter;
import tanawinwichitcom.android.inventoryapp.R;

@SuppressLint("ValidFragment")
public class GiveUserCommentFragment extends Fragment{

    private GiveRatingPagerAdapter giveRatingPagerAdapter;
    private EditText commentEditText;
    private TextInputLayout commentEditWrapper;
    private Button summitReviewButton;

    private CheckUserScoreListener checkUserScoreListener;
    private SummitButtonClickListener summitButtonClickListener;


    public GiveUserCommentFragment(){
    }

    @SuppressLint("ValidFragment")
    public GiveUserCommentFragment(GiveRatingPagerAdapter giveRatingPagerAdapter){
        this.giveRatingPagerAdapter = giveRatingPagerAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.item_give_comment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        commentEditText = view.findViewById(R.id.commentEditText);
        commentEditWrapper = view.findViewById(R.id.commentEditWrapper);
        summitReviewButton = view.findViewById(R.id.submitReviewButton);

        summitReviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(commentEditText.getText().toString().isEmpty()){
                    commentEditWrapper.setError("Type something here please");
                    return;
                }

                if(summitButtonClickListener != null && checkUserScoreListener != null){
                    summitButtonClickListener.onReviewSummitButtonClick(checkUserScoreListener.onCheckUserScore(), commentEditText.getText().toString());
                }
            }
        });
    }


    public String getComment(){
        return commentEditText.getText().toString();
    }

    public void setSummitButtonClickListener(SummitButtonClickListener summitButtonClickListener){
        this.summitButtonClickListener = summitButtonClickListener;
    }

    public void setCheckUserScoreListener(CheckUserScoreListener checkUserScoreListener){
        this.checkUserScoreListener = checkUserScoreListener;
    }

    public interface CheckUserScoreListener{
        public float onCheckUserScore();
    }

    public interface SummitButtonClickListener{
        public void onReviewSummitButtonClick(float rating, String comment);
    }
}
