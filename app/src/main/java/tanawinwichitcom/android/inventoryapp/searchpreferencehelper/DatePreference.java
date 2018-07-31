package tanawinwichitcom.android.inventoryapp.searchpreferencehelper;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class DatePreference extends SwitchablePreference implements Parcelable{
    private Date date;
    private FilterPreference.DateType dateType;

    public DatePreference(Date date){
        this.date = date;
    }

    public Date getDate(){
        return date;
    }

    public void setDate(Date date){
        this.date = date;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder("DatePreference{");
        sb.append("date = ").append(date.getTime());
        sb.append(", isPreferenceEnabled = ").append(isPreferenceEnabled());
        sb.append('}');
        return sb.toString();
    }

    public FilterPreference.DateType getDateType(){
        return dateType;
    }

    public void setDateType(FilterPreference.DateType dateType){
        this.dateType = dateType;
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeInt(this.dateType == null ? -1 : this.dateType.ordinal());
    }

    protected DatePreference(Parcel in){
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
        int tmpDateType = in.readInt();
        this.dateType = tmpDateType == -1 ? null : FilterPreference.DateType.values()[tmpDateType];
    }

    public static final Parcelable.Creator<DatePreference> CREATOR = new Parcelable.Creator<DatePreference>(){
        @Override
        public DatePreference createFromParcel(Parcel source){
            return new DatePreference(source);
        }

        @Override
        public DatePreference[] newArray(int size){
            return new DatePreference[size];
        }
    };
}
