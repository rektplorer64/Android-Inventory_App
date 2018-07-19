package tanawinwichitcom.android.inventoryapp.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.appyvet.materialrangebar.IRangeBarFormatter;
import com.appyvet.materialrangebar.RangeBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.DataRepository;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.rvadapters.ItemAdapter;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class SearchPreferenceFragment extends Fragment{

    private int PREF_SEARCH_BY = 0;

    private ItemAdapter.SearchPreference searchPreference;

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
    private Switch containsImageSwitch;

    private Switch quantitySwitch;
    private RangeBar quantityRangeBar;

    private SearchPreferenceUpdateListener searchPreferenceUpdateListener;


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
        containsImageSwitch = view.findViewById(R.id.containsImageSwitch);

        quantitySwitch = view.findViewById(R.id.quantitySwitch);
        quantityRangeBar = view.findViewById(R.id.quantityRangeBar);
    }

    private void setOnClick(){
        searchPreference = new ItemAdapter.SearchPreference();
        Pref_searchItemBy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String[] strings = new String[]{"Item Name", "Item ID", "Item Description"};
                new MaterialDialog.Builder(v.getContext())
                        .title("Search Items by")
                        .items(strings)
                        .itemsCallbackSingleChoice(PREF_SEARCH_BY, new MaterialDialog.ListCallbackSingleChoice(){
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text){
                                ItemAdapter.SearchPreference.SearchBy searchBy = ItemAdapter.SearchPreference.SearchBy.ItemName;
                                PREF_SEARCH_BY = which;
                                switch(which){
                                    case 0:
                                        searchBy = ItemAdapter.SearchPreference.SearchBy.ItemName;
                                        break;
                                    case 1:
                                        searchBy = ItemAdapter.SearchPreference.SearchBy.ItemId;
                                        break;
                                    case 2:
                                        searchBy = ItemAdapter.SearchPreference.SearchBy.ItemDescription;
                                        break;
                                }
                                searchPreference.setSearchBy(searchBy);
                                searchBy_subtitle.setText(text.toString());

                                if(searchPreferenceUpdateListener != null){
                                    searchPreferenceUpdateListener.onSearchByDialogChange(searchBy);
                                }
                                return false;
                            }
                        })
                        .positiveText("Choose")
                        .show();
            }
        });

        Pref_containsImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                containsImageSwitch.setChecked(!containsImageSwitch.isChecked());
            }
        });

        containsImageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                searchPreference.setContainsImage(isChecked);
                if(searchPreferenceUpdateListener != null){
                    searchPreferenceUpdateListener.onContainImageSwitchChange(isChecked);
                }
            }
        });


        final MaterialDialog.Builder datePickerDialog = new MaterialDialog.Builder(getContext());
        datePickerDialog.customView(R.layout.dialog_datepicker, true)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel);

        quantitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                searchPreference.getQuantityPreference().setPreferenceEnabled(isChecked);
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

                searchPreference.getQuantityPreference().setMinRange(min);
                searchPreference.getQuantityPreference().setMaxRange(max);

                if(searchPreferenceUpdateListener != null){
                    searchPreferenceUpdateListener.onQuantityRangeChange(min, max);
                }
            }
        });

        final ArrayList<CardView> datePickerCards = new ArrayList<>();
        datePickerCards.add(Pref_dateCreatedFrom);
        datePickerCards.add(Pref_dateCreatedTo);
        datePickerCards.add(Pref_dateModifiedFrom);
        datePickerCards.add(Pref_dateModifiedTo);

        ArrayList<CheckBox> switchArrayList = new ArrayList<>();
        switchArrayList.add(dateCreated_fromSwitch);
        switchArrayList.add(dateCreated_toSwitch);
        switchArrayList.add(dateModified_fromSwitch);
        switchArrayList.add(dateModified_toSwitch);

        final ArrayList<TextView> dateDisplayTextView = new ArrayList<>();
        dateDisplayTextView.add(dateCreated_from);
        dateDisplayTextView.add(dateCreated_to);
        dateDisplayTextView.add(dateModified_from);
        dateDisplayTextView.add(dateModified_to);

        final ItemAdapter.SearchPreference.DateType dateTypes[] = ItemAdapter.SearchPreference.DateType.values();

        Integer count = 0;
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        for(TextView dateDisplay : dateDisplayTextView){
            Date date = searchPreference.getDatePreference(dateTypes[count++]).getDate();
            dateDisplay.setText(dateFormat.format(date));
        }

        count = 0;
        for(final CardView cardView : datePickerCards){
            final Integer finalCount = count;
            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    setDateDialogPositiveBehavior(datePickerDialog, dateDisplayTextView, finalCount);
                    if(finalCount == 0 || finalCount == 2){
                        datePickerDialog.title("Pick Starting Date");
                    }else if(finalCount == 1 || finalCount == 3){
                        datePickerDialog.title("Pick Ending Date");
                    }
                    datePickerDialog.show();
                }
            });
            count++;
        }

        count = 0;
        for(CheckBox checkBox : switchArrayList){
            final Integer finalCount = count;
            checkBox.setChecked(searchPreference.getDatePreference(dateTypes[finalCount]).isPreferenceEnabled());
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    int colorFrom = Color.WHITE;
                    @SuppressLint("ResourceType") int colorTo = Color.parseColor(getString(R.color.md_yellow_400));

                    searchPreference.getDatePreference(dateTypes[finalCount]).setPreferenceEnabled(isChecked);

                    if(searchPreferenceUpdateListener != null){
                        searchPreferenceUpdateListener.onDateSwitchChange(dateTypes[finalCount], isChecked);
                    }

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
            });
            count++;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        if(savedInstanceState != null){
            searchPreference = savedInstanceState.getParcelable("preferences");
        }else{
            searchPreference = new ItemAdapter.SearchPreference();
            searchPreference.setSearchBy(ItemAdapter.SearchPreference.SearchBy.ItemName);
            searchPreference.setContainsImage(false);
            Date date = Calendar.getInstance().getTime();

            for(ItemAdapter.SearchPreference.DateType dateType : ItemAdapter.SearchPreference.DateType.values()){
                searchPreference.setDatePreference(dateType, date);
                searchPreference.getDatePreference(dateType).setPreferenceEnabled(false);
            }
        }

        setOnClick();
        setupsQuantityRangeBar();
    }

    private void setupsQuantityRangeBar(){
        ItemViewModel itemViewModel = ViewModelProviders.of(getActivity()).get(ItemViewModel.class);
        int maxQuantityInDB = itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MAX_VALUE, DataRepository.ITEM_FIELD_QUANTITY);
        int minQuantityInDB = itemViewModel.getItemDomainValue(DataRepository.ENTITY_ITEM, DataRepository.MIN_VALUE, DataRepository.ITEM_FIELD_QUANTITY);
        quantityRangeBar.setTickStart((float) minQuantityInDB);
        // Toast.makeText(getContext(), "maxQuantity: " + maxQuantityInDB, Toast.LENGTH_SHORT).show();
        quantityRangeBar.setTickEnd((float) maxQuantityInDB);
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
        quantityRangeBar.setRangePinsByValue(0.25f * minQuantityInDB, 0.75f * maxQuantityInDB);
    }

    public void setSearchPreferenceUpdateListener(SearchPreferenceUpdateListener s){
        this.searchPreferenceUpdateListener = s;
    }

    private void setDateDialogPositiveBehavior(MaterialDialog.Builder d, final ArrayList<TextView> dateDisplayTextView, final int mode){
        final DatePicker datePicker = d.build().getCustomView().findViewById(R.id.datePicker);
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY", HelperUtility.getCurrentLocale(getContext()));

        d.onPositive(new MaterialDialog.SingleButtonCallback(){
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                final int year = datePicker.getYear();
                final int month = datePicker.getMonth();
                final int dayOfMonth = datePicker.getDayOfMonth();
                Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
                // Toast.makeText(getContext(), date.toString(), Toast.LENGTH_SHORT).show();
                // Toast.makeText(getContext(), "onPositive", Toast.LENGTH_SHORT).show();

                ItemAdapter.SearchPreference.DateType dateType = null;
                switch(mode){
                    case 0:
                        dateType = ItemAdapter.SearchPreference.DateType.DateCreated_From;
                        break;
                    case 1:
                        dateType = ItemAdapter.SearchPreference.DateType.DateCreated_To;
                        break;
                    case 2:
                        dateType = ItemAdapter.SearchPreference.DateType.DateModified_From;
                        break;
                    case 3:
                        dateType = ItemAdapter.SearchPreference.DateType.DateModified_To;
                        break;
                }

                searchPreference.setDatePreference(dateType, date);
                dateDisplayTextView.get(mode).setText(dateFormat.format(searchPreference.getDatePreference(dateType).getDate()));

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
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable("preferences", searchPreference);
    }

    public interface SearchPreferenceUpdateListener{
        void onDateChange(ItemAdapter.SearchPreference.DateType dateType, Date date);

        void onDateSwitchChange(ItemAdapter.SearchPreference.DateType dateType, boolean isCheck);

        void onSearchByDialogChange(ItemAdapter.SearchPreference.SearchBy searchBy);

        void onContainImageSwitchChange(boolean isChecked);

        void onQuantitySwitchChange(boolean isChecked);

        void onQuantityRangeChange(int min, int max);
    }
}
