package tanawinwichitcom.android.inventoryapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class AutoHeightViewPager extends ViewPager{
    public AutoHeightViewPager(@NonNull Context context){
        super(context);
    }

    /**
     * This constructor is required in order to user XML attributes
     *
     * @param context context
     * @param attrs   xml attributes
     */
    public AutoHeightViewPager(@NonNull Context context, @Nullable AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int height = 0;
        for(int i = 0; i < getChildCount(); i++){
            View child = getChildAt(i);
            child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int h = child.getMeasuredHeight() + child.getPaddingTop() + child.getPaddingBottom();
            if(h > height){
                height = h;
            }
        }

        if(height != 0){
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
