package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

@Entity(tableName = "reviews")
public class Review implements Parcelable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected int id;

    @ColumnInfo(name = "timeStamp")
    private String timeStamp;

    @ColumnInfo(name = "userId")
    private int userId;

    @ColumnInfo(name = "itemId")
    private int itemId;

    @ColumnInfo(name = "commentString")
    private String comment;

    @ColumnInfo(name = "ratingValue")
    private double rating;

    public Review(String timeStamp, int userId, int itemId, String comment, double rating){
        this.timeStamp = timeStamp;
        this.userId = userId;
        this.itemId = itemId;
        this.comment = comment;
        this.rating = rating;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp){
        this.timeStamp = timeStamp;
    }

    public int getUserId(){
        return userId;
    }

    public void setUserId(int userId){
        this.userId = userId;
    }

    public String getComment(){
        return comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public double getRating(){
        return rating;
    }

    public void setRating(double rating){
        this.rating = rating;
    }

    public int getItemId(){
        return itemId;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public static double calculateAverage(List<Review> reviewArrayList){

        // If there is no review for the item
        if(reviewArrayList == null || reviewArrayList.isEmpty()){
            return 0.0;     /* The average score is always zero */
        }

        double summation = 0.0;
        for(Review review : reviewArrayList){
            summation += review.rating;
        }

        double result = summation / reviewArrayList.size();
        if(Double.isNaN(result)){
            return 0.0;
        }else{
            return result;
        }
    }

    protected Review(Parcel in){
        id = in.readInt();
        timeStamp = in.readString();
        userId = in.readInt();
        itemId = in.readInt();
        comment = in.readString();
        rating = in.readDouble();
    }

    @Override
    public int describeContents(){
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(id);
        dest.writeString(timeStamp);
        dest.writeInt(userId);
        dest.writeInt(itemId);
        dest.writeString(comment);
        dest.writeDouble(rating);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Review> CREATOR = new Parcelable.Creator<Review>(){
        @Override
        public Review createFromParcel(Parcel in){
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size){
            return new Review[size];
        }
    };
}
