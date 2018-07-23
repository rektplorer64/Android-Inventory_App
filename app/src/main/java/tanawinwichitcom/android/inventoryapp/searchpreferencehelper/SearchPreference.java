package tanawinwichitcom.android.inventoryapp.searchpreferencehelper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import tanawinwichitcom.android.inventoryapp.R;

import static java.lang.annotation.RetentionPolicy.CLASS;

public class SearchPreference implements Parcelable{

    public enum DateType{DateCreated_From, DateCreated_To, DateModified_From, DateModified_To}
    public enum SearchBy{ItemName, ItemId, ItemDescription}

    public static final String SEARCH_ALL_ITEMS = "___@AllEntry";

    @Retention(CLASS)
    @IntDef({ANY_IMAGE, CONTAINS_IMAGE, NO_IMAGE})
    public @interface ImageMode{}
    public static final int ANY_IMAGE = 0;
    public static final int CONTAINS_IMAGE = 1;
    public static final int NO_IMAGE = 2;

    private SearchBy searchByPref;
    private int imageMode;
    private ArrayList<DatePreference> datePreferenceLists = new ArrayList<>();

    private QuantityPreference quantityPreference;

    private String keyword;

    public SearchPreference(){
        // Set Default Preferences
        Date currentTime = Calendar.getInstance().getTime();
        for(int i = 0; i < DateType.values().length; i++){
            datePreferenceLists.add(new DatePreference(currentTime));
            datePreferenceLists.get(i).setDateType(DateType.values()[i]);
            // System.out.println("#" + i + ", " + datePreferenceLists.get(i).getDateType().toString());
        }
        searchByPref = SearchBy.ItemName;
        imageMode = 0;
        quantityPreference = new QuantityPreference();
    }

    public void setDatePreference(DateType dateType, Date date){
        for(int i = 0; i < DateType.values().length; i++){
            if(datePreferenceLists.get(i).getDateType() == dateType){
                datePreferenceLists.get(i).setDate(date);
                break;
            }
        }
    }

    public DatePreference getDatePreference(DateType dateType){
        DatePreference datePreference = null;
        for(int i = 0; i < DateType.values().length; i++){
            if(datePreferenceLists.get(i).getDateType() == dateType){
                datePreference = datePreferenceLists.get(i);
                break;
            }
        }
        return datePreference;
    }

    public SearchBy getSearchBy(){
        return searchByPref;
    }

    public void setSearchBy(SearchBy searchBy){
        if(searchBy != null){
            this.searchByPref = searchBy;
        }else{
            this.searchByPref = SearchBy.ItemName;
        }
    }

    public int getImageMode(){
        return imageMode;
    }

    public void setImageMode(@ImageMode int imageMode){
        this.imageMode = imageMode;
    }

    public String getKeyword(){
        return keyword;
    }

    public void setKeyword(String keyword){
        this.keyword = keyword;
    }

    public QuantityPreference getQuantityPreference(){
        return quantityPreference;
    }

    public static SearchPreference loadFromSharedPreference(Context c){
        SharedPreferences sharedPref = c.getSharedPreferences(c.getString(R.string.pref_search_filter), Context.MODE_PRIVATE);

        SearchPreference searchPref = new SearchPreference();
        searchPref.setSearchBy(SearchBy.values()[sharedPref.getInt(c.getString(R.string.pref_title_search_search_by), 0)]);

        int dateValueStringRes[] = new int[]{R.string.pref_value_date_created_from, R.string.pref_value_date_created_to, R.string.pref_value_date_modified_from, R.string.pref_value_date_modified_to};
        int dateBooleanStringRes[] = new int[]{R.string.pref_value_date_created_from_switch, R.string.pref_value_date_created_to_switch, R.string.pref_value_date_modified_from_switch, R.string.pref_value_date_modified_to_switch};
        int i = 0;
        for(DateType d : DateType.values()){
            Date date = new Date();
            date.setTime(sharedPref.getLong(c.getString(dateValueStringRes[i]), Calendar.getInstance().getTimeInMillis()));
            searchPref.setDatePreference(d, date);
            searchPref.getDatePreference(d).setPreferenceEnabled(sharedPref.getBoolean(c.getString(dateBooleanStringRes[i++]), false));
        }

        searchPref.setImageMode(sharedPref.getInt(c.getString(R.string.pref_title_search_image), 0));

        searchPref.getQuantityPreference().setPreferenceEnabled(sharedPref.getBoolean(c.getString(R.string.pref_title_search_quantity), false));
        searchPref.getQuantityPreference().setMinRange(sharedPref.getInt(c.getString(R.string.pref_value_search_quantity_from), 1));
        searchPref.getQuantityPreference().setMaxRange(sharedPref.getInt(c.getString(R.string.pref_value_search_quantity_to), 1000));

        return searchPref;
    }

