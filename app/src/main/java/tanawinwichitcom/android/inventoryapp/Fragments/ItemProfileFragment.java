package tanawinwichitcom.android.inventoryapp.Fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.KeyEvent;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tanawinwichitcom.android.inventoryapp.AddItemActivity;
import tanawinwichitcom.android.inventoryapp.AutoHeightViewPager;
import tanawinwichitcom.android.inventoryapp.ColorUtility;
import tanawinwichitcom.android.inventoryapp.Fragments.RateItemFragments.GiveUserCommentFragment;
import tanawinwichitcom.android.inventoryapp.GiveRatingPagerAdapter;
import tanawinwichitcom.android.inventoryapp.HelperUtilities;
import tanawinwichitcom.android.inventoryapp.ItemProfileContainerActivity;
import tanawinwichitcom.android.inventoryapp.MainActivity;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RecyclerViewAdapters.ItemInfoAdapter;
import tanawinwichitcom.android.inventoryapp.RecyclerViewAdapters.UserReviewAdapter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Item;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.User;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.ItemViewModel;

import static tanawinwichitcom.android.inventoryapp.ColorUtility.darkenColor;

public class ItemProfileFragment extends CircularRevealFragment{

    private ItemDeleteListener itemDeleteListener;

    private Window window;
    private NestedScrollView nestedScrollView;
    private AppBarLayout appBarLayout;
    private LinearLayout itemProfileLinearLayout;
    private ItemViewModel itemViewModel;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView itemImageView, moreInfoIcon;
    private TextView itemNameTextView, quantityTextView, descriptionTextView;
    //private RatingBar ratingBar;

    // Views of the Rating Card
    private RatingBar ratingBar1;
    private TextView scoreTextView, totalReviewTextView, ratingTextView;
    private Button showAllReviewsButton;
    private ArrayList<View> scoreBarRatioViewList;

    private TabLayout tabLayout;

    private ItemStatusListener itemStatusListener;
    private ItemInfoAdapter itemInfoAdapter;
    private UserReviewAdapter userReviewAdapter;


    private int itemId = 1;
    private AutoHeightViewPager giveRatingPager;


    public ItemProfileFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static ItemProfileFragment newInstance(int fragmentLayoutRes, int itemId, int centerX, int centerY){
        Bundle args = new Bundle();
        args.putInt("resLayout", fragmentLayoutRes);
        args.putInt("itemId", itemId);
        args.putInt("cx", centerX);
        args.putInt("cy", centerY);
        // args.putInt("color", color);

        ItemProfileFragment fragment = new ItemProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState){
        itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);

        final Bundle bundle = getArguments();

        itemId = itemViewModel.getMinItemId();
        System.out.println("ItemProfileFragment, itemId = " + itemId);
        if(bundle != null){
            itemId = bundle.getInt("itemId");
            System.out.println("bundle != null; " + itemId);
        }

        if(bundle.getBoolean("isAfterDeletion")){
            itemId = itemViewModel.getMinItemId();
            System.out.println("isAfterDeletion; " + itemId);
        }

        final int finalItemId = itemId;
        initializeViews(view);

        itemInfoAdapter = new ItemInfoAdapter(getContext());
        itemInfoAdapter.setHasStableIds(true);

        userReviewAdapter = new UserReviewAdapter(finalItemId, getContext());
        userReviewAdapter.setHasStableIds(true);

