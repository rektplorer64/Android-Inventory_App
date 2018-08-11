package io.rektplorer.inventoryapp.fragments;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import es.dmoral.toasty.Toasty;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.roomdatabase.DataRepository;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference;
import io.rektplorer.inventoryapp.utility.HelperUtility;

import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.DateType;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.SearchBy;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.loadFromSharedPreference;
import static io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference.saveToSharedPreference;

public class SearchPreferenceFragment extends Fragment{

    private FilterPreference searchPref;

    private LinearLayout Pref_searchItemBy;
    private AppCompatTextView searchBy_subtitle;

    private CardView Pref_dateCreatedFrom;
    private AppCompatTextView dateCreated_from;
    private CheckBox dateCreated_fromSwitch;

    private CardView Pref_dateCreatedTo;
    private AppCompatTextView dateCreated_to;
    private CheckBox dateCreated_toSwitch;

    private CardView Pref_dateModifiedFrom;
    private AppCompatTextView dateModified_from;
    private CheckBox dateModified_fromSwitch;

    private CardView Pref_dateModifiedTo;
    private AppCompatTextView dateModified_to;
    private CheckBox dateModified_toSwitch;

    private LinearLayout Pref_containsImage;
    private AppCompatTextView imageFiltering_subtitle;

    private Switch quantitySwitch;
    private RangeBar quantityRangeBar;

    private LinearLayout Pref_tags;
    private AppCompatTextView tag_subtitle;

    private SearchPreferenceUpdateListener searchPreferenceUpdateListener;

    public static final String SEARCH_PREF = "SEARCH_PREF";

    private final String[] searchByStrings = new String[]{"Item Name", "Item ID",
                                                          "Item Description"};
    private final String subtitleImageModeStr[] = new String[]{"Fetch any items",
                                                               "Fetch every items with image only",
                                                               "Fetch every item without image only"};

    public SearchPreferenceFragment(){
    }

    public static SearchPreferenceFragment newInstance(){
        // Bundle args = new Bundle();
        // args.putParcelable(SEARCH_PREF, searchPref);
        // fragment.setArguments(args);
        return new SearchPreferenceFragment();
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

        Pref_tags = view.findViewById(R.id.Pref_tags);
        tag_subtitle = view.findViewById(R.id.tag_subtitle);
        Pref_tags.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                TagSelectorDialogFragment tagSelectorDialogFragment = (TagSelectorDialogFragment) getChildFragmentManager().findFragmentByTag("TAG_SELECTOR");
                if(tagSelectorDialogFragment == null){
                    tagSelectorDialogFragment = TagSelectorDialogFragment.newInstance();
                }
                tagSelectorDialogFragment.show(fragmentTransaction, "TAG_SELECTOR");

                final TagSelectorDialogFragment finalTagSelectorDialogFragment = tagSelectorDialogFragment;
                tagSelectorDialogFragment.setDismissListener(new TagSelectorDialogFragment.DismissListener(){
                    @Override
                    public void onDismiss(){
                        // Assert not null because onPositive will be called after the views are inflated
                        List<String> selectedTags = finalTagSelectorDialogFragment.getSelectedTags();
                        finalTagSelectorDialogFragment.reInstantiateTagSet();
                        searchPref.setTagList(selectedTags);
                        tag_subtitle.setText((searchPref.getTagList().size() != 0) ?
                                "Currently selected " + searchPref.getTagList().size() + " tags" : "No tag selected");
                        FilterPreference.saveToSharedPreference(getContext(), searchPref);
                        if(searchPreferenceUpdateListener != null){
                            searchPreferenceUpdateListener.onTagSelectionConfirm(selectedTags);
                        }
                    }
                });
            }
        });
        // tagGroup.setSingleSelection(false);
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
                                return true;
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
                                return true;
                            }
                        }).show();
            }
        });

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
        for(TextView dateDisplay: dateDisplayTextView){
            Date date = searchPref.getDatePreference(dateTypes[count++]).getDate();
            dateDisplay.setText(dateFormat.format(date));
        }

        count = 0;
        for(final CardView cardView: datePickerCards){
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
        for(CheckBox checkBox: switchArrayList){
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
        searchBy_subtitle.setText((searchPref.getSearchBy() == SearchBy.ItemName) ?
                searchByStrings[0] : (searchPref.getSearchBy() == SearchBy.ItemId) ? searchByStrings[1] : searchByStrings[2]);

        imageFiltering_subtitle.setText(subtitleImageModeStr[searchPref.getImageMode()]);
        quantitySwitch.setChecked(searchPref.getQuantityPreference().isPreferenceEnabled());

        tag_subtitle.setText((searchPref.getTagList().size() != 0) ?
                "Currently selected " + searchPref.getTagList().size() + " tag"
                        + ((searchPref.getTagList().size() > 1) ? "s" : "") : "No tag selected");
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
        //     searchPref = new FilterPreference();
        //     searchPref.setSearchBy(FilterPreference.SearchBy.ItemName);
        //     searchPref.setImageMode(FilterPreference.ANY_IMAGE);
        //     Date date = Calendar.getInstance().getTime();
        //
        //     for(FilterPreference.DateType dateType : FilterPreference.DateType.values()){
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
            searchPreferenceUpdateListener.onFragmentResumed(searchPref);
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
        d.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener(){
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
        Toasty.success(getContext(), "Saved filter preferences").show();
        saveToSharedPreference(getContext(), searchPref);
    }

    public FilterPreference getSearchPreference(){
        return searchPref;
    }

    public interface SearchPreferenceUpdateListener{
        void onDateChange(DateType dateType, Date date);

        void onDateSwitchChange(DateType dateType, boolean isCheck);

        void onSearchByDialogChange(SearchBy searchBy);

        void onImageModePrefChange(int mode);

        void onQuantitySwitchChange(boolean isChecked);

        void onQuantityRangeChange(int min, int max);

        void onFragmentResumed(FilterPreference filterPreference);

        void onTagSelectionConfirm(List<String> tagSelections);
    }
}
