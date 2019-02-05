package io.rektplorer.inventoryapp.utility;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ScreenUtility{

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
        return ScreenUtility.getScreenSizeCategory(context) < ScreenUtility.SCREENSIZE_LARGE
                || ScreenUtility.getScreenOrientation(context) == ScreenUtility.SCREENORIENTATION_PORTRAIT;
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

}
