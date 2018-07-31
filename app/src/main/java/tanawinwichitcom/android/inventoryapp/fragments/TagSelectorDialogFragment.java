package tanawinwichitcom.android.inventoryapp.fragments;


import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.kennyc.view.MultiStateView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.roomdatabase.ItemViewModel;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.FilterPreference;

public class TagSelectorDialogFragment extends DialogFragment{

    private MultiStateView tagSelectorMultiStateView;

    private ChipGroup tagChipGroup;
    private TextInputEditText tagSearch;

    private ItemViewModel itemViewModel;
    private List<String> sortedTagList;

    private HashSet<String> tempTagsSet;

    private Button dialogCloseButton;

    private TextView totalTagSelectedView;

    private DismissListener dismissListener;

    private FilterPreference filterPreference;

    public TagSelectorDialogFragment(){
    }

    public static TagSelectorDialogFragment newInstance(){
        return new TagSelectorDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_tag_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(getDialog().getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(lWindowParams);

        tempTagsSet = new HashSet<>();

        tagSelectorMultiStateView = view.findViewById(R.id.tagSelectorMultiStateView);
        tagChipGroup = view.findViewById(R.id.tagChipGroup);
        tagSearch = view.findViewById(R.id.tagSearch);
        totalTagSelectedView = view.findViewById(R.id.totalTagSelectedView);

        view.findViewById(R.id.dialogCloseButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dismiss();
            }
        });

        filterPreference = FilterPreference.loadFromSharedPreference(getContext());

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        sortedTagList = new ArrayList<>(itemViewModel.getAllTags());


        if(sortedTagList.size() != 0){
            tagSelectorMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);

            new Thread(new Runnable(){
                @Override
                public void run(){
                    Collections.sort(sortedTagList);
                }
            }).start();

            populateTagChipGroup(null, filterPreference.getTagList(), false, true);
            totalTagSelectedView.setText("TOTAL SELECTED: " + getCheckedCount(tagChipGroup));

            final FilterPreference finalFilterPreference = filterPreference;
            tagSearch.addTextChangedListener(new TextWatcher(){
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                    if(!charSequence.toString().isEmpty()){
                        populateTagChipGroup(charSequence.toString(), finalFilterPreference.getTagList(), false, false);
                    }else{
                        populateTagChipGroup(null, finalFilterPreference.getTagList(), false, true);
                    }
                    totalTagSelectedView.setText("TOTAL SELECTED: " + getCheckedCount(tagChipGroup));
                }

                @Override
                public void afterTextChanged(Editable editable){

                }
            });
        }else{
            tagSelectorMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
        }
    }

    public void resetTempTagSet(){
        tempTagsSet = new HashSet<>();
    }

    private void populateTagChipGroup(CharSequence query, final List<String> tagsPrefList, boolean limitSize, boolean showOnlyCheckedChips){
        tagChipGroup.removeAllViews();

        int iterationLimit = 0;
        if(limitSize){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                iterationLimit = Integer.min(20, sortedTagList.size());
            }
        }else{
            iterationLimit = sortedTagList.size();
        }

        for(int i = 0; i < iterationLimit; i++){
            String tag = sortedTagList.get(i);
            if(query != null){
                if(tag.toLowerCase().contains(query.toString().toLowerCase())
                        || tag.equalsIgnoreCase(query.toString())){

                }else{
                    continue;
                }
            }

            final Chip tagChip = new Chip(getContext());
            tagChip.setText(tag);
            tagChip.setCheckable(true);
            tagChip.setCheckedIconEnabled(true);

            ColorStateList colorStateList = getContext().getResources().getColorStateList(R.color.chip_tag_color);
            tagChip.setChipBackgroundColor(colorStateList);
            // tagChip.setCheckedIcon(view.getContext().getDrawable(R.drawable.ic_check_black_24dp));
            tagChip.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    tagChip.setSelected(!tagChip.isSelected());
                    // totalTagSelectedView.setText("TOTAL SELECTED: " + getCheckedCount(tagChipGroup));
                }
            });

            tagChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean selected){
                    tagChip.setTextColor((selected) ? Color.WHITE : Color.BLACK);
                    if(selected){
                        tempTagsSet.add(tagChip.getText().toString());
                    }else{
                        tempTagsSet.remove(tagChip.getText().toString());
                    }
                    totalTagSelectedView.setText("TOTAL SELECTED: " + getCheckedCount(tagChipGroup));
                }
            });

            if(showOnlyCheckedChips && !(tagsPrefList.contains(tag)
                    || tempTagsSet.contains(tagChip.getText().toString()))){
                continue;
            }

            if(tagsPrefList.contains(tag) || tempTagsSet.contains(tagChip.getText().toString())){
                tagChip.setChecked(true);
                tagChip.setSelected(true);
            }

            tagChipGroup.addView(tagChip);
        }
    }

    public List<String> getSelectedTag(){
        List<String> stringList = new ArrayList<>();
        for(int i = 0; i < tagChipGroup.getChildCount(); i++){
            if(((Chip) tagChipGroup.getChildAt(i)).isChecked()){
                Chip chip = (Chip) tagChipGroup.getChildAt(i);
                stringList.add(chip.getText().toString());
            }
        }
        return stringList;
    }

    private int getCheckedCount(ChipGroup chipGroup){
        // int checkedCount = 0;
        // for(int i = 0; i < chipGroup.getChildCount(); i++){
        //     if(((Chip) chipGroup.getChildAt(i)).isChecked()){
        //         checkedCount++;
        //     }
        // }
        return tempTagsSet.size();
    }

    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        if(dismissListener != null){
            dismissListener.onDismiss();
        }
    }

    public void setDismissListener(DismissListener dismissListener){
        this.dismissListener = dismissListener;
    }

    public interface DismissListener{
        void onDismiss();
    }
}
