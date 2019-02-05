package io.rektplorer.inventoryapp.utility;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;

public class UserInterfaceUtility{
    /**
     * For each star (from 1 to 5), find the number of occurrences, then calculate the percentages
     * out of them. Then multiply with 100 to get the int number and store them into an Array
     *
     * @param reviewArrayList list object of reviews
     *
     * @return the percentage numbers sorted from index 0 = 1 star, index 1 = 2 stars, and so on...
     */
    public static ArrayList<Float> calculateScalePercentage(List<Review> reviewArrayList){
        ArrayList<Float> result = new ArrayList<>();

        /* If the review list is null, return empty list */
        if(reviewArrayList == null){
            for(int i = 0; i < 5; i++){
                result.add(0f);
            }
            return result;
        }

        // System.out.println("Array size: " + reviewArrayList.size());
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        // int count = 0;
        for(Review review : reviewArrayList){
            double reviewScore = review.getRating();
            double roundedScore = Math.round(reviewScore);
            // System.out.println("L" + count++ + ": " + fiveStar + ", " + fourStar + ", "
            //         + threeStar + ", " + twoStar + ", " + oneStar);
            if(roundedScore == 5.0){
                fiveStar++;
            }else if(roundedScore == 4.0){
                fourStar++;
            }else if(roundedScore == 3.0){
                threeStar++;
            }else if(roundedScore == 2.0){
                twoStar++;
            }else{
                oneStar++;
            }
        }

        // For One Stars
        result.add((float) oneStar / reviewArrayList.size() * 100);

        // For Two Stars
        result.add((float) twoStar / reviewArrayList.size() * 100);

        // For Three Stars
        result.add((float) threeStar / reviewArrayList.size() * 100);

        // For Four Stars
        result.add((float) fourStar / reviewArrayList.size() * 100);

        // For Five Stars
        result.add((float) fiveStar / reviewArrayList.size() * 100);

        //System.out.println((float) oneStar / reviewArrayList.size());

        return result;
    }

    public static int getStatusBarHeight(Context context){
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void expandActionBarToFitStatusBar(Toolbar toolbar, Context context){
        // README: android:minHeight="?android:actionBarSize" is required in order to align menu icons
        // center vertically inside action bar

        // Sets the action bar's content padding, offset equal to height of the status bar
        toolbar.setPadding(0, getStatusBarHeight(context), 0, 0);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            toolbar.setForegroundGravity(Gravity.CENTER_VERTICAL);
        }
        // Gets action bar's height (ActionBar.getHeight() is useless)
        TypedValue tv = new TypedValue();
        int toolBarHeight = 0;
        if(context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            toolBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }

        // Sets action bar a new height (Height of Status bar + origin Height of Action bar)
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        layoutParams.height = getStatusBarHeight(context) + toolBarHeight;
        toolbar.setLayoutParams(layoutParams);
    }
}
