package tanawinwichitcom.android.inventoryapp.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.appyvet.materialrangebar.IRangeBarFormatter;
import com.appyvet.materialrangebar.RangeBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DataRepository;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference.*;

public class SearchPreferenceFragment extends Fragment{

    private SearchPreference searchPref;

    private LinearLayout Pref_searchItemBy;
    private TextView searchBy_subtitle;

    private CardView Pref_dateCreatedFrom;
    private TextView dateCreated_from;
    private CheckBox dateCreated_fromSwitch;

    private CardView Pref_dateCreatedTo;
    private TextView dateCreated_to;
    private CheckBox dateCreated_toSwitch;

    private CardView Pref_dateModifiedFrom;
    private TextView dateModified_from;
    private CheckBox dateModified_fromSwitch;

    private CardView Pref_dateModifiedTo;
    private TextView dateModified_to;
    private CheckBox dateModified_toSwitch;

    private LinearLayout Pref_containsImage;
    private TextView imageFiltering_subtitle;

    private Switch quantitySwitch;
    private RangeBar quantityRangeBar;

    private SearchPreferenceUpdateListener searchPreferenceUpdateListener;

    public static final String SEARCH_PREF = "SEARCH_PREF";

    private final String[] searchByStrings = new String[]{"Item Name", "Item ID", "Item Description"};
    private final String subtitleImageModeStr[] = new String[]{"Fetch any items", "Fetch every items with image only", "Fetch every item without image only"};

    public SearchPreferenceFragment(){
    }

    //TODO: Implements more features
    //
    // //Simple
    // toggle search by id
    // *toggle search by tags
    // toggle search by description
    //
    // date added
    // date modified
    // contains picture?
    // no of reviews
    // ratings
    //
    // Sort
    // sort by name, date added, date modified, no of tags,


    public static SearchPreferenceFragment newInstance(){
        // Bundle args = new Bundle();
        SearchPreferenceFragment fragment = new SearchPreferenceFragment();
        // args.putParcelable(SEARCH_PREF, searchPref);
        // fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_advance_search_filter, container, false);
    }

    private void initializeViews(View view){
        Pref_searchItemBy = view.findViewById(R.id.Pref_searchItemBy);
        searchBy_subtitle = view.findViewById(R.id.searchBy_subtitle);

        Pref_dateCreatedFrom = view.findViewById(R.id.Pref_dateCreatedFrom);
        dateCreated_from = view.findViewById(R.id.dateCreated_from);
        dateCreated_fromSwitch = view.findViewById(R.id.dateCreated_fromSwitch);

        Pref_dateCreatedTo = view.findViewById(R.id.Pref_dateCreatedTo);
        dateCreated_to = view.findViewById(R.id.dateCreated_to);
        dateCreated_toSwitch = view.findViewById(R.id.dateCreated_toSwitch);

        Pref_dateModifiedFrom = view.findViewById(R.id.Pref_dateModifiedFrom);
        dateModified_from = view.findViewById(R.id.dateModified_from);
        dateModified_fromSwitch = view.findViewById(R.id.dateModified_fromSwitch);

        Pref_dateModifiedTo = view.findViewById(R.id.Pref_dateModifiedTo);
        dateModified_to = view.findViewById(R.id.dateModified_to);
        dateModified_toSwitch = view.findViewById(R.id.dateModified_toSwitch);

        Pref_containsImage = view.findViewById(R.id.Pref_containsImage);
        imageFiltering_subtitle = view.findViewById(R.id.imageFiltering_subtitle);

        quantitySwitch = view.findViewById(R.id.quantitySwitch);
        quantityRangeBar = view.findViewById(R.id.quantityRangeBar);
    }

