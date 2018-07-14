package tanawinwichitcom.android.inventoryapp.Fragments.RateItemFragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;
import tanawinwichitcom.android.inventoryapp.GiveRatingPagerAdapter;
import tanawinwichitcom.android.inventoryapp.R;

@SuppressLint("ValidFragment")
public class GiveScoreFragment extends Fragment{

    private GiveRatingPagerAdapter giveRatingPagerAdapter;
    private MaterialRatingBar ratingBar;

    public GiveScoreFragment(){
    }

    @SuppressLint("ValidFragment")
    public GiveScoreFragment(GiveRatingPagerAdapter giveRatingPagerAdapter){
        this.giveRatingPagerAdapter = giveRatingPagerAdapter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.item_give_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        ratingBar = view.findViewById(R.id.giveScoreRatingBar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){
            @Override
            public void onRatingChanged(final RatingBar ratingBar, final float rating, boolean fromUser){
                if(rating < 0.5){
                    ratingBar.setRating(0.5f);
                }
                if(giveRatingPagerAdapter != null){
                    giveRatingPagerAdapter.getUserCommentFrag().setCheckUserScoreListener(new GiveUserCommentFragment.CheckUserScoreListener(){
                        @Override
                        public float onCheckUserScore(){
                            return ratingBar.getRating();
                        }
                    });
                }
            }
        });
    }

    public double getRating(){
        return ratingBar.getRating();
    }
}
