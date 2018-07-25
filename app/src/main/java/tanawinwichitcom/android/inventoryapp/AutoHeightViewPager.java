package tanawinwichitcom.android.inventoryapp;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AutoHeightViewPager extends ViewPager{
    private boolean isPagingEnabled;

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

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b){
        this.isPagingEnabled = b;
    }
}
