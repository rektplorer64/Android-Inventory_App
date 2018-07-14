package tanawinwichitcom.android.inventoryapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;

public class ColorUtility{
    public static int darkenColor(@ColorInt int colorInt){
        float[] hsv = new float[3];
        Color.colorToHSV(colorInt, hsv);
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static int getSuitableFrontColor(Context context, Integer backgroundColor, boolean paramIsColorInt){
        int color;      /* Decode Hex String into a Color integer */
        if(!paramIsColorInt){
            String colorString = context.getResources().getString(backgroundColor);     /* Gets Hex Color from given resource id*/
            color = Color.parseColor(colorString);
        }else{
            color = backgroundColor;
        }

        int red = Color.red(color);     /* Extracts Red Channel Value from the Color integer */
        int green = Color.green(color);     /* Extracts Green Channel Value from the Color integer */
        int blue = Color.blue(color);       /* Extracts Blue Channel Value from the Color integer */

        // System.out.println("Param:" + backgroundColor);
        // System.out.println("Param Value: " + color);
        // System.out.println("RGB: " + red + " " + green + " " + blue);

        double greyValue = (red * 0.299) + (green * 0.587) + (blue * 0.114);

        if(greyValue < 186){       /* Simple Algorithm for determining suitable foreground color */
            // White
            red = 255;
            green = 255;
            blue = 255;
        }else{
            // Black
            red = 0;
            green = 0;
            blue = 0;
        }
        // System.out.println("Returns: " + Color.rgb(red, green, blue));
        return Color.rgb(red, green, blue);     /* Converts RGB to an Integer color value*/
    }
}
