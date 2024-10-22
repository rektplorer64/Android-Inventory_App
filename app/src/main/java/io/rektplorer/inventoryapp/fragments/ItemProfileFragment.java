package io.rektplorer.inventoryapp.fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.kennyc.view.MultiStateView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;
import io.rektplorer.inventoryapp.CollectionActivity;
import io.rektplorer.inventoryapp.ItemEditingContainerActivity;
import io.rektplorer.inventoryapp.ItemProfileContainerActivity;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.SearchActivity;
import io.rektplorer.inventoryapp.fragments.dialogfragment.CircularRevealFragment;
import io.rektplorer.inventoryapp.fragments.dialogfragment.ItemEditingDialogFragment;
import io.rektplorer.inventoryapp.roomdatabase.DataRepository;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;
import io.rektplorer.inventoryapp.roomdatabase.Entities.User;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.rvadapters.DetailedScoreAdapter;
import io.rektplorer.inventoryapp.rvadapters.ItemImageAdapter;
import io.rektplorer.inventoryapp.rvadapters.ItemInfoAdapter;
import io.rektplorer.inventoryapp.rvadapters.UserReviewAdapter;
import io.rektplorer.inventoryapp.utility.ColorUtility;
import io.rektplorer.inventoryapp.utility.ScreenUtility;
import io.rektplorer.inventoryapp.utility.UserInterfaceUtility;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static io.rektplorer.inventoryapp.utility.ColorUtility.darkenColor;

public class ItemProfileFragment extends CircularRevealFragment implements Toolbar.OnMenuItemClickListener{

    private ItemChangeListener itemChangeListener;

    private MultiStateView itemProfFragMultiState;

    private Window window;
    private NestedScrollView nestedScrollView;
    private AppBarLayout appBarLayout;
    private LinearLayout itemProfileLinearLayout;
    private ItemViewModel itemViewModel;
    private Toolbar toolbar;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView itemImageView;
    private AppCompatTextView itemNameTextView, quantityTextView, descriptionTextView, tagsTextView;
    //private RatingBar ratingBar;

    // Views of the Image Card
    private MultiStateView imageRecyclerMultiState;
    private RecyclerView imageRecyclerView;
    private ItemImageAdapter imageAdapter;

    // Views of the Rating Card
    private RatingBar ratingBar1;
    private AppCompatTextView scoreTextView, totalReviewTextView, ratingTextView;
    private Button showAllReviewsButton;
    private CardView scoreRatioCardView;
    private ArrayList<View> scoreBarRatioViewList;

    private ChipGroup tagChipGroup;

    private ItemInfoAdapter itemInfoAdapter;
    private UserReviewAdapter userReviewAdapter;

    // In case of the user already rated this item
    private ImageButton reviewOptionImageButton;

    private Item CURRENT_ITEM;

    private int itemId = 0;

    public ItemProfileFragment(){
    }

    public static ItemProfileFragment newInstance(int fragmentLayoutRes, int itemId, int centerX,
                                                  int centerY){
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
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState){
        itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        final Bundle bundle = getArguments();

        itemId = itemViewModel
                .getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE,
                                    DataRepository.ITEM_FIELD_ID);
        if(bundle != null){
            itemId = bundle.getInt("itemId");
        }

        final int finalItemId = itemId;
        initializeViews(view);
        setupUiScales(view);

        itemInfoAdapter = new ItemInfoAdapter(getContext());

        userReviewAdapter = new UserReviewAdapter(finalItemId);
        userReviewAdapter.setHasStableIds(true);

        final Toolbar.OnMenuItemClickListener menuItemClickListener = this;
        itemViewModel.getAllItems().observe(this, new Observer<List<Item>>(){
            @Override
            public void onChanged(final List<Item> items){
                if(itemViewModel.getItemById(itemId).hasActiveObservers()){
                    itemViewModel.getItemById(itemId).removeObservers(getViewLifecycleOwner());
                }
                itemViewModel.getItemById(itemId).observe(getActivity(), new Observer<Item>(){
                    @Override
                    public void onChanged(@Nullable final Item item){
                        if(item == null){
                            // itemProfFragMultiState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                            view.setVisibility(View.INVISIBLE);
                            return;
                        }else{
                            itemProfFragMultiState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                        }
                        CURRENT_ITEM = item;
                        toolbar.setOnMenuItemClickListener(menuItemClickListener);
                        onItemDataChanged(item, view);
                    }
                });
            }
        });

