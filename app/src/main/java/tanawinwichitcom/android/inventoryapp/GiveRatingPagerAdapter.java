package tanawinwichitcom.android.inventoryapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tanawinwichitcom.android.inventoryapp.Fragments.RateItemFragments.GiveScoreFragment;
import tanawinwichitcom.android.inventoryapp.Fragments.RateItemFragments.GiveUserCommentFragment;

public class GiveRatingPagerAdapter extends FragmentPagerAdapter{

    private Fragment giveScoreFragment;
    private Fragment giveUserCommentFragment;

    public GiveRatingPagerAdapter(FragmentManager fm){
        super(fm);
        giveScoreFragment = new GiveScoreFragment(this);
        giveUserCommentFragment = new GiveUserCommentFragment(this);
        // ((GiveUserCommentFragment) giveUserCommentFragment).configureViews();
    }

    @Override
    public Fragment getItem(int position){
        Fragment f;
        if(position == 0){
            f = giveScoreFragment;
        }else{
            f = giveUserCommentFragment;
        }
        return f;
    }

    @Override
    public int getCount(){
        return 2;
    }

    public GiveScoreFragment getGiveScoreFrag(){
        return ((GiveScoreFragment) giveScoreFragment);
    }

    public GiveUserCommentFragment getUserCommentFrag(){
        return ((GiveUserCommentFragment) giveUserCommentFragment);
    }

}
