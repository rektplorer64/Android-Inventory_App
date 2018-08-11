package io.rektplorer.inventoryapp.searchpreferencehelper;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class QuantityPreference extends SwitchablePreference implements Parcelable{
    private int maxRange;
    private int minRange;

    public QuantityPreference(){
    }

    public int getMaxRange(){
        return maxRange;
    }

    public void setMaxRange(int maxRange){
        this.maxRange = maxRange;
    }

    public int getMinRange(){
        return minRange;
    }

    public void setMinRange(int minRange){
        this.minRange = minRange;
    }

    @Override
    public String toString(){
        return new ToStringBuilder(this)
                .append("maxRange", maxRange)
                .append("minRange", minRange)
                .toString();
    }

    @Override
    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(this.maxRange);
        dest.writeInt(this.minRange);
    }

    protected QuantityPreference(Parcel in){
        this.maxRange = in.readInt();
        this.minRange = in.readInt();
    }

    public static final Parcelable.Creator<QuantityPreference> CREATOR = new Parcelable.Creator<QuantityPreference>(){
        @Override
        public QuantityPreference createFromParcel(Parcel source){
            return new QuantityPreference(source);
        }

        @Override
        public QuantityPreference[] newArray(int size){
            return new QuantityPreference[size];
        }
    };
}