        itemViewModel.getReviewsByItemId(itemId).observe(this, new Observer<List<Review>>(){
            @Override
            public void onChanged(@Nullable List<Review> reviewList){
                // replaceReviewSectionView();
                onReviewsDataChanged(reviewList, itemId);
                setupScoreInfoDialog(reviewList);
            }
        });

        itemViewModel.getAllUsers().observe(this, new Observer<List<User>>(){
            @Override
            public void onChanged(@Nullable List<User> users){
                userReviewAdapter.applyUserDataChanges(users);
            }
        });

        final Fragment fragment = this;
        itemViewModel.getReviewByItemAndUserId(itemId, 1).observe(this, new Observer<Review>(){
            @Override
            public void onChanged(@Nullable final Review review){
                itemViewModel.getUserById(1).observe(fragment, new Observer<User>(){
                    @Override
                    public void onChanged(@Nullable final User user){
                        adjustReviewControllerMode(view, review != null, user, review);
                    }
                });
            }
        });

        itemViewModel.getHeroImageByItemId(itemId).observe(this, new Observer<Image>(){
            @Override
            public void onChanged(Image image){
                Glide.with(getContext())
                     .load((image != null) ? image
                             .getImageFile() : R.drawable.md_wallpaper_placeholder)
                     .transition(DrawableTransitionOptions.withCrossFade())
                     .thumbnail(0.025f)
                     .into(itemImageView);
            }
        });