    private void setOnClick(){
        Pref_searchItemBy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MaterialDialog.Builder(v.getContext())
                        .title("Search items by")
                        .items(searchByStrings)
                        .itemsCallbackSingleChoice(searchPref.getSearchBy().ordinal(), new MaterialDialog.ListCallbackSingleChoice(){
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text){
                                SearchBy searchBy = SearchBy.ItemName;
                                switch(which){
                                    case 0:
                                        searchBy = SearchBy.ItemName;
                                        break;
                                    case 1:
                                        searchBy = SearchBy.ItemId;
                                        break;
                                    case 2:
                                        searchBy = SearchBy.ItemDescription;
                                        break;
                                }
                                searchPref.setSearchBy(searchBy);
                                searchBy_subtitle.setText(text.toString());
                                if(searchPreferenceUpdateListener != null){
                                    searchPreferenceUpdateListener.onSearchByDialogChange(searchBy);
                                }
                                return false;
                            }
                        })
                        .show();
            }
        });

        final String imageModeStr[] = new String[]{"Any", "Contains image only", "No image only"};
        Pref_containsImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MaterialDialog.Builder(getContext())
                        .title("Image filter mode").items(imageModeStr)
                        .itemsCallbackSingleChoice(searchPref.getImageMode(), new MaterialDialog.ListCallbackSingleChoice(){
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text){
                                searchPref.setImageMode(which);
                                imageFiltering_subtitle.setText(subtitleImageModeStr[searchPref.getImageMode()]);
                                if(searchPreferenceUpdateListener != null){
                                    searchPreferenceUpdateListener.onImageModePrefChange(which);
                                }
                                return false;
                            }
                        }).show();
            }
        });

        // final MaterialDialog.Builder datePickerDialog = new MaterialDialog.Builder(getContext());
        // datePickerDialog.customView(R.layout.dialog_datepicker, true)
        //         .positiveText(android.R.string.ok)
        //         .negativeText(android.R.string.cancel);
        //


        quantitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(searchPref != null){
                    searchPref.getQuantityPreference().setPreferenceEnabled(isChecked);
                }
                if(searchPreferenceUpdateListener != null){
                    searchPreferenceUpdateListener.onQuantitySwitchChange(isChecked);
                }
            }
        });

        quantityRangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener(){
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int leftPinIndex, int rightPinIndex, String leftPinValue, String rightPinValue){
                int min = Integer.valueOf(leftPinValue);
                int max = Integer.valueOf(rightPinValue);

                if(searchPref != null){
                    searchPref.getQuantityPreference().setMinRange(min);
                    searchPref.getQuantityPreference().setMaxRange(max);
                }

                if(searchPreferenceUpdateListener != null){
                    searchPreferenceUpdateListener.onQuantityRangeChange(min, max);
                }
            }
        });

    }

    private void setupViewArray(){
        // Array of Date picker Card
        final ArrayList<CardView> datePickerCards = new ArrayList<>();
        datePickerCards.add(Pref_dateCreatedFrom);
        datePickerCards.add(Pref_dateCreatedTo);
        datePickerCards.add(Pref_dateModifiedFrom);
        datePickerCards.add(Pref_dateModifiedTo);

        // Array of Date CheckBox
        ArrayList<CheckBox> switchArrayList = new ArrayList<>();
        switchArrayList.add(dateCreated_fromSwitch);
        switchArrayList.add(dateCreated_toSwitch);
        switchArrayList.add(dateModified_fromSwitch);
        switchArrayList.add(dateModified_toSwitch);

        // Array of Date display TextView
        final ArrayList<TextView> dateDisplayTextView = new ArrayList<>();
        dateDisplayTextView.add(dateCreated_from);
        dateDisplayTextView.add(dateCreated_to);
        dateDisplayTextView.add(dateModified_from);
        dateDisplayTextView.add(dateModified_to);

        final DateType dateTypes[] = DateType.values();

        Integer count = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", HelperUtility.getCurrentLocale(getContext()));
        for(TextView dateDisplay : dateDisplayTextView){
            Date date = searchPref.getDatePreference(dateTypes[count++]).getDate();
            dateDisplay.setText(dateFormat.format(date));
        }

        count = 0;
        for(final CardView cardView : datePickerCards){
            final Integer finalCount = count;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(searchPref.getDatePreference(DateType.values()[count]).getDate());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

            final DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), null, year, month, dayOfMonth);
            setDateDialogPositiveBehavior(datePickerDialog, dateDisplayTextView, finalCount);
            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    datePickerDialog.show();
                }
            });
            count++;
        }

        count = 0;
        final int colorFrom = Color.WHITE;
        @SuppressLint("ResourceType") final int colorTo = Color.parseColor(getString(R.color.md_yellow_400));
        for(CheckBox checkBox : switchArrayList){
            final Integer finalCount = count;
            boolean datePrefCheckBox = searchPref.getDatePreference(dateTypes[finalCount]).isPreferenceEnabled();
            // Set appearances of card depending on checkBox's state
            animateDateDisplayText(datePrefCheckBox, dateDisplayTextView, datePickerCards, finalCount, colorFrom, colorTo);

            // Set checkBox's boolean from stored preference
            checkBox.setChecked(datePrefCheckBox);

            // Set onClickListener behavior to change appearances of card depending on checkBox's state
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    searchPref.getDatePreference(dateTypes[finalCount]).setPreferenceEnabled(isChecked);
                    if(searchPreferenceUpdateListener != null){
                        searchPreferenceUpdateListener.onDateSwitchChange(dateTypes[finalCount], isChecked);
                    }
                    animateDateDisplayText(isChecked, dateDisplayTextView, datePickerCards, finalCount, colorFrom, colorTo);
                }
            });
            count++;
        }
    }

    private void populateExistingData(){
        imageFiltering_subtitle.setText(subtitleImageModeStr[searchPref.getImageMode()]);
        quantitySwitch.setChecked(searchPref.getQuantityPreference().isPreferenceEnabled());
        searchBy_subtitle.setText((searchPref.getSearchBy() == SearchBy.ItemName) ?
                searchByStrings[0] : (searchPref.getSearchBy() == SearchBy.ItemId) ? searchByStrings[1] : searchByStrings[2]);
    }

    private void animateDateDisplayText(boolean isChecked
            , ArrayList<TextView> dateDisplayTextView, final ArrayList<CardView> datePickerCards
            , final int finalCount, int colorFrom, int colorTo){
        ValueAnimator colorAnimation;
        if(isChecked){
            dateDisplayTextView.get(finalCount).setTypeface(Typeface.DEFAULT_BOLD);
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(250); // milliseconds
            // datePickerCards.get(finalCount).setRadius(HelperUtility.dpToPx(200, getContext()));
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animator){
                    datePickerCards.get(finalCount).setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
        }else{
            dateDisplayTextView.get(finalCount).setTypeface(Typeface.DEFAULT);
            colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
            colorAnimation.setDuration(250); // milliseconds
            // datePickerCards.get(finalCount).setRadius(HelperUtility.dpToPx(0, getContext()));
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                @Override
                public void onAnimationUpdate(ValueAnimator animator){
                    datePickerCards.get(finalCount).setBackgroundColor((int) animator.getAnimatedValue());
                }
            });
            colorAnimation.start();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // setRetainInstance(true);
        initializeViews(view);
        setOnClick();
        setupsQuantityRangeBar();
        // if(savedInstanceState != null){
        //     searchPref = savedInstanceState.getParcelable("preferences");
        // }else{
        //     searchPref = new SearchPreference();
        //     searchPref.setSearchBy(SearchPreference.SearchBy.ItemName);
        //     searchPref.setImageMode(SearchPreference.ANY_IMAGE);
        //     Date date = Calendar.getInstance().getTime();
        //
        //     for(SearchPreference.DateType dateType : SearchPreference.DateType.values()){
        //         searchPref.setDatePreference(dateType, date);
        //         searchPref.getDatePreference(dateType).setPreferenceEnabled(false);
        //     }
        // }

    }

    @Override
    public void onResume(){
        super.onResume();
        searchPref = loadFromSharedPreference(Objects.requireNonNull(getContext()));
        populateExistingData();
        setupViewArray();
        if(searchPreferenceUpdateListener != null){
            searchPreferenceUpdateListener.onFragmentResume(searchPref);
        }
    }

    private void setupsQuantityRangeBar(){
        ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        int maxQuantityInDB = itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MAX_VALUE, DataRepository.ITEM_FIELD_QUANTITY);
        int minQuantityInDB = itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE, DataRepository.ITEM_FIELD_QUANTITY);

        if(minQuantityInDB < 1){
            minQuantityInDB = 1;
        }

        if(maxQuantityInDB == minQuantityInDB){
            maxQuantityInDB = minQuantityInDB * 2;
        }
        quantityRangeBar.setTickStart(1f);
        // Toast.makeText(getContext(), "maxQuantity: " + maxQuantityInDB, Toast.LENGTH_SHORT).show();
        try{
            quantityRangeBar.setTickEnd(maxQuantityInDB);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            quantityRangeBar.setTickEnd(100);
        }
        quantityRangeBar.setTemporaryPins(false);
        quantityRangeBar.setDrawTicks(false);
        quantityRangeBar.setPinTextFormatter(new RangeBar.PinTextFormatter(){
            @Override
            public String getText(String value){
                return value;
            }
        });
        quantityRangeBar.setFormatter(new IRangeBarFormatter(){
            @Override
            public String format(String value){
                return HelperUtility.shortenNumber(Long.valueOf(value));
            }
        });
        try{
            quantityRangeBar.setRangePinsByValue((0.25f * minQuantityInDB <= 0.5) ? 2f : 0.25f * minQuantityInDB, 0.75f * maxQuantityInDB);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            quantityRangeBar.setRangePinsByValue((0.25f * minQuantityInDB <= 0.5) ? 2f : 0.25f * 25, 0.75f * 100);
        }
        // quantityRangeBar.setRangePinsByValue(searchPref.getQuantityPreference().getMinRange(), searchPref.getQuantityPreference().getMaxRange());

    }

    public void setSearchPreferenceUpdateListener(SearchPreferenceUpdateListener s){
        this.searchPreferenceUpdateListener = s;
    }

    private void setDateDialogPositiveBehavior(final DatePickerDialog d, final ArrayList<TextView> dateDisplayTextView, final int dateTypeIndex){
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", HelperUtility.getCurrentLocale(getContext()));
        d.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                final int year = d.getDatePicker().getYear();
                final int month = d.getDatePicker().getMonth();
                final int dayOfMonth = d.getDatePicker().getDayOfMonth();
                Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();

                DateType dateType = null;
                switch(dateTypeIndex){
                    case 0:
                        dateType = DateType.DateCreated_From;
                        break;
                    case 1:
                        dateType = DateType.DateCreated_To;
                        break;
                    case 2:
                        dateType = DateType.DateModified_From;
                        break;
                    case 3:
                        dateType = DateType.DateModified_To;
                        break;
                }

                searchPref.setDatePreference(dateType, date);
                dateDisplayTextView.get(dateTypeIndex).setText(dateFormat.format(searchPref.getDatePreference(dateType).getDate()));

                if(searchPreferenceUpdateListener != null){
                    searchPreferenceUpdateListener.onDateChange(dateType, date);
                }
            }
        });

        // d.onNegative(new MaterialDialog.SingleButtonCallback(){
        //     @Override
        //     public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
        //         Toast.makeText(getContext(), "onNegative", Toast.LENGTH_SHORT).show();
        //     }
        // });
        //
        // d.onNeutral(new MaterialDialog.SingleButtonCallback(){
        //     @Override
        //     public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
        //         Toast.makeText(getContext(), "onNeutral", Toast.LENGTH_SHORT).show();
        //     }
        // });
    }

    @Override
    public void onPause(){
        super.onPause();
        // System.out.println("onPause......: " + searchPref);
        saveToSharedPreference(getContext(), searchPref);
    }

    public SearchPreference getSearchPreference(){
        return searchPref;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        // outState.putParcelable("preferences", searchPref);
    }

    public interface SearchPreferenceUpdateListener{
        void onDateChange(DateType dateType, Date date);

        void onDateSwitchChange(DateType dateType, boolean isCheck);

        void onSearchByDialogChange(SearchBy searchBy);

        void onImageModePrefChange(int mode);

        void onQuantitySwitchChange(boolean isChecked);

        void onQuantityRangeChange(int min, int max);

        void onFragmentResume(SearchPreference searchPreference);
    }
}
