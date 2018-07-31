package tanawinwichitcom.android.inventoryapp.rvadapters.itemdecoration;


import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tanawinwichitcom.android.inventoryapp.utility.HelperUtility;

public class MarginItemDecoration extends RecyclerView.ItemDecoration{

    private int margin;
    private int column;

    /**
     * Constructor for MarginItemDecoration class
     *
     * @param context of activity
     * @param margin  in DP
     */
    public MarginItemDecoration(Context context, int margin, int column){
        if(column < 1){
            throw new IllegalArgumentException("Column can't be lower than 1, current value is " + column);
        }
        this.column = column;
        this.margin = HelperUtility.dpToPx(margin, context);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
        super.getItemOffsets(outRect, view, parent, state);
        if(parent.getChildAdapterPosition(view) < column){
            outRect.top = margin;
        }
        outRect.left = margin;
        outRect.right = margin;
        outRect.bottom = margin;
    }
}
