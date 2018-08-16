package io.rektplorer.inventoryapp.fragments;


import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.roomdatabase.ItemViewModel;
import io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference;

public class TagSelectorDialogFragment extends DialogFragment{

    private MultiStateView tagSelectorMultiStateView;

    private ChipGroup tagChipGroup;
    private TextInputEditText tagSearch;

    private ItemViewModel itemViewModel;
    private Set<String> sortedTagList;

    private TreeSet<String> tempTagsSet;

    private AppCompatTextView totalTagSelectedView;

    private Button dialogClearTagsButton;

    private DismissListener dismissListener;

    private FilterPreference filterPreference;

    public TagSelectorDialogFragment(){
    }

    public static TagSelectorDialogFragment newInstance(){
        return new TagSelectorDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_tag_selector, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(getDialog().getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;       // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(lWindowParams);

        tagSelectorMultiStateView = view.findViewById(R.id.tagSelectorMultiStateView);
        tagChipGroup = view.findViewById(R.id.tagChipGroup);
        tagSearch = view.findViewById(R.id.tagSearch);
        totalTagSelectedView = view.findViewById(R.id.totalTagSelectedView);

        dialogClearTagsButton = view.findViewById(R.id.dialogClearTagsButton);

        view.findViewById(R.id.dialogDoneButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                dismiss();
            }
        });

        dialogClearTagsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                for(int i = 0; i < tagChipGroup.getChildCount(); i++){
                    if(((Chip) tagChipGroup.getChildAt(i)).isChecked()){
                        ((Chip) tagChipGroup.getChildAt(i)).setChecked(false);
                        tagChipGroup.getChildAt(i).setSelected(false);
                    }
                }
            }
        });

        filterPreference = FilterPreference.loadFromSharedPreference(getContext());

        tempTagsSet = new TreeSet<>(filterPreference.getTagList());
        dialogClearTagsButton.setEnabled(tempTagsSet.size() != 0);

        itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);
        sortedTagList = itemViewModel.getAllTags();

        if(sortedTagList.size() != 0){
            tagSelectorMultiStateView.setViewState(MultiStateView.VIEW_STATE_CONTENT);

            populateTagChipGroup(null, false, true);
            totalTagSelectedView.setText(
                    new StringBuilder().append("TOTAL SELECTED: ").append(tempTagsSet.size())
                                       .toString());

            tagSearch.addTextChangedListener(new TextWatcher(){
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                    if(!charSequence.toString().isEmpty()){
                        populateTagChipGroup(charSequence.toString(), false, false);
                    }else{
                        populateTagChipGroup(null, false, true);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable){

                }
            });
        }else{
            tagSelectorMultiStateView.setViewState(MultiStateView.VIEW_STATE_EMPTY);
        }
    }

    public void reInstantiateTagSet(){
        tempTagsSet = new TreeSet<>();
    }

    private void populateTagChipGroup(CharSequence query, boolean limitSize,
                                      boolean showOnlyCheckedChips){
        tagChipGroup.removeAllViews();
        new TagSelectorAsyncBuilder(tagChipGroup, query, limitSize, showOnlyCheckedChips,
                                    tempTagsSet, dialogClearTagsButton, totalTagSelectedView)
                .execute(sortedTagList.toArray(new String[0]));
        // if(tagChipGroup.getChildCount() > 0){
        //     tagChipGroup.removeAllViews();
        // }
        //
        // int iterationLimit = 0;
        // if(limitSize){
        //     if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        //         iterationLimit = Integer.min(20, sortedTagList.size());
        //     }
        // }else{
        //     iterationLimit = sortedTagList.size();
        // }
        //
        // int count = 0;
        // for(Iterator<String> iterator = sortedTagList.iterator(); iterator.hasNext(); ){
        //     String tag = iterator.next();
        //     if(query != null){
        //         if(tag.toLowerCase().contains(query.toString().toLowerCase())
        //                 || tag.equalsIgnoreCase(query.toString())){
        //             // Empty
        //         }else{
        //             continue;
        //         }
        //     }
        //
        //     if(count == iterationLimit){
        //         break;
        //     }
        //
        //     if(showOnlyCheckedChips && !tempTagsSet.contains(tag)){
        //         continue;
        //     }
        //
        //     final Chip tagChip = new Chip(getContext());
        //
        //     if(!showOnlyCheckedChips){
        //         String formattedString = tag.replaceAll(query.toString().toLowerCase(), "<b>" + query + "</b>");
        //         Spanned finalTagString;
        //         if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
        //             finalTagString = Html.fromHtml(formattedString, Html.FROM_HTML_MODE_COMPACT);
        //         }else{
        //             finalTagString = Html.fromHtml(formattedString);
        //         }
        //         tagChip.setText(finalTagString);
        //     }else{
        //         tagChip.setText(tag);
        //     }
        //     tagChip.setCheckable(true);
        //     tagChip.setCheckedIconEnabled(true);
        //
        //     tagChip.setChipBackgroundColor(getContext().getResources().getColorStateList(R.color.chip_tag_color));
        //     // tagChip.setCheckedIcon(view.getContext().getDrawable(R.drawable.ic_check_black_24dp));
        //     tagChip.setOnClickListener(new View.OnClickListener(){
        //         @Override
        //         public void onClick(View view){
        //             tagChip.setSelected(!tagChip.isSelected());
        //             // totalTagSelectedView.setText("TOTAL SELECTED: " + getCheckedCount(tagChipGroup));
        //         }
        //     });
        //
        //     tagChip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
        //         @Override
        //         public void onCheckedChanged(CompoundButton compoundButton, boolean selected){
        //             tagChip.setTextColor((selected) ? Color.WHITE : Color.BLACK);
        //             if(selected){
        //                 tempTagsSet.add(tagChip.getText().toString());
        //                 // Toasty.info(getContext(), "Selected " + tagChip.getText()).show();
        //             }else{
        //                 tempTagsSet.remove(tagChip.getText().toString());
        //                 // Toasty.info(getContext(), "Deselected " + tagChip.getText()).show();
        //             }
        //             dialogClearTagsButton.setEnabled(tempTagsSet.size() != 0);
        //             totalTagSelectedView.setText(new StringBuilder().append("TOTAL SELECTED: ").append(tempTagsSet.size()).toString());
        //         }
        //     });
        //
        //     if(tempTagsSet.contains(tagChip.getText().toString())){
        //         tagChip.setChecked(true);
        //         tagChip.setSelected(true);
        //     }
        //
        //     count++;
        //     tagChipGroup.addView(tagChip);
        // }
    }

    public List<String> getSelectedTags(){
        List<String> stringList = new ArrayList<>();
        for(int i = 0; i < tagChipGroup.getChildCount(); i++){
            if(((Chip) tagChipGroup.getChildAt(i)).isChecked()){
                Chip chip = (Chip) tagChipGroup.getChildAt(i);
                stringList.add(chip.getText().toString());
            }
        }
        return stringList;
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

    private static class TagSelectorAsyncBuilder extends AsyncTask<String, Chip, Void>{

        private final WeakReference<ChipGroup> chipGroupWeakReference;
        private final CharSequence query;
        private final boolean limitSize;
        private final boolean showOnlyCheckedChips;
        private TreeSet<String> tempTagsSet;
        private WeakReference<Button> dialogClearTagsButton;
        private WeakReference<TextView> totalTagSelectedView;

        TagSelectorAsyncBuilder(ChipGroup chipGroup, CharSequence query, boolean limitSize,
                                boolean showOnlyCheckedChips, TreeSet<String> tempTagsSet,
                                Button dialogClearTagsButton, TextView totalTagSelectedView){
            this.chipGroupWeakReference = new WeakReference<>(chipGroup);
            this.query = query;
            this.limitSize = limitSize;
            this.showOnlyCheckedChips = showOnlyCheckedChips;
            this.tempTagsSet = tempTagsSet;
            this.dialogClearTagsButton = new WeakReference<>(dialogClearTagsButton);
            this.totalTagSelectedView = new WeakReference<>(totalTagSelectedView);
        }

        @Override
        protected Void doInBackground(String... lists){
            TreeSet<String> sortedTagList = new TreeSet<>(Arrays.asList(lists));

            int iterationLimit = 0;
            if(limitSize){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    iterationLimit = Integer.min(20, sortedTagList.size());
                }
            }else{
                iterationLimit = sortedTagList.size();
            }

            int count = 0;
            for(Iterator<String> iterator = sortedTagList.iterator(); iterator.hasNext(); ){
                String tag = iterator.next();
                if(query != null){
                    if(tag.toLowerCase().contains(query.toString().toLowerCase())
                            || tag.equalsIgnoreCase(query.toString())){
                        // Empty
                    }else{
                        continue;
                    }
                }

                if(count == iterationLimit){
                    break;
                }

                if(showOnlyCheckedChips && !tempTagsSet.contains(tag)){
                    continue;
                }

                final Chip tagChip = new Chip(chipGroupWeakReference.get().getContext());

                if(!showOnlyCheckedChips){
                    String formattedString = tag
                            .replaceAll(query.toString().toLowerCase(), "<b>" + query + "</b>");
                    Spanned finalTagString;
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        finalTagString = Html
                                .fromHtml(formattedString, Html.FROM_HTML_MODE_COMPACT);
                    }else{
                        finalTagString = Html.fromHtml(formattedString);
                    }
                    tagChip.setText(finalTagString);
                }else{
                    tagChip.setText(tag);
                }
                tagChip.setCheckable(true);
                tagChip.setCheckedIconEnabled(true);

                tagChip.setChipBackgroundColor(
                        chipGroupWeakReference.get().getContext().getResources()
                                              .getColorStateList(R.color.chip_tag_color));
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
                            // Toasty.info(getContext(), "Selected " + tagChip.getText()).show();
                        }else{
                            tempTagsSet.remove(tagChip.getText().toString());
                            // Toasty.info(getContext(), "Deselected " + tagChip.getText()).show();
                        }
                        dialogClearTagsButton.get().setEnabled(tempTagsSet.size() != 0);
                        totalTagSelectedView.get().setText(
                                new StringBuilder().append("TOTAL SELECTED: ")
                                                   .append(tempTagsSet.size()).toString());
                    }
                });

                count++;
                publishProgress(tagChip);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Chip... values){
            super.onProgressUpdate(values);
            if(tempTagsSet.contains(values[0].getText().toString())){
                values[0].setChecked(true);
                values[0].setSelected(true);
            }
            chipGroupWeakReference.get().addView(values[0]);
        }
    }
}
