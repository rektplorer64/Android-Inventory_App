package io.rektplorer.inventoryapp.rvadapters.itemdecoration;


import android.content.Context;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.rektplorer.inventoryapp.utility.HelperUtility;

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
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent
            , @NonNull RecyclerView.State state){
        super.getItemOffsets(outRect, view, parent, state);
        // TODO: Adjust this to suit with Grid layout
        int position = parent.getChildAdapterPosition(view) + 1;

        if(position <= column){
            outRect.top = margin;
        }else{

        }

        outRect.left = margin;
        if(position % column == 0){
            outRect.right = margin;
        }
        outRect.bottom = margin;
    }
}
