package tanawinwichitcom.android.inventoryapp.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.SearchActivity;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SearchPreference;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

import static tanawinwichitcom.android.inventoryapp.fragments.SearchPreferenceFragment.SEARCH_PREF;

public class SearchOptionDialogFragment extends DialogFragment{

    private SearchPreferenceFragment.SearchPreferenceUpdateListener searchPreferenceUpdateListener;
    private SortPreferenceFragment.SortPreferenceUpdateListener sortPreferenceUpdateListener;

    private SearchPreferenceFragment searchPrefFragment;
    private SortPreferenceFragment sortPrefFragment;


    private ScrollView frameLayoutsScroller;

    public SearchOptionDialogFragment(){}

    public static SearchOptionDialogFragment newInstance(SearchPreference searchPreference){
        Bundle args = new Bundle();
        SearchOptionDialogFragment fragment = new SearchOptionDialogFragment();
        args.putParcelable(SEARCH_PREF, searchPreference);
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

        searchPrefFragment = (SearchPreferenceFragment) getChildFragmentManager().findFragmentByTag(SearchActivity.TAG_FILTER_PREF_FRAGMENT);
        if(searchPrefFragment == null){
            searchPrefFragment = SearchPreferenceFragment.newInstance();
        }

        sortPrefFragment = (SortPreferenceFragment) getChildFragmentManager().findFragmentByTag(SearchActivity.TAG_SORT_PREF_FRAGMENT);
        if(sortPrefFragment == null){
            sortPrefFragment = SortPreferenceFragment.newInstance();
        }

        searchPrefFragment.setSearchPreferenceUpdateListener(searchPreferenceUpdateListener);
        sortPrefFragment.setSortPreferenceUpdateListener(sortPreferenceUpdateListener);

        ft.replace(R.id.filterSettingFrame_dialog, searchPrefFragment, SearchActivity.TAG_FILTER_PREF_FRAGMENT);
        ft.replace(R.id.sortSettingFrame_dialog, sortPrefFragment, SearchActivity.TAG_SORT_PREF_FRAGMENT);
        ft.commit();

        view.findViewById(R.id.dialogCloseButton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                SearchPreference.saveToSharedPreference(getActivity(), searchPrefFragment.getSearchPreference());
                Toasty.success(getActivity(), "Search preferences saved!").show();
                getDialog().dismiss();
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        // Set dialog dimension (Being in onResume() is a must)
        int dialogWidth = HelperUtility.dpToPx(380, getContext());
        int dialogHeight = HelperUtility.dpToPx(750, getContext());
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

        frameLayoutsScroller.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, HelperUtility.dpToPx(750 - 82, getContext())));
    }

    public void setSearchPreferenceUpdateListener(SearchPreferenceFragment.SearchPreferenceUpdateListener searchPreferenceUpdateListener){
        this.searchPreferenceUpdateListener = searchPreferenceUpdateListener;
    }

    public void setSortPreferenceUpdateListener(SortPreferenceFragment.SortPreferenceUpdateListener sortPreferenceUpdateListener){
        this.sortPreferenceUpdateListener = sortPreferenceUpdateListener;
    }
}
