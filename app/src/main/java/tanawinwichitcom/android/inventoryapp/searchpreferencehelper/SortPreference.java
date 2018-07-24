package tanawinwichitcom.android.inventoryapp.searchpreferencehelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import tanawinwichitcom.android.inventoryapp.R;

import static java.lang.annotation.RetentionPolicy.CLASS;

public class SortPreference implements Parcelable{
    @Retention(CLASS)
    @IntDef({ID, NAME, DESCRIPTION, DATE_CREATED, DATE_MODIFIED, COLOR_ACCENT, QUANTITY, RATING})
    public @interface ItemFieldType{
    }

    public static final int ID = 0;
    public static final int NAME = 1;
    public static final int DESCRIPTION = 2;
    public static final int DATE_CREATED = 3;
    public static final int DATE_MODIFIED = 4;
    public static final int COLOR_ACCENT = 5;
    public static final int QUANTITY = 6;
    public static final int RATING = 7;

    private boolean inAscendingOrder;

    private int field;

    private boolean stringLength;

    public SortPreference(){
        inAscendingOrder = true;
    }

    public boolean isInAscendingOrder(){
        return inAscendingOrder;
    }

    public void setInAscendingOrder(boolean inAscendingOrder){
        this.inAscendingOrder = inAscendingOrder;
    }

    public int getField(){
        return field;
    }

    public void setField(@ItemFieldType int field){
        this.field = field;
    }

    public boolean isStringLength(){
        return stringLength;
    }

    public void setStringLength(boolean stringLength){
        this.stringLength = stringLength;
    }

    public static void saveToSharedPreference(Context c, SortPreference sortPreference){
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.search_sort_preference), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        editor.putInt(c.getString(R.string.pref_title_sort_sort_by), sortPreference.getField());
        editor.putBoolean(c.getString(R.string.pref_title_sort_string_length), sortPreference.isStringLength());
        editor.putBoolean(c.getString(R.string.pref_value_sort_sorting_order), sortPreference.isInAscendingOrder());

        editor.commit();
    }

    public static SortPreference loadFromSharedPreference(Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.search_sort_preference), Context.MODE_PRIVATE);

        SortPreference sortPreference = new SortPreference();
        sortPreference.setField(sharedPreferences.getInt(c.getString(R.string.pref_title_sort_sort_by), 0));
        sortPreference.setStringLength(sharedPreferences.getBoolean(c.getString(R.string.pref_title_sort_string_length), false));
        sortPreference.setStringLength(sharedPreferences.getBoolean(c.getString(R.string.pref_value_sort_sorting_order), true));

        return sortPreference;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeByte(this.inAscendingOrder ? (byte) 1 : (byte) 0);
        dest.writeInt(this.field);
        dest.writeByte(this.stringLength ? (byte) 1 : (byte) 0);
    }

    protected SortPreference(Parcel in){
        this.inAscendingOrder = in.readByte() != 0;
        this.field = in.readInt();
        this.stringLength = in.readByte() != 0;
    }

    public static final Parcelable.Creator<SortPreference> CREATOR = new Parcelable.Creator<SortPreference>(){
        @Override
        public SortPreference createFromParcel(Parcel source){
            return new SortPreference(source);
        }

        @Override
        public SortPreference[] newArray(int size){
            return new SortPreference[size];
        }
    };
}