        final ItemProfileFragment ogFragment = this;

        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(@Nullable List<Item> itemList){
                if(itemList == null || itemList.isEmpty()){
                    if(itemStatusListener != null){
                        itemStatusListener.onItemListEmpty();
                    }
                }else{
                    if(itemStatusListener != null){
                        itemStatusListener.onItemListNotEmpty();
                    }
                }
            }
        });
        itemViewModel.getItemById(itemId).observe(this, new Observer<Item>(){
            @Override
            public void onChanged(@Nullable final Item item){
                toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener(){
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem){
                        switch(menuItem.getItemId()){
                            case R.id.action_edit:{
                                Intent intent = new Intent(getActivity(), AddItemActivity.class);
                                intent.putExtra("itemId", itemId);
                                startActivity(intent);
                                break;
                            }
                            case R.id.action_delete:{
                                new MaterialDialog.Builder(getContext()).title("Do you want to delete the item?")
                                        .negativeText("No").positiveText("Yes")
                                        .onPositive(new MaterialDialog.SingleButtonCallback(){
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                                                itemViewModel.delete(item);
                                                if(getActivity() instanceof ItemProfileContainerActivity){
                                                    closeActivityCircularly();
                                                }else if(getActivity() instanceof MainActivity){
                                                    bundle.putBoolean("isAfterDeletion", true);
                                                    getFragmentManager()
                                                            .beginTransaction()
                                                            .detach(ogFragment)
                                                            .attach(ogFragment)
                                                            .commitAllowingStateLoss();
                                                    if(itemDeleteListener != null){
                                                        itemDeleteListener.onDelete();
                                                    }
                                                }
                                            }
                                        }).show();
                            }
                        }
                        return true;
                    }
                });
                onItemDataChanged(item, view);
            }
        });

        itemViewModel.getReviewsByItemId(itemId).observe(this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviewList){
                onReviewsDataChanged(reviewList, itemId);
            }
        });

        itemViewModel.getAllUsers().observe(this, new Observer<List<User>>(){
            @Override
            public void onChanged(@Nullable List<User> users){
                userReviewAdapter.applyUserDataChanges(users);
            }
        });

    }

    private void setUpStatusAndToolbar(@ColorInt int backColorInt, @ColorInt int frontColorInt, final View rootView){
        // Changes Toolbar's color according to the selected color
        //toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitleTextColor(frontColorInt);

        itemImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                appBarLayout.setExpanded(false);
            }
        });

        if(getActivity() instanceof ItemProfileContainerActivity){
            collapsingToolbarLayout.setContentScrimColor(backColorInt);
            // Changes Navigation Icon (Back Arrow Icon)'s color
            toolbar.getNavigationIcon().setTint(frontColorInt);
            toolbar.setNavigationOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    // getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                    if(getActivity() instanceof ItemProfileContainerActivity){
                        closeActivityCircularly();
                    }
                }
            });

            // If this fragment is launched in ItemProfileContainerActivity and the screen size at least LARGE
            if(HelperUtilities.getScreenSizeCategory(getContext()) >= HelperUtilities.SCREENSIZE_LARGE){
                int padding = HelperUtilities.dpToPx(100, getContext());
                itemProfileLinearLayout.setPadding(padding, 0, padding, 0);
                // itemImageView.getLayoutParams().height = HelperUtilities.dpToPx(1000, getContext());
                // itemImageView.requestLayout();
            }

            // Changes Status bar's color according to the selected color
            window.setStatusBarColor(darkenColor(backColorInt));
            window.setNavigationBarColor(backColorInt);
        }else{
            HelperUtilities.expandActionBarToFitStatusBar(toolbar, getContext());
            collapsingToolbarLayout.setContentScrimColor(darkenColor(backColorInt));
            /* Adjusts Layout According to the screen size */
            rootView.post(new Runnable(){
                @Override
                public void run(){
                    System.out.println("ItemProfile RootView's Width: " + rootView.getWidth());

                    // If the app takes the entire screen
                    if(rootView.getWidth() > 838){
                        int padding = HelperUtilities.dpToPx(75, rootView.getContext());
                        itemProfileLinearLayout.setPadding(padding, 0, padding, 0);
                    }
                }
            });
        }
    }

    private void closeActivityCircularly(){
        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void initializeViews(View view){
        if(getActivity() instanceof ItemProfileContainerActivity){
            // Gets the Window in order to change Status Bar's Color
            window = getActivity().getWindow();
            // // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        toolbar = view.findViewById(R.id.fragmentToolbar);
        if(getActivity() instanceof ItemProfileContainerActivity){
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }else{
            // toolbar.setVisibility(View.GONE);
            toolbar.inflateMenu(R.menu.menu_item_profile);
        }

        /* Sets the weight sum of the Rating LinearLayout to 100 */
        LinearLayout ratingRatioGroup = view.findViewById(R.id.ratingRatioGroup);
        ratingRatioGroup.setWeightSum(100f);

        appBarLayout = view.findViewById(R.id.appBarLayout);
        itemProfileLinearLayout = view.findViewById(R.id.itemProfileLinearLayout);
        collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbar);
        itemImageView = view.findViewById(R.id.itemImageView);
        moreInfoIcon = view.findViewById(R.id.moreInfoIcon);
        itemNameTextView = view.findViewById(R.id.itemTextView);
        quantityTextView = view.findViewById(R.id.quantityTextView);
        //ratingBar = view.findViewById(R.id.ratingBarView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        //fab = view.findViewById(R.id.profileFab);

        /* Setup Views of the lower card */
        giveRatingPager = view.findViewById(R.id.giveRatingPager);
        ratingBar1 = view.findViewById(R.id.ratingBarView2);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        totalReviewTextView = view.findViewById(R.id.totalReviewTextView);
        ratingTextView = view.findViewById(R.id.ratingTextView);
        showAllReviewsButton = view.findViewById(R.id.showAllReviewsButton);
        tabLayout = view.findViewById(R.id.tabLayout);
        setupFragmentViewPager();

        /* Setup Views of Rating Ratio Indicator */
        scoreBarRatioViewList = new ArrayList<>();
        scoreBarRatioViewList.add(view.findViewById(R.id.oneStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.twoStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.threeStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.fourStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.fiveStarRec));
    }

    private void setupFragmentViewPager(){
        final GiveRatingPagerAdapter ratingPagerAdapter = new GiveRatingPagerAdapter(getChildFragmentManager());
        ratingPagerAdapter.getUserCommentFrag().setSummitButtonClickListener(new GiveUserCommentFragment.SummitButtonClickListener(){
            @Override
            public void onReviewSummitButtonClick(float rating, String comment){
                // TODO: Handle comment
                Toast.makeText(getContext(), rating + " " + ratingPagerAdapter.getUserCommentFrag().getComment(), Toast.LENGTH_SHORT).show();
            }
        });
        //giveRatingPager.setOffscreenPageLimit(1);
        giveRatingPager.setAdapter(ratingPagerAdapter);
        tabLayout.setupWithViewPager(giveRatingPager, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_edit:{
                Toast.makeText(getContext(), "Clicked edit button...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), AddItemActivity.class);
                intent.putExtra("itemId", getActivity().getIntent().getExtras().getInt("itemId"));
                startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setItemStatusListener(ItemStatusListener itemStatusListener){
        this.itemStatusListener = itemStatusListener;
    }

    private void onItemDataChanged(@Nullable Item item, View rootView){

        if(item == null){
            // System.out.println("onItemDataChanged(): " + item.getId());
            return;
        }

        if(itemStatusListener != null){
            itemStatusListener.onItemBinding();
        }

        //window.setStatusBarColor(darkenColor(item.getItemColorAccent()));
        int backgroundColor = item.getItemColorAccent();
        int frontColor = ColorUtility.getSuitableFrontColor(getContext(), backgroundColor, true);
        setUpStatusAndToolbar(backgroundColor, frontColor, rootView);

        if(item.getImageFile() != null){
            Glide.with(getContext())
                    .load(item.getImageFile())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemImageView);
        }else{
            Glide.with(getContext())
                    .load(R.drawable.md_wallpaper_placeholder)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .thumbnail(0.001f)
                    .into(itemImageView);
        }

        itemNameTextView.setText(item.getName());
        quantityTextView.setText(new StringBuilder().append("QTY ")
                .append(NumberFormat.getNumberInstance(HelperUtilities.getCurrentLocale(getContext())).format(item.getQuantity()))
                .toString());

        itemInfoAdapter.applyInfoDataChanges(item);
        moreInfoIcon.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Toast.makeText(getContext(), "ImageButton Clicked", Toast.LENGTH_SHORT).show();
                new MaterialDialog.Builder(getContext()).title("Info")
                        .adapter(itemInfoAdapter, new LinearLayoutManager(getContext()))
                        .negativeText("Close")
                        .build()
                        .show();
            }
        });

        // if(item.getComment() != null){
        //     ratingBar.setRating(Float.valueOf(String.valueOf(item.getComment())));
        // }else{
        //     ratingBar.setRating((float) 0.0);
        // }

        descriptionTextView.setText(item.getDescription());

        // fab.setBackgroundTintList(ColorStateList.valueOf((backgroundColor)));
        // Drawable fabIcon = getResources().getDrawable(R.drawable.ic_star_black_24dp, null).getConstantState().newDrawable();
        // fabIcon.mutate().setColorFilter(frontColor, PorterDuff.Mode.SRC_ATOP);
        // fab.setImageDrawable(fabIcon);
    }

    private void onReviewsDataChanged(List<Review> reviewList, final int itemId){
        // Updates the rating cardView below
        if(reviewList != null){     /* If review list is not empty */
            // Converts Total Review to a String
            String totalReviews = NumberFormat.getNumberInstance(Locale.US)
                    .format(reviewList.size());
            totalReviewTextView.setText(totalReviews);      // Sets TextView a Total Review

            // Calculates average score
            Double calculatedAverage = Review.calculateAverage(reviewList);

            // System.out.println("Score: " + calculatedAverage + " (" + totalReviews + ")");
            // Applies Total Review and average score to dialog
            itemInfoAdapter.applyReviewsChanges(totalReviews, calculatedAverage);

            // ratingTextView.setText(String.format("%.1f", calculatedAverage) + " (" + totalReviews + ")");

            // Sets Average Score to a TextView
            scoreTextView.setText(String.format(HelperUtilities.getCurrentLocale(getContext()), "%.1f", calculatedAverage));

            // Sets Average Score to a RatingBar
            ratingBar1.setRating(Float.valueOf(String.valueOf(calculatedAverage)));

            // Calculates weights for Ratio Views
            ArrayList<Float> calculateWeight = calculateScalePercentage(reviewList);
            int count = 0;
            for(View ratioBar : scoreBarRatioViewList){
                ratioBar.setLayoutParams(new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, calculateWeight.get(count++)));
            }
        }else{
            totalReviewTextView.setText("0");
            ratingTextView.setText("0 (0)");
            ratingBar1.setRating((float) 0.0);
        }

        // Setups the showAllReviewButton
        final SparseArray<ArrayList<Review>> reviewMap = ItemViewModel.convertReviewListToSparseArray(reviewList);
        userReviewAdapter.applyReviewDataChanges(reviewMap.get(itemId));

        // If there are reviews of this item
        if(reviewMap.get(itemId) != null){
            showAllReviewsButton.setEnabled(true);      // Enables the showAllReviewButton
            showAllReviewsButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    // AllReviewDialogFragment allReviewDialogFragment = new AllReviewDialogFragment();
                    // Bundle bundle = new Bundle();
                    // bundle.putInt("itemId", finalItemId);
                    // allReviewDialogFragment.setArguments(bundle);
                    // allReviewDialogFragment.show(getActivity().getSupportFragmentManager(), "Oh shit");
                    new MaterialDialog.Builder(getContext())
                            .title("All Reviews (" + reviewMap.get(itemId).size() + ")")
                            .positiveText("Done")
                            .adapter(userReviewAdapter, new LinearLayoutManager(getContext()))
                            .show();
                }
            });
        }else{
            showAllReviewsButton.setText(R.string.noReviewAvailable);
            showAllReviewsButton.setEnabled(false);
        }
    }

    public void setItemDeleteListener(ItemDeleteListener itemDeleteListener){
        this.itemDeleteListener = itemDeleteListener;
    }

    public interface ItemStatusListener{
        void onItemListEmpty();

        void onItemBinding();

        void onItemListNotEmpty();
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

    public interface ItemDeleteListener{
        public void onDelete();
    }
}
