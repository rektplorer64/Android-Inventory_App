package tanawinwichitcom.android.inventoryapp;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AdvancedSearchSettingFragment extends Fragment{

    private int PREF_SEARCH_BY = 0;
    private DateFilterPreference dateFilterPreference;

    private LinearLayout Pref_searchItemBy;
    private TextView searchBy_subtitle;

    private CardView Pref_dateCreatedFrom;
    private TextView dateCreated_from;
    private Switch dateCreated_fromSwitch;

    private CardView Pref_dateCreatedTo;
    private TextView dateCreated_to;
    private Switch dateCreated_toSwitch;

    private CardView Pref_dateModifiedFrom;
    private TextView dateModified_from;
    private Switch dateModified_fromSwitch;

    private CardView Pref_dateModifiedTo;
    private TextView dateModified_to;
    private Switch dateModified_toSwitch;

    private LinearLayout Pref_containsImage;

    public AdvancedSearchSettingFragment(){
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
    }

    private void setOnClick(){
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
                                PREF_SEARCH_BY = which;
                                searchBy_subtitle.setText(text.toString());
                                return false;
                            }
                        })
                        .positiveText("Choose")
                        .show();
            }
        });

        final MaterialDialog.Builder datePickerDialog = new MaterialDialog.Builder(getContext());
        datePickerDialog.customView(R.layout.dialog_datepicker, false)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel);

        final ArrayList<CardView> datePickerCards = new ArrayList<>();
        datePickerCards.add(Pref_dateCreatedFrom);
        datePickerCards.add(Pref_dateCreatedTo);
        datePickerCards.add(Pref_dateModifiedFrom);
        datePickerCards.add(Pref_dateModifiedTo);

        ArrayList<Switch> switchArrayList = new ArrayList<>();
        switchArrayList.add(dateCreated_fromSwitch);
        switchArrayList.add(dateCreated_toSwitch);
        switchArrayList.add(dateModified_fromSwitch);
        switchArrayList.add(dateModified_toSwitch);

        Integer count = 0;
        for(final CardView cardView : datePickerCards){
            final Integer finalCount = count;
            cardView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    setDateDialogPositiveBehavior(datePickerDialog, finalCount);
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
        for(Switch s : switchArrayList){
            final Integer finalCount = count;
            s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    int colorFrom = Color.WHITE;
                    @SuppressLint("ResourceType") int colorTo = Color.parseColor(getString(R.color.md_yellow_400));

                    ValueAnimator colorAnimation;
                    if(isChecked){
                        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                        colorAnimation.setDuration(250); // milliseconds
                        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
                            @Override
                            public void onAnimationUpdate(ValueAnimator animator){
                                datePickerCards.get(finalCount).setBackgroundColor((int) animator.getAnimatedValue());
                            }
                        });
                        colorAnimation.start();
                    }else{
                        colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorTo, colorFrom);
                        colorAnimation.setDuration(250); // milliseconds
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
        dateFilterPreference = new DateFilterPreference();
        initializeViews(view);
        setDateTexts();
        setOnClick();
    }

    private void setDateTexts(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");
        String dateString = dateFormat.format(date);
        dateCreated_from.setText(dateString);
        dateCreated_to.setText(dateString);
        dateModified_from.setText(dateString);
        dateModified_to.setText(dateString);
    }

    private void setDateDialogPositiveBehavior(MaterialDialog.Builder d, final int mode){
        final DatePicker datePicker = d.build().getCustomView().findViewById(R.id.datePicker);
        final DateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY");

        d.onPositive(new MaterialDialog.SingleButtonCallback(){
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which){
                final int year = datePicker.getYear();
                final int month = datePicker.getMonth();
                final int dayOfMonth = datePicker.getDayOfMonth();
                Date date = new GregorianCalendar(year, month, dayOfMonth).getTime();
                Toast.makeText(getContext(), date.toString(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(getContext(), "onPositive", Toast.LENGTH_SHORT).show();
                switch(mode){
                    case 0:
                        Toast.makeText(getContext(), "case 0", Toast.LENGTH_SHORT).show();
                        dateFilterPreference.setDateCreatedFrom(date);
                        dateCreated_from.setText(dateFormat.format(dateFilterPreference.getDateCreatedFrom()));
                        break;
                    case 1:
                        Toast.makeText(getContext(), "case 1", Toast.LENGTH_SHORT).show();
                        dateFilterPreference.setDateCreatedTo(date);
                        dateCreated_to.setText(dateFormat.format(dateFilterPreference.getDateCreatedTo()));
                        break;
                    case 2:
                        Toast.makeText(getContext(), "case 2", Toast.LENGTH_SHORT).show();
                        dateFilterPreference.setDateModifiedFrom(date);
                        dateModified_from.setText(dateFormat.format(dateFilterPreference.getDateModifiedFrom()));
                        break;
                    case 3:
                        Toast.makeText(getContext(), "case 3", Toast.LENGTH_SHORT).show();
                        dateFilterPreference.setDateModifiedTo(date);
                        dateModified_to.setText(dateFormat.format(dateFilterPreference.getDateModifiedTo()));
                        break;
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

    private class DateFilterPreference{

        private boolean isIncludeDateCreatedFrom;
        private Date dateCreatedFrom;

        private boolean isIncludeDateCreatedTo;
        private Date dateCreatedTo;

        private boolean isIncludeDateModifiedFrom;
        private Date dateModifiedFrom;

        private boolean isIncludeDateModifiedTo;
        private Date dateModifiedTo;

        public DateFilterPreference(){
        }

        public boolean isIncludeDateCreatedFrom(){
            return isIncludeDateCreatedFrom;
        }

        public void setIncludeDateCreatedFrom(boolean includeDateCreatedFrom){
            isIncludeDateCreatedFrom = includeDateCreatedFrom;
        }

        public Date getDateCreatedFrom(){
            return dateCreatedFrom;
        }

        public void setDateCreatedFrom(Date dateCreatedFrom){
            this.dateCreatedFrom = dateCreatedFrom;
            if(dateCreatedFrom != null){
                isIncludeDateCreatedFrom = true;
            }else{
                isIncludeDateCreatedFrom = false;
            }
        }

        public boolean isIncludeDateCreatedTo(){
            return isIncludeDateCreatedTo;
        }

        public void setIncludeDateCreatedTo(boolean includeDateCreatedTo){
            isIncludeDateCreatedTo = includeDateCreatedTo;
        }

        public Date getDateCreatedTo(){
            return dateCreatedTo;
        }

        public void setDateCreatedTo(Date dateCreatedTo){
            this.dateCreatedTo = dateCreatedTo;
            if(dateCreatedTo != null){
                isIncludeDateCreatedTo = true;
            }else{
                isIncludeDateCreatedTo = false;
            }
        }

        public boolean isIncludeDateModifiedFrom(){
            return isIncludeDateModifiedFrom;
        }

        public void setIncludeDateModifiedFrom(boolean includeDateModifiedFrom){
            isIncludeDateModifiedFrom = includeDateModifiedFrom;
        }

        public Date getDateModifiedFrom(){
            return dateModifiedFrom;
        }

        public void setDateModifiedFrom(Date dateModifiedFrom){
            this.dateModifiedFrom = dateModifiedFrom;
            if(dateModifiedFrom != null){
                isIncludeDateModifiedFrom = true;
            }else{
                isIncludeDateModifiedFrom = false;
            }
        }

        public boolean isIncludeDateModifiedTo(){
            return isIncludeDateModifiedTo;
        }

        public void setIncludeDateModifiedTo(boolean includeDateModifiedTo){
            isIncludeDateModifiedTo = includeDateModifiedTo;
        }

        public Date getDateModifiedTo(){
            return dateModifiedTo;
        }

        public void setDateModifiedTo(Date dateModifiedTo){
            this.dateModifiedTo = dateModifiedTo;
            if(dateModifiedTo != null){
                isIncludeDateModifiedTo = true;
            }else{
                isIncludeDateModifiedTo = false;
            }
        }
    }
}
