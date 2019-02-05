package io.rektplorer.inventoryapp.rvadapters.itemdecoration;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.rektplorer.inventoryapp.utility.ScreenUtility;

public class PaddingLeftDecoration extends RecyclerView.ItemDecoration{

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private final Context context;
    private final int margin;

    private Drawable divider;
    private Rect bounds = new Rect();

    public static final int LIST_COMPACT_MARGIN = 52;
    public static final int LIST_NORMAL_MARGIN = 120;

    /**
     * Constructor for PaddingLeftDecoration class
     *
     * @param context of activity
     * @param margin  in DP
     */
    public PaddingLeftDecoration(Context context, int margin){
        this.context = context;
        this.margin = margin;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        divider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * Make set divider a left margin
     *
     * @param canvas canvas
     * @param parent recyclerView
     * @param state  recyclerView state
     *
     * @see <a href="ItemDecoration in Android by Riyaz Ahamed">https://proandroiddev.com/itemdecoration-in-android-e18a0692d848</a>
     */
    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
        canvas.save();
        final int leftWithMargin = ScreenUtility.dpToPx(margin, context);
        final int right = parent.getWidth();

        final int childCount = parent.getChildCount();      // Get number of children in RecyclerView
        for(int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            // int adapterPosition = parent.getChildAdapterPosition(child);    // Position of child
            // int left = (adapterPosition == childCount - 1) ? 0 : leftWithMargin;       // If it is the last child
            int left = leftWithMargin;

            parent.getDecoratedBoundsWithMargins(child, bounds);
            final int bottom = bounds.bottom + Math.round(child.getTranslationY());
            final int top = bottom - divider.getIntrinsicHeight();
            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
        canvas.restore();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state){
        if(divider == null){
            outRect.set(0, 0, 0, 0);
            return;
        }
        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
    }
}