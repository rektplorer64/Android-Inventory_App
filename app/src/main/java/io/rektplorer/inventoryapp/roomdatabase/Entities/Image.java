package io.rektplorer.inventoryapp.roomdatabase.Entities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.util.ArithmeticUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Date;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.rektplorer.inventoryapp.roomdatabase.typeconverters.DateConverter;
import io.rektplorer.inventoryapp.roomdatabase.typeconverters.FileConverter;

@Entity(tableName = "images")
public class Image implements Parcelable{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected int id;

    ///n Field Variables
    @TypeConverters(FileConverter.class)
    @ColumnInfo(name = "imageFileUrl")
    private File imageFile;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "isHeroImage")
    private boolean isHeroImage;

    @ColumnInfo(name = "userId")
    private int userId;

    @ColumnInfo(name = "itemId")
    private int itemId;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "date_added")
    private Date dateAdded;

    @ColumnInfo(name = "aspectRatio")
    private String aspectRatio;

    public Image(File imageFile, String description, boolean isHeroImage, int userId, int itemId,
                 Date dateAdded){
        this.imageFile = imageFile;
        this.description = description;
        this.isHeroImage = isHeroImage;
        this.userId = userId;
        this.itemId = itemId;
        this.dateAdded = dateAdded;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

    public File getImageFile(){
        return imageFile;
    }

    public void setImageFile(File imageFile){
        this.imageFile = imageFile;
        this.aspectRatio = calculateAspectRatio(imageFile);
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public boolean isHeroImage(){
        return isHeroImage;
    }

    public void setHeroImage(boolean heroImage){
        isHeroImage = heroImage;
    }

    public int getUserId(){
        return userId;
    }

    public void setUserId(int userId){
        this.userId = userId;
    }

    public int getItemId(){
        return itemId;
    }

    public void setItemId(int itemId){
        this.itemId = itemId;
    }

    public Date getDateAdded(){
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded){
        this.dateAdded = dateAdded;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(imageFile)
                .append(description)
                .append(userId)
                .append(itemId)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }

        if(o == null || getClass() != o.getClass()){
            return false;
        }

        Image image = (Image) o;

        return new EqualsBuilder()
                .append(id, image.id)
                .append(userId, image.userId)
                .append(itemId, image.itemId)
                .append(imageFile, image.imageFile)
                .append(description, image.description)
                .isEquals();
    }

    public String getAspectRatio(){
        // if(aspectRatio == null){
        //     aspectRatio = calculateAspectRatio(imageFile);
        // }
        return aspectRatio;
    }

    public void setAspectRatio(String aspectRatio){
        this.aspectRatio = aspectRatio;
    }

    public static String calculateAspectRatio(File imageFile){
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = 5;

        Bitmap bitmap = BitmapFactory
                .decodeFile(imageFile.getPath(), bitmapOptions);      /* Gets Bitmap from file */

        int widthRatio = 1, heightRatio = 1;
        if(bitmap != null){
            int width = bitmap.getWidth(), height = bitmap.getHeight();
            int resolutionGcd = ArithmeticUtils.gcd(width, height);
            widthRatio = width / resolutionGcd;
            heightRatio = height / resolutionGcd;

        }
        return widthRatio + ":" + heightRatio;
    }

    @Override
    public int describeContents(){ return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags){
        dest.writeInt(this.id);
        dest.writeString(this.imageFile.getPath());
        dest.writeString(this.description);
        dest.writeByte(this.isHeroImage ? (byte) 1 : (byte) 0);
        dest.writeInt(this.userId);
        dest.writeInt(this.itemId);
        dest.writeLong(this.dateAdded != null ? this.dateAdded.getTime() : -1);
        dest.writeString(this.aspectRatio);
    }

    protected Image(Parcel in){
        this.id = in.readInt();
        String path = in.readString();
        if(path != null && !path.isEmpty()){
            this.imageFile = new File(path);
        }
        this.description = in.readString();
        this.isHeroImage = in.readByte() != 0;
        this.userId = in.readInt();
        this.itemId = in.readInt();
        long tmpDateAdded = in.readLong();
        this.dateAdded = tmpDateAdded == -1 ? null : new Date(tmpDateAdded);
        this.aspectRatio = in.readString();
    }

    public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>(){
        @Override
        public Image createFromParcel(Parcel source){return new Image(source);}

        @Override
        public Image[] newArray(int size){return new Image[size];}
    };

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder("Image{");
        sb.append("id=").append(id);
        sb.append(", imageFile=").append(imageFile);
        sb.append(", description='").append(description).append('\'');
        sb.append(", isHeroImage=").append(isHeroImage);
        sb.append(", userId=").append(userId);
        sb.append(", itemId=").append(itemId);
        sb.append(", dateAdded=").append(dateAdded);
        sb.append(", aspectRatio='").append(aspectRatio).append('\'');
        sb.append('}').append("\n");
        return sb.toString();
    }

    public static class ImageViewSizeAsyncCalculator extends AsyncTask<File, Void, String>{

        private ConstraintSet cs;
        private WeakReference<ConstraintLayout> weakConstraintLayout;
        private WeakReference<ImageView> weakImageView;
        private final Image image;

        public ImageViewSizeAsyncCalculator(ConstraintLayout cl,
                                            ImageView imageView, Image image){
            this.cs = new ConstraintSet();
            this.cs.clone(cl);
            weakConstraintLayout = new WeakReference<>(cl);
            weakImageView = new WeakReference<>(imageView);
            this.image = image;
        }

        @Override
        protected String doInBackground(File... files){
            return calculateAspectRatio(files[0]);
        }

        @Override
        protected void onPostExecute(String s){
            if(weakImageView.get() != null){
                cs.setDimensionRatio(
                        weakConstraintLayout.get().getViewById(weakImageView.get().getId()).getId(),
                        s);
                cs.applyTo(weakConstraintLayout.get());
                weakConstraintLayout.get().setConstraintSet(cs);
                Glide.with(weakImageView.get().getContext())
                     .load(image.getImageFile())
                     .transition(DrawableTransitionOptions.withCrossFade()).thumbnail(0.01f)
                     .into(weakImageView.get());
            }
            image.setAspectRatio(s);
            super.onPostExecute(s);
        }
    }
}
