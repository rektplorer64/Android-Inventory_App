package tanawinwichitcom.android.inventoryapp.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import es.dmoral.toasty.Toasty;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class ItemProfileDialogFragment extends DialogFragment{

    public ItemProfileDialogFragment(){
        setRetainInstance(true);
    }


    public static ItemProfileDialogFragment newInstance(int itemId){
        Bundle args = new Bundle();
        args.putInt("itemId", itemId);
        ItemProfileDialogFragment fragment = new ItemProfileDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        // dismissAllowingStateLoss();
        // dismiss();
        // getDialog().cancel();
        // getDialog().create();
        // getDialog().show();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode){
        // dismissAllowingStateLoss();
        super.onMultiWindowModeChanged(isInMultiWindowMode);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        WindowManager.LayoutParams wmLayoutParams = getDialog().getWindow().getAttributes();
        wmLayoutParams.gravity = Gravity.CENTER;    // Adjusts Dialog Position (Left, Mid, Right)

        getDialog().setCanceledOnTouchOutside(false);
        View rootView = inflater.inflate(R.layout.dialog_item_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        int itemId = bundle.getInt("itemId");
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        ItemProfileFragment itemProfileFragment = ItemProfileFragment.newInstance(R.layout.fragment_profile_item, itemId,
                0, 0);

        itemProfileFragment.setItemChangeListener(new ItemProfileFragment.ItemChangeListener(){
            @Override
            public void onItemNotFound(int itemId){
                getDialog().dismiss();
            }
        });

        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.add(R.id.itemProfileFrame, itemProfileFragment, "itemProfileDialog");
        fragmentTransaction.commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        // Set dialog dimension (Being in onResume() is a must)
        int dialogWidth = HelperUtility.dpToPx(450, getContext());
        int dialogHeight = HelperUtility.dpToPx(750, getContext());
        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }
}