        itemViewModel.getImagesByItemId(itemId).observe(this, new Observer<List<Image>>(){
            @Override
            public void onChanged(List<Image> imageList){
                if(imageList != null && !imageList.isEmpty()){
                    imageRecyclerMultiState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
                    imageAdapter.applyDataChanges(imageList);
                }else{
                    imageRecyclerMultiState.setViewState(MultiStateView.VIEW_STATE_EMPTY);
                }
            }
        });
    }

    private void setupScoreInfoDialog(List<Review> reviewList){
        final DetailedScoreAdapter detailedScoreAdapter = new DetailedScoreAdapter();
        detailedScoreAdapter.applyReviewsDataChanges(reviewList);

        scoreRatioCardView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MaterialDialog materialDialog = new MaterialDialog.Builder(getContext())
                        .title("Score Information")
                        .negativeText("close")
                        .content(
                                "Full details of all score rated this item, separated by Number of stars.")
                        .adapter(detailedScoreAdapter,
                                 new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                                                         false))
                        .build();

                RecyclerView rc = materialDialog.getRecyclerView();
                rc.addItemDecoration(
                        new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
                materialDialog.show();
            }
        });
    }

    private void adjustReviewControllerMode(View rootView, final boolean isRated, final User user,
                                            final Review review){
        ViewSwitcher reviewControl = rootView.findViewById(R.id.reviewControl);
        reviewControl.setInAnimation(
                AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_in_left));
        reviewControl.setOutAnimation(
                AnimationUtils.loadAnimation(getContext(), android.R.anim.slide_out_right));

        // ViewSwitcher States
        // 1. Empty State (No review from current user)
        // 2. Review from current user Available

        if(isRated){
            // Initialize Views
            if(reviewControl.getDisplayedChild() != 1){
                reviewControl.showNext();
            }

            TextView realNameTextView = rootView.findViewById(R.id.realNameTextView);
            TextView ratedDateTextView = rootView.findViewById(R.id.ratedDateTextView);
            MaterialRatingBar indicatorScoreRatingBar = rootView
                    .findViewById(R.id.indicatorScoreRatingBar);

            realNameTextView.setText(
                    new StringBuilder().append(user.getName()).append(" ").append(user.getSurname())
                                       .append(" (").append(user.getUsername()).append(")")
                                       .toString());

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", ScreenUtility
                    .getCurrentLocale(getContext()));
            ratedDateTextView.setText(new StringBuilder().append("Rated on ").append(dateFormat
                                                                                             .format(review.getTimeStamp()))
                                                         .toString());
            indicatorScoreRatingBar.setRating((float) review.getRating());

            reviewOptionImageButton.setVisibility(View.VISIBLE);
            reviewOptionImageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(getContext(), reviewOptionImageButton);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.review_actions, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                        public boolean onMenuItemClick(MenuItem menuItem){
                            switch(menuItem.getItemId()){
                                case R.id.action_edit:
                                    showReviewEditDialog(isRated, review, user);
                                    break;
                                case R.id.action_delete:
                                    new MaterialDialog.Builder(getContext())
                                            .title("Delete your review?")
                                            .positiveText("Yes")
                                            .negativeText("No")
                                            .positiveColor(Color.RED)
                                            .negativeColor(Color.BLUE)
                                            .buttonRippleColor(Color.RED)
                                            .onPositive(new MaterialDialog.SingleButtonCallback(){
                                                @Override
                                                public void onClick(@NonNull MaterialDialog dialog,
                                                                    @NonNull DialogAction which){
                                                    itemViewModel.delete(review);
                                                }
                                            }).show();
                                    break;
                            }
                            return true;
                        }
                    });
                    popup.show();
                }
            });
        }else{
            if(reviewControl.getDisplayedChild() != 0){
                reviewControl.showNext();
            }
            Button showReviewDialogButton = rootView.findViewById(R.id.showReviewDialogButton);
            showReviewDialogButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    showReviewEditDialog(isRated, review, user);
                }
            });

            reviewOptionImageButton.setVisibility(View.GONE);
        }
    }

    private void showReviewEditDialog(final boolean isRated, final Review review, final User user){
        final MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_modify_review, false)
                .canceledOnTouchOutside(false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .positiveColor(Color.BLUE)
                .contentGravity(GravityEnum.CENTER)
                .build();

        View dialogRootView = dialog.getCustomView();
        final MaterialRatingBar giveScoreRatingBar = dialogRootView
                .findViewById(R.id.giveScoreRatingBar);
        final TextView scoreTextView = dialogRootView.findViewById(R.id.scoreTextView);
        final TextInputLayout reviewEditTextWrapper = dialogRootView
                .findViewById(R.id.reviewEditTextWrapper);
        final EditText reviewEditText = dialogRootView.findViewById(R.id.reviewEditText);

        final View positiveButton = dialog.getActionButton(DialogAction.POSITIVE);

        if(isRated){
            scoreTextView.setText(String.format("%.1f", review.getRating()));
            giveScoreRatingBar.setRating((float) review.getRating());
            reviewEditText.setText(review.getComment());
        }

        giveScoreRatingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener(){
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating){
                if(rating == 0.0f){
                    positiveButton.setEnabled(false);
                }else{
                    positiveButton.setEnabled(true);
                }
                scoreTextView.setText(String.format("%.1f", rating));
            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                if(reviewEditText.getText().toString().isEmpty()){
                    reviewEditTextWrapper.setError("Please tell us what you think");
                    return;
                }

                Date currentDate = Calendar.getInstance().getTime();
                if(isRated){
                    review.setComment(reviewEditText.getText().toString());
                    review.setRating(giveScoreRatingBar.getRating());
                    review.setTimeStamp(currentDate);
                    itemViewModel.update(review);
                }else{
                    Review newReview = new Review(currentDate, user.getId(), itemId,
                                                  reviewEditText.getText().toString(),
                                                  giveScoreRatingBar.getRating());
                    itemViewModel.insert(newReview);
                }
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    /**
     * Sets up status bar and toolbar
     *
     * @param backColorInt  Background color
     * @param frontColorInt Foreground color
     * @param rootView      fragment's root view
     */
    private void setupStatusAndToolbar(@ColorInt int backColorInt, @ColorInt int frontColorInt
            , final View rootView){
        // Changes Toolbar's color according to the selected color
        //toolbar.setBackgroundColor(backgroundColor);
        toolbar.setTitleTextColor(frontColorInt);       // Sets Toolbar's Title text color

        // Set behavior: when clicks on itemImageView, collapses appBarLayout
        itemImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                appBarLayout.setExpanded(false);
            }
        });

        if(getActivity() instanceof ItemProfileContainerActivity){
            collapsingToolbarLayout.setContentScrimColor(backColorInt);
            // Changes Navigation Icon (Back Arrow Icon)'s color
            toolbar.getNavigationIcon().setTint(Color.WHITE);

            // Changes Back button on toolbar behavior
            toolbar.setNavigationOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(getActivity() instanceof ItemProfileContainerActivity){
                        rootView.requestFocus();    // Clears focus
                        closeActivityCircularly();      // Close Fragment
                    }
                }
            });

            // Changes Status bar's color according to the selected color
            window.setStatusBarColor(darkenColor(backColorInt));

            // Changes Navigation Bar (Soft keys bar) color
            window.setNavigationBarColor(darkenColor(backColorInt, 0.5f));
        }else if(getActivity() instanceof CollectionActivity){
            collapsingToolbarLayout
                    .setContentScrimColor(darkenColor(backColorInt));    // Set scrimColor
        }else{
            collapsingToolbarLayout
                    .setContentScrimColor(darkenColor(backColorInt));    // Set scrimColor
            toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
            toolbar.getNavigationIcon().setTint(Color.WHITE);

            // Changes Back button on toolbar behavior
            toolbar.setNavigationOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    getActivity().getSupportFragmentManager().beginTransaction()
                                 .remove(getParentFragment()).commit();
                }
            });
        }
    }

    private void setupUiScales(final View rootView){
        if(getActivity() instanceof ItemProfileContainerActivity){
            // If this fragment is launched in ItemProfileContainerActivity and the screen size at least LARGE
            if(ScreenUtility.getScreenSizeCategory(getContext()) >= ScreenUtility.SCREENSIZE_LARGE){
                // If the screen is too wide, sets the side padding of fragment's LinearLayout.
                int padding = ScreenUtility.dpToPx(100, getContext());
                itemProfileLinearLayout.setPadding(padding, 0, padding, 0);
                // itemImageView.getLayoutParams().height = ScreenUtility.dpToPx(1000, getContext());
                // itemImageView.requestLayout();
            }
        }else if(getActivity() instanceof CollectionActivity){
            /* Adjusts Layout According to the screen size */
            rootView.post(new Runnable(){
                @Override
                public void run(){
                    // System.out.println("ItemProfile RootView's Width: " + rootView.getWidth());
                    if(rootView
                            .getWidth() > 1174){      // If the app takes the entire screen (too wide in landscape)
                        // Sets the horizontal padding
                        int padding = ScreenUtility.dpToPx(120, rootView.getContext());
                        itemProfileLinearLayout.setPadding(padding, 0, padding, 0);
                    }
                }
            });
        }
    }

    /**
     * Triggered activity closing sequence with Circular Reveal Activity
     */
    @SuppressLint("RestrictedApi")
    private void closeActivityCircularly(){
        // Triggers Back button because the fragment is a subclass of CircularRevealFragment (Circular Reveal will trigger when Back Button is Pressed)
        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

        // Close Activity
        getActivity().finish();

        // Sets Fade in animation and Fade out animation
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Binds all views in the Layout Resource
     *
     * @param view rootView of the fragment
     */
    private void initializeViews(View view){
        if(getActivity() instanceof ItemProfileContainerActivity){      // If fragment launches inside ItemProfileContainerActivity
            // Gets the Window in order to change Status Bar's Color
            window = getActivity().getWindow();
            // // clear FLAG_TRANSLUCENT_STATUS flag:
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        itemProfFragMultiState = view.findViewById(R.id.itemProfFragMultiState);
        if(getActivity() instanceof ItemProfileContainerActivity){
            itemProfFragMultiState.setViewState(MultiStateView.VIEW_STATE_CONTENT);
        }

        nestedScrollView = view.findViewById(R.id.nestedScrollView);
        toolbar = view.findViewById(R.id.fragmentToolbar);      // Binds toolbar
        if(getActivity() instanceof ItemProfileContainerActivity){      // If fragment launches inside ItemProfileContainerActivity
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.setSupportActionBar(
                    toolbar);       // Sets support ActionBar with toolbar object
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);    // Hides Title
            activity.getSupportActionBar()
                    .setDisplayHomeAsUpEnabled(true);      // Shows Navigation Icon (Back button)
        }else{
            // toolbar.setVisibility(View.GONE);
            toolbar.inflateMenu(R.menu.menu_item_profile);      // Inflates Menu Item
        }

        /* Sets the weight sum of the Rating LinearLayout to 100 */
        LinearLayout ratingRatioGroup = view.findViewById(R.id.ratingRatioGroup);
        ratingRatioGroup.setWeightSum(100f);

        appBarLayout = view.findViewById(R.id.appBarLayout);
        itemProfileLinearLayout = view.findViewById(R.id.itemProfileLinearLayout);
        collapsingToolbarLayout = view.findViewById(R.id.collapsingToolbar);
        itemImageView = view.findViewById(R.id.itemImageView);
        itemNameTextView = view.findViewById(R.id.itemTextView);
        quantityTextView = view.findViewById(R.id.quantityTextView);
        //ratingBar = view.findViewById(R.id.ratingBarView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        tagsTextView = view.findViewById(R.id.tagsTextView);
        //fab = view.findViewById(R.id.profileFab);

        tagChipGroup = view.findViewById(R.id.tagChipGroup);

        imageRecyclerMultiState = view.findViewById(R.id.imageRecyclerMultiState);

        imageRecyclerView = view.findViewById(R.id.imageRecyclerView);
        imageAdapter = new ItemImageAdapter(getContext(), false, false);
        imageRecyclerView.setAdapter(imageAdapter);
        imageRecyclerView.setLayoutManager(
                new GridLayoutManager(getContext(), 3, RecyclerView.HORIZONTAL, false));

        /* Setup Views of the lower card */
        reviewOptionImageButton = view.findViewById(R.id.reviewOptionImageButton);
        reviewOptionImageButton.setVisibility(View.GONE);
        ratingBar1 = view.findViewById(R.id.ratingBarView2);
        scoreTextView = view.findViewById(R.id.scoreTextView);
        totalReviewTextView = view.findViewById(R.id.totalReviewTextView);
        ratingTextView = view.findViewById(R.id.ratingTextView);
        scoreRatioCardView = view.findViewById(R.id.scoreRatioCardView);
        showAllReviewsButton = view.findViewById(R.id.showAllReviewsButton);

        /* Setup Views of Rating Ratio Indicator */
        scoreBarRatioViewList = new ArrayList<>();
        scoreBarRatioViewList.add(view.findViewById(R.id.oneStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.twoStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.threeStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.fourStarRec));
        scoreBarRatioViewList.add(view.findViewById(R.id.fiveStarRec));
    }

    // @Override
    // public boolean onOptionsItemSelected(MenuItem item){
    //     switch(item.getItemId()){
    //         case R.id.action_edit:{
    //             // Toast.makeText(getContext(), "Clicked edit button...", Toast.LENGTH_SHORT).show();
    //             Intent intent = new Intent(getContext(), AddItemActivity.class);
    //             intent.putExtra("itemId", getActivity().getIntent().getExtras().getInt("itemId"));
    //             startActivity(intent);
    //             return true;
    //         }
    //     }
    //     return super.onOptionsItemSelected(item);
    // }

    public void setItemChangeListener(ItemChangeListener itemChangeListener){
        this.itemChangeListener = itemChangeListener;
    }

    private void onItemDataChanged(@Nullable Item item, View rootView){
        if(item == null || getContext() == null){
            return;
        }

        //window.setStatusBarColor(darkenColor(item.getItemColorAccent()));
        int backgroundColor = item.getItemColorAccent();
        int frontColor = ColorUtility.getSuitableFrontColor(getContext(), backgroundColor, true);
        setupStatusAndToolbar(backgroundColor, frontColor, rootView);

        itemNameTextView.setText(item.getName());
        quantityTextView.setText(new StringBuilder()
                                         .append(NumberFormat.getNumberInstance(
                                                 ScreenUtility.getCurrentLocale(getContext()))
                                                             .format(item.getQuantity()))
                                         .toString());

        itemInfoAdapter.applyInfoDataChanges(item);

        if(item.getTags() != null && !item.getTags().isEmpty()){
            tagChipGroup.setVisibility(View.VISIBLE);
            tagsTextView.setText("Tags");
            tagsTextView.setGravity(View.TEXT_ALIGNMENT_TEXT_START);
            tagChipGroup.removeAllViews();
            for(String tag : item.getTags()){
                Chip chip = new Chip(getContext());
                chip.setText(tag);
                tagChipGroup.addView(chip);
            }
        }else{
            tagChipGroup.setVisibility(View.GONE);
            tagsTextView.setText("NO TAGS AVAILABLE");
            tagsTextView.setGravity(View.TEXT_ALIGNMENT_CENTER);

        }

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
            scoreTextView.setText(
                    String.format(ScreenUtility.getCurrentLocale(getContext()), "%.1f",
                                  calculatedAverage));

            // Sets Average Score to a RatingBar
            ratingBar1.setRating(Float.valueOf(String.valueOf(calculatedAverage)));

            // Calculates weights for Ratio Views
            ArrayList<Float> calculateWeight = UserInterfaceUtility
                    .calculateScalePercentage(reviewList);
            int count = 0;
            for(View ratioBar : scoreBarRatioViewList){
                ratioBar.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                       ViewGroup.LayoutParams.MATCH_PARENT,
                                                                       calculateWeight
                                                                               .get(count++)));
            }
        }else{
            totalReviewTextView.setText("0");
            ratingTextView.setText("0 (0)");
            ratingBar1.setRating((float) 0.0);
            for(View ratioBar : scoreBarRatioViewList){
                ratioBar.setVisibility(View.INVISIBLE);
            }
        }

        // Setups the showAllReviewButton
        final SparseArray<ArrayList<Review>> reviewMap = ItemViewModel
                .convertReviewListToSparseArray(reviewList);
        userReviewAdapter.applyReviewDataChanges(reviewMap.get(itemId));

        // If there are reviews of this item
        if(reviewMap.get(itemId) != null){
            showAllReviewsButton.setEnabled(true);      // Enables the showAllReviewButton
            showAllReviewsButton.setText("show all reviews");
            showAllReviewsButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    // AllReviewDialogFragment allReviewDialogFragment = new AllReviewDialogFragment();
                    // Bundle bundle = new Bundle();
                    // bundle.putInt("itemId", finalItemId);
                    // allReviewDialogFragment.setArguments(bundle);
                    // allReviewDialogFragment.show(getActivity().getSupportFragmentManager(), "Oh shit");
                    MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(getContext())
                            .title("All Reviews (" + reviewMap.get(itemId).size() + ")")
                            .positiveText("Done");

                    RecyclerView.LayoutManager layoutManager;

                    if(ScreenUtility
                            .getScreenSizeCategory(getContext()) >= ScreenUtility.SCREENSIZE_LARGE){
                        layoutManager = new GridLayoutManager(getContext(),
                                                              GridLayoutManager.VERTICAL);
                    }else{
                        layoutManager = new LinearLayoutManager(getContext());
                    }
                    dialogBuilder.adapter(userReviewAdapter, layoutManager);
                    dialogBuilder.show();
                }
            });
        }else{
            showAllReviewsButton.setText(R.string.noReviewAvailable);
            showAllReviewsButton.setEnabled(false);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem){
        final Item item = CURRENT_ITEM;
        switch(menuItem.getItemId()){
            case R.id.action_more_info:{
                new MaterialDialog.Builder(getContext()).title("Info")
                                                        .adapter(itemInfoAdapter,
                                                                 new LinearLayoutManager(
                                                                         getContext()))
                                                        .negativeText("Close")
                                                        .build()
                                                        .show();
                break;
            }
            case R.id.action_edit:{
                if(ScreenUtility.getScreenSizeCategory(
                        getContext()) >= ScreenUtility.SCREENSIZE_LARGE && ScreenUtility
                        .getScreenOrientation(
                                getContext()) == ScreenUtility.SCREENORIENTATION_PORTRAIT){
                    ItemEditingDialogFragment editingDialog = ItemEditingDialogFragment
                            .newInstance(itemId, true);
                    editingDialog.setOnDialogConfirmListener(
                            new ItemEditingDialogFragment.OnDialogConfirmListener(){
                                @Override
                                public void onDialogConfirm(int itemId){
                                    Toasty.success(getContext(), "Successfully saved!").show();
                                }
                            });
                    editingDialog.show(getFragmentManager(), "itemEditingDialogFragment");
                }else{
                    Intent intent = new Intent(getActivity(), ItemEditingContainerActivity.class);
                    intent.putExtra("itemId", itemId);
                    intent.putExtra("inEditMode", true);
                    startActivity(intent);
                }
                break;
            }
            case R.id.action_delete:{
                new MaterialDialog.Builder(getContext()).title("Delete " + item.getName() + "?")
                                                        .negativeText("No").positiveText("Yes")
                                                        .theme(Theme.LIGHT)
                                                        .onPositive(
                                                                new MaterialDialog.SingleButtonCallback(){
                                                                    @Override
                                                                    public void onClick(
                                                                            @NonNull MaterialDialog dialog,
                                                                            @NonNull DialogAction which){
                                                                        itemViewModel.delete(item);
                                                                        // Toasty.success(getContext(), "Item Deleted successfully").show();
                                                                        if(getActivity() instanceof ItemProfileContainerActivity){
                                                                            closeActivityCircularly();
                                                                        }else if(getActivity() instanceof SearchActivity){
                                                                            // If item profile is contained in a dialog fragment
                                                                            if(getParentFragment() != null){
                                                                                // Close dialog
                                                                                ((DialogFragment) getParentFragment())
                                                                                        .dismiss();
                                                                            }
                                                                        }
                                                                        if(itemChangeListener != null){
                                                                            itemChangeListener
                                                                                    .onItemNotFound(
                                                                                            itemId);
                                                                        }
                                                                    }
                                                                }).show();
            }
        }
        return true;
    }

    public interface ItemChangeListener{
        void onItemNotFound(int itemId);
    }
}
