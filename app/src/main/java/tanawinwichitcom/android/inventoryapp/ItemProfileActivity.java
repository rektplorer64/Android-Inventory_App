package tanawinwichitcom.android.inventoryapp;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.DialogFragments.AllReviewDialogFragment;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

import static tanawinwichitcom.android.inventoryapp.DialogFragments.ColorSelectorDialogFragment.darkenColor;

public class ItemProfileActivity extends AppCompatActivity{

    private ItemViewModel itemViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_item);

        Bundle bundle = getIntent().getExtras();
        final int itemId = bundle.getInt("itemId");

        // Gets the Window in order to change Status Bar's Color
        final Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* Sets the weight sum of the Rating LinearLayout to 100 */
        LinearLayout ratingRatioGroup = findViewById(R.id.ratingRatioGroup);
        ratingRatioGroup.setWeightSum(100f);

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> items){
                Item item = items.get(itemId - 1);
                window.setStatusBarColor(darkenColor(item.getItemColorAccent()));

                int backgroundColor = item.getItemColorAccent();
                int frontColor = (Color.red(backgroundColor) + Color.green(backgroundColor) + Color.blue(backgroundColor) >= 383) ? Color.BLACK : Color.WHITE;
                // Changes Toolbar's color according to the selected color
                //toolbar.setBackgroundColor(backgroundColor);
                toolbar.setTitleTextColor(frontColor);

                CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
                collapsingToolbarLayout.setContentScrimColor(backgroundColor);

                if(item.getImageFile() != null){
                    ImageView itemImageView = findViewById(R.id.itemImageView);
                    System.out.println("item.getImageFile(): " + item.getImageFile());
                    System.out.println("item.getImageFile().getPath(): " + item.getImageFile().getPath());
                    Glide.with(getApplicationContext()).load(item.getImageFile()).into(itemImageView);
                }

                // Changes Navigation Icon (Back Arrow Icon)'s color
                toolbar.getNavigationIcon().setTint(frontColor);

                // Changes Status bar's color according to the selected color
                window.setStatusBarColor(darkenColor(backgroundColor));

                TextView itemNameTextView = findViewById(R.id.itemTextView);
                itemNameTextView.setText(item.getName());

                TextView quantityTextView = findViewById(R.id.quantityTextView);
                quantityTextView.setText("QTY " + item.getQuantity());

                RatingBar ratingBar = findViewById(R.id.ratingBarView);
                if(item.getRating() != null){
                    ratingBar.setRating(Float.valueOf(String.valueOf(item.getRating())));
                }else{
                    ratingBar.setRating((float) 0.0);
                }

                TextView descriptionTextView = findViewById(R.id.descriptionTextView);
                descriptionTextView.setText(item.getDescription());

                FloatingActionButton fab = findViewById(R.id.profileFab);
                fab.setBackgroundTintList(ColorStateList.valueOf((backgroundColor)));
                Drawable fabIcon = getResources().getDrawable(R.drawable.ic_star_black_24dp, null).getConstantState().newDrawable();
                fabIcon.mutate().setColorFilter(frontColor, PorterDuff.Mode.SRC_ATOP);
                fab.setImageDrawable(fabIcon);

            }
        });
        itemViewModel.getAllReviews().observe(this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviewList){

                // Updates the rating cardView below
                RatingBar ratingBar1 = findViewById(R.id.ratingBarView2);
                TextView scoreTextView = findViewById(R.id.scoreTextView);
                TextView totalReviewTextView = findViewById(R.id.totalReviewTextView);
                TextView ratingTextView = findViewById(R.id.ratingTextView);
                ArrayList<Float> calculateWeight;
                if(reviewList != null){
                    SparseArray<ArrayList<Review>> reviewMap = ItemViewModel.convertReviewListToSparseArray(reviewList);
                    ArrayList<Review> reviewsOfCurrentItem = reviewMap.get(itemId);
                    String totalReviews = NumberFormat.getNumberInstance(Locale.US).format((reviewsOfCurrentItem == null) ? 0 : reviewsOfCurrentItem.size());

                    totalReviewTextView.setText(totalReviews);
                    Double calculatedAverage = Review.calculateAverage(reviewsOfCurrentItem);
                    ratingTextView.setText(String.format("%.1f", calculatedAverage) + " (" + totalReviews + ")");
                    scoreTextView.setText(String.format("%.1f", calculatedAverage));
                    ratingBar1.setRating(Float.valueOf(String.valueOf(calculatedAverage)));

                    calculateWeight = calculateScalePercentage(reviewsOfCurrentItem);
                    // Codes for Score bar ratio
                    ArrayList<View> scoreBarRatioViewList = new ArrayList<>();
                    scoreBarRatioViewList.add(findViewById(R.id.oneStarRec));
                    scoreBarRatioViewList.add(findViewById(R.id.twoStarRec));
                    scoreBarRatioViewList.add(findViewById(R.id.threeStarRec));
                    scoreBarRatioViewList.add(findViewById(R.id.fourStarRec));
                    scoreBarRatioViewList.add(findViewById(R.id.fiveStarRec));

                    int count = 0;
                    for(View ratioBar : scoreBarRatioViewList){
                        ratioBar.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, calculateWeight.get(count++)));
                    }
                }else{
                    totalReviewTextView.setText("0");
                    ratingTextView.setText("0 (0)");
                    ratingBar1.setRating((float) 0.0);
                }

                // Setups the showAllReviewButton
                HashMap<Integer, ArrayList<Review>> integerArrayListHashMap = ItemViewModel.convertReviewListToHashMap(reviewList);
                Button showAllReviewsButton = findViewById(R.id.showAllReviewsButton);
                if(!(integerArrayListHashMap.get(itemId) == null)){
                    showAllReviewsButton.setEnabled(true);
                    showAllReviewsButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            AllReviewDialogFragment allReviewDialogFragment = new AllReviewDialogFragment();

                            Bundle bundle = new Bundle();
                            bundle.putInt("itemId", itemId);
                            allReviewDialogFragment.setArguments(bundle);
                            allReviewDialogFragment.show(getSupportFragmentManager(), "Oh shit");
                        }
                    });
                }else{
                    showAllReviewsButton.setText("No reviews available");
                    showAllReviewsButton.setEnabled(false);
                }
            }
        });
    }

    /**
     * For each star (from 1 to 5), find the number of occurrences, then calculate the percentages
     * out of them. Then multiply with 100 to get the int number and store them into an Array
     *
     * @param reviewArrayList list object of reviews
     *
     * @return the percentage numbers sorted from index 0 = 1 star, index 1 = 2 stars, and so on...
     */
    private ArrayList<Float> calculateScalePercentage(List<Review> reviewArrayList){
        ArrayList<Float> result = new ArrayList<>();

        /* If the review list is null, return empty list */
        if(reviewArrayList == null){
            for(int i = 0; i < 5; i++){
                result.add(0f);
            }
            return result;
        }

        System.out.println("Array size: " + reviewArrayList.size());
        int fiveStar = 0, fourStar = 0, threeStar = 0, twoStar = 0, oneStar = 0;
        int count = 0;
        for(Review review : reviewArrayList){
            double reviewScore = review.getRating();
            double roundedScore = Math.round(reviewScore);
            System.out.println("L" + count++ + ": " + fiveStar + ", " + fourStar + ", "
                    + threeStar + ", " + twoStar + ", " + oneStar);
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


        System.out.println((float) oneStar / reviewArrayList.size());

        return result;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_edit:{
                Toast.makeText(this, "Clicked edit button...", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_item_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
