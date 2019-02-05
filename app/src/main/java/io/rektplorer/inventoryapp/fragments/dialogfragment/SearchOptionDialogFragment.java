package io.rektplorer.inventoryapp.fragments.dialogfragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import es.dmoral.toasty.Toasty;
import io.rektplorer.inventoryapp.ConstantsHolder;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.fragments.SearchPreferenceFragment;
import io.rektplorer.inventoryapp.fragments.SortPreferenceFragment;
import io.rektplorer.inventoryapp.searchpreferencehelper.FilterPreference;
import io.rektplorer.inventoryapp.utility.ScreenUtility;

import static io.rektplorer.inventoryapp.fragments.SearchPreferenceFragment.SEARCH_PREF;

public class SearchOptionDialogFragment extends DialogFragment{

    private SearchPreferenceFragment.SearchPreferenceUpdateListener searchPreferenceUpdateListener;
    private SortPreferenceFragment.SortPreferenceUpdateListener sortPreferenceUpdateListener;

    private SearchPreferenceFragment searchPrefFragment;
    private SortPreferenceFragment sortPrefFragment;


    private ScrollView frameLayoutsScroller;

    public SearchOptionDialogFragment(){}

    public static SearchOptionDialogFragment newInstance(FilterPreference filterPreference){
        Bundle args = new Bundle();
        SearchOptionDialogFragment fragment = new SearchOptionDialogFragment();
        args.putParcelable(SEARCH_PREF, filterPreference);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.dialog_search_option, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        setRetainInstance(true);

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();

        frameLayoutsScroller = view.findViewById(R.id.frameLayoutsScroller);

        searchPrefFragment = (SearchPreferenceFragment) getChildFragmentManager().findFragmentByTag(ConstantsHolder.FRAGMENT_ITEM_FILTER);
        if(searchPrefFragment == null){
            searchPrefFragment = SearchPreferenceFragment.newInstance();
        }

        sortPrefFragment = (SortPreferenceFragment) getChildFragmentManager().findFragmentByTag(ConstantsHolder.FRAGMENT_ITEM_SORT);
        if(sortPrefFragment == null){
            sortPrefFragment = SortPreferenceFragment.newInstance();
        }

        searchPrefFragment.setSearchPreferenceUpdateListener(searchPreferenceUpdateListener);
        sortPrefFragment.setSortPreferenceUpdateListener(sortPreferenceUpdateListener);

        ft.replace(R.id.filterSettingFrame_dialog, searchPrefFragment, ConstantsHolder.FRAGMENT_ITEM_FILTER);
        ft.replace(R.id.sortSettingFrame_dialog, sortPrefFragment, ConstantsHolder.FRAGMENT_ITEM_SORT);
        ft.commit();

        view.findViewById(R.id.dialogDoneButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                FilterPreference.saveToSharedPreference(getActivity(), searchPrefFragment.getSearchPreference());
                Toasty.success(getActivity(), "Search preferences saved!").show();
                getDialog().dismiss();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        // Set dialog dimension (Being in onResume() is a must)
        int dialogWidth = ScreenUtility.dpToPx(380, getContext());
        int dialogHeight = ScreenUtility.dpToPx(750, getContext());
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

        frameLayoutsScroller.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, ScreenUtility.dpToPx(750 - 82, getContext())));
    }

    public void setSearchPreferenceUpdateListener(SearchPreferenceFragment.SearchPreferenceUpdateListener searchPreferenceUpdateListener){
        this.searchPreferenceUpdateListener = searchPreferenceUpdateListener;
    }

    public void setSortPreferenceUpdateListener(SortPreferenceFragment.SortPreferenceUpdateListener sortPreferenceUpdateListener){
        this.sortPreferenceUpdateListener = sortPreferenceUpdateListener;
    }
}
