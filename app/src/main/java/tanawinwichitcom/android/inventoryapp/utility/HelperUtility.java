package tanawinwichitcom.android.inventoryapp.utility;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.view.ViewGroup;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class HelperUtility{

    public final static int SCREENSIZE_SMALL = 0;
    public final static int SCREENSIZE_NORMAL = 1;
    public final static int SCREENSIZE_LARGE = 2;
    public final static int SCREENSIZE_XLARGE = 3;
    public final static int SCREENSIZE_UNDEFINED = 4;

    public final static int SCREENORIENTATION_LANDSCAPE = 0;
    public final static int SCREENORIENTATION_PORTRAIT = 1;
    public final static int SCREENORIENTATION_UNDEFINED = 2;


    public static int getScreenSizeCategory(Context context){
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch(screenLayout){
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return SCREENSIZE_SMALL;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return SCREENSIZE_NORMAL;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return SCREENSIZE_LARGE;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                return SCREENSIZE_XLARGE;
            default:
                return SCREENSIZE_UNDEFINED;
        }
    }

    public static int getScreenOrientation(Context context){
        int screenLayout = context.getResources().getConfiguration().orientation;

        switch(screenLayout){
            case Configuration.ORIENTATION_LANDSCAPE:
                return SCREENORIENTATION_LANDSCAPE;
            case Configuration.ORIENTATION_PORTRAIT:
                return SCREENORIENTATION_PORTRAIT;
            default:
                return SCREENORIENTATION_UNDEFINED;
        }
    }

    public static boolean isScreenLargeOrPortrait(Context context){
        return HelperUtility.getScreenSizeCategory(context) < HelperUtility.SCREENSIZE_LARGE
                || HelperUtility.getScreenOrientation(context) == HelperUtility.SCREENORIENTATION_PORTRAIT;
    }

    /**
     * Converts a long number into one of these manner
     * 1000 to 1k
     * 5821 to 5.8k
     * 10500 to 10k
     * 101800 to 101k
     * 2000000 to 2m
     * 7800000 to 7.8m
     * 92150000 to 92m
     * 123200000 to 123m
     *
     * @param value a long value
     *
     * @return formatted string
     */
    public static String shortenNumber(long value){
        final NavigableMap<Long, String> suffixes = new TreeMap<>();
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");

        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if(value == Long.MIN_VALUE){
            return shortenNumber(Long.MIN_VALUE + 1);
        }
        if(value < 0){
            return "-" + shortenNumber(-value);
        }
        if(value < 1000){
            return Long.toString(value); //deal with easy case
        }

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static int dpToPx(int dps, Context context){
        return Math.round(context.getResources().getDisplayMetrics().density * dps);
    }

    public static Locale getCurrentLocale(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        }else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static int getStatusBarHeight(Context context){
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId > 0){
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void expandActionBarToFitStatusBar(Toolbar toolbar, Context context){
        // Sets the action bar's content padding, offset equal to height of the status bar
        toolbar.setPadding(0, HelperUtility.getStatusBarHeight(context), 0, 0);

        // Gets action bar's height (ActionBar.getHeight() is useless)
        TypedValue tv = new TypedValue();
        int toolBarHeight = 0;
        if(context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
            toolBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }

        // Sets action bar a new height (Height of Status bar + origin Height of Action bar)
        ViewGroup.LayoutParams layoutParams = toolbar.getLayoutParams();
        layoutParams.height = HelperUtility.getStatusBarHeight(context) + toolBarHeight;
        toolbar.setLayoutParams(layoutParams);
    }
}
