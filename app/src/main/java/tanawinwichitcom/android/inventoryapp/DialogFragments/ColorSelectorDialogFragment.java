package tanawinwichitcom.android.inventoryapp.DialogFragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;

import tanawinwichitcom.android.inventoryapp.AddItemActivity;
import tanawinwichitcom.android.inventoryapp.AddItemActivity.SelectableColor;
import tanawinwichitcom.android.inventoryapp.R;
import tanawinwichitcom.android.inventoryapp.RecycleViewAdapters.ColorAdapter;

public class ColorSelectorDialogFragment extends DialogFragment{

    private Context mContext;
    private ArrayList<SelectableColor> selectableColorArrayList;
    private ArrayList<SelectableColor> originalColorArrayList;
    private ImageButton circleImageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        return inflater.inflate(R.layout.dialog_colorpicker, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();

        RecyclerView colorGrid = view.findViewById(R.id.colorGrid);
        colorGrid.setAdapter(new ColorAdapter(mContext, selectableColorArrayList));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(mContext, 4);
        colorGrid.setLayoutManager(layoutManager);
        colorGrid.setHasFixedSize(true);

        Button confirmButton = view.findViewById(R.id.colorConfirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                dismiss();
            }
        });

        Button cancelButton = view.findViewById(R.id.colorCancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                selectableColorArrayList.clear();
                selectableColorArrayList.addAll(originalColorArrayList);
                circleImageView.setBackgroundColor(AddItemActivity.getSelectedColor(mContext));
                dismiss();
            }
        });

    }

    /**
     * This method assigns onDismiss() method from AddItemActivity to this DialogFragment
     *
     * @param dialog dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog){
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if(activity instanceof DialogInterface.OnDismissListener){        /* If the activity implements DialogInterface.OnDismissListener */
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);       /* Runs onDismiss() of the activity */
        }
    }

    public void putArguments(ArrayList<SelectableColor> selectableColorArrayList, ImageButton circleImageView){
        this.selectableColorArrayList = selectableColorArrayList;
        this.originalColorArrayList = AddItemActivity.SelectableColor.copyArrayList(selectableColorArrayList);
        this.circleImageView = circleImageView;
    }

    public static int darkenColor(@ColorInt int colorInt){
        float[] hsv = new float[3];
        Color.colorToHSV(colorInt, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }
}
