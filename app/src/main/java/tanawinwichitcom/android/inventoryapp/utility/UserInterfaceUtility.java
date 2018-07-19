package tanawinwichitcom.android.inventoryapp.utility;

import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;

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
}
