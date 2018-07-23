package tanawinwichitcom.android.inventoryapp.fragments;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import tanawinwichitcom.android.inventoryapp.ItemEditingFragment;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class ItemEditingDialogFragment extends DialogFragment{

    private OnDialogConfirmListener onDialogConfirmListener;

    public ItemEditingDialogFragment(){
        setRetainInstance(true);
    }

    public static ItemEditingDialogFragment newInstance(int itemId, boolean inEditMode){
        Bundle args = new Bundle();
        args.putInt("itemId", itemId);
        args.putBoolean("inEditMode", inEditMode);
        ItemEditingDialogFragment fragment = new ItemEditingDialogFragment();
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
        boolean inEditMode = bundle.getBoolean("inEditMode");
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        ItemEditingFragment itemEditingFragment = ItemEditingFragment.newInstance(itemId, inEditMode);

        itemEditingFragment.setOnConfirmListener(new ItemEditingFragment.OnConfirmListener(){
            @Override
            public void onConfirm(int itemId){
                getDialog().dismiss();
                if(onDialogConfirmListener != null){
                    onDialogConfirmListener.onDialogConfirm(itemId);
                }
            }
        });

        fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        fragmentTransaction.add(R.id.itemProfileFrame, itemEditingFragment, "itemEditingFragment");
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

    public void setOnDialogConfirmListener(OnDialogConfirmListener onDialogConfirmListener){
        this.onDialogConfirmListener = onDialogConfirmListener;
    }

    public interface OnDialogConfirmListener{
        void onDialogConfirm(int itemId);
    }
}
