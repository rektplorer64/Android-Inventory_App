package tanawinwichitcom.android.inventoryapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;

public class SortPreferenceFragment extends Fragment{

    private LinearLayout Pref_sortBy;
    private TextView sortBy_subtitle;

    private LinearLayout Pref_textLength;
    private Switch textLengthSwitch;

    private LinearLayout Pref_sortingOrder;
    private TextView sortingOrder_subtitle;
    private Switch sortingSwitch;

    private SortPreference sortPreference;

    private SortPreferenceUpdateListener sortPreferenceUpdateListener;

    public SortPreferenceFragment(){

    }

    public static SortPreferenceFragment newInstance(){
        // Bundle args = new Bundle();
        SortPreferenceFragment fragment = new SortPreferenceFragment();
        // fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_sorting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        // setRetainInstance(true);
        initializeViews(view);
    }

    private void initializeViews(View view){
        Pref_sortBy = view.findViewById(R.id.Pref_sortBy);
        sortBy_subtitle = view.findViewById(R.id.sortBy_subtitle);

        Pref_textLength = view.findViewById(R.id.Pref_textLength);
        textLengthSwitch = view.findViewById(R.id.textLengthSwitch);

        Pref_sortingOrder = view.findViewById(R.id.Pref_sortingOrder);
        sortingOrder_subtitle = view.findViewById(R.id.sortingOrder_subtitle);
        sortingSwitch = view.findViewById(R.id.sortingSwitch);
    }

    private void setSwitchState(int which){
        if(which == 1 || which == 2){
            textLengthSwitch.setEnabled(true);
            Pref_textLength.setClickable(true);
            if(textLengthSwitch.isChecked()){
                sortPreference.setStringLength(true);
            }else{
                sortPreference.setStringLength(false);
            }
        }else{
            textLengthSwitch.setEnabled(false);
            Pref_textLength.setClickable(false);
            if(textLengthSwitch.isChecked()){
                sortPreference.setStringLength(true);
            }else{
                sortPreference.setStringLength(false);
            }
        }
    }

    private void setOnClick(){
        final String fields[] = new String[]{"Item ID", "Item Name", "Item Description"
                , "Date Created", "Date Modified", "Color Accent", "Quantity", "Rating"};

        Pref_textLength.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                textLengthSwitch.setChecked(!textLengthSwitch.isChecked());
            }
        });

        setSwitchState(sortPreference.getField());

        sortBy_subtitle.setText(fields[sortPreference.getField()]);
        Pref_sortBy.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new MaterialDialog.Builder(getContext())
                        .items(fields)
                        .title("Sort items by")
                        .itemsCallbackSingleChoice(sortPreference.getField(), new MaterialDialog.ListCallbackSingleChoice(){
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text){
                                sortPreference.setField(which);
                                sortBy_subtitle.setText(fields[which]);
                                setSwitchState(which);
                                if(sortPreferenceUpdateListener != null){
                                    sortPreferenceUpdateListener.onSortByPrefChange(which);
                                }
                                return false;
                            }
                        }).show();
            }
        });

        textLengthSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                sortPreference.setStringLength(isChecked);
                if(sortPreferenceUpdateListener != null){
                    sortPreferenceUpdateListener.onTextLengthSwitchChange(isChecked);
                }
            }
        });

        Pref_sortingOrder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sortingSwitch.setChecked(!sortingSwitch.isChecked());
            }
        });

        sortingSwitch.setChecked(sortPreference.isInAscendingOrder());
        sortingOrder_subtitle.setText((sortPreference.isInAscendingOrder()) ? "Ascending order" : "Descending order");
        sortingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                sortPreference.setInAscendingOrder(isChecked);
                sortingOrder_subtitle.setText((isChecked) ? "Ascending order" : "Descending order");
                if(sortPreferenceUpdateListener != null){
                    sortPreferenceUpdateListener.onSortOrderSwitchChange(isChecked);
                }
            }
        });
    }

    public void setSortPreferenceUpdateListener(SortPreferenceUpdateListener sortPreferenceUpdateListener){
        this.sortPreferenceUpdateListener = sortPreferenceUpdateListener;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putParcelable("sortPreference", sortPreference);
    }

    @Override
    public void onResume(){
        super.onResume();
        sortPreference = SortPreference.loadFromSharedPreference(getActivity());
        setOnClick();
    }

    @Override
    public void onPause(){
        super.onPause();
        SortPreference.saveToSharedPreference(getActivity(), sortPreference);
    }

    public interface SortPreferenceUpdateListener{
        void onSortByPrefChange(int field);

        void onTextLengthSwitchChange(boolean isChecked);

        void onSortOrderSwitchChange(boolean isChecked);
    }
}