    public static void saveToSharedPreference(Context c, SearchPreference searchPref){
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.pref_search_filter), Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.clear();

        e.putInt(c.getString(R.string.pref_title_search_search_by), searchPref.getSearchBy().ordinal());
        e.putString(c.getString(R.string.pref_title_keyword), SEARCH_ALL_ITEMS);

        int dateValueStringRes[] = new int[]{R.string.pref_value_date_created_from, R.string.pref_value_date_created_to, R.string.pref_value_date_modified_from, R.string.pref_value_date_modified_to};
        int dateBooleanStringRes[] = new int[]{R.string.pref_value_date_created_from_switch, R.string.pref_value_date_created_to_switch, R.string.pref_value_date_modified_from_switch, R.string.pref_value_date_modified_to_switch};
        int i = 0;
        for(DateType d : DateType.values()){
            e.putLong(c.getString(dateValueStringRes[i]), searchPref.getDatePreference(d).getDate().getTime());
            e.putBoolean(c.getString(dateBooleanStringRes[i++]), searchPref.getDatePreference(d).isPreferenceEnabled());
        }

        e.putInt(c.getString(R.string.pref_title_search_image), searchPref.getImageMode());

        e.putBoolean(c.getString(R.string.pref_title_search_quantity), searchPref.getQuantityPreference().isPreferenceEnabled());
        e.putInt(c.getString(R.string.pref_value_search_quantity_from), searchPref.getQuantityPreference().getMinRange());
        e.putInt(c.getString(R.string.pref_value_search_quantity_to), searchPref.getQuantityPreference().getMaxRange());

        e.commit();
    }

    @Override
    public String toString(){
        return new ToStringBuilder(this)
                .append("searchByPref", searchByPref)
                .append("imageMode", imageMode)
                .append("datePreferenceLists", datePreferenceLists)
                .append("quantityPreference", quantityPreference)
                .append("keyword", keyword)
                .toString();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(this.searchByPref == null ? -1 : this.searchByPref.ordinal());
        dest.writeInt(this.imageMode);
        dest.writeTypedList(this.datePreferenceLists);
        dest.writeParcelable(this.quantityPreference, flags);
        dest.writeString(this.keyword);
    }

    protected SearchPreference(Parcel in){
        int tmpSearchByPref = in.readInt();
        this.searchByPref = tmpSearchByPref == -1 ? null : SearchBy.values()[tmpSearchByPref];
        this.imageMode = in.readInt();
        this.datePreferenceLists = in.createTypedArrayList(DatePreference.CREATOR);
        this.quantityPreference = in.readParcelable(QuantityPreference.class.getClassLoader());
        this.keyword = in.readString();
    }

    public static final Creator<SearchPreference> CREATOR = new Creator<SearchPreference>(){
        @Override
        public SearchPreference createFromParcel(Parcel source){
            return new SearchPreference(source);
        }

        @Override
        public SearchPreference[] newArray(int size){
            return new SearchPreference[size];
        }
    };
}
