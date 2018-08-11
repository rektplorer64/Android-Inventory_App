package io.rektplorer.inventoryapp.roomdatabase.Entities;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.rektplorer.inventoryapp.roomdatabase.DateConverter;
import io.rektplorer.inventoryapp.roomdatabase.FileConverter;

@Entity(tableName = "images")
public class Image{
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;

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

    public Image(File imageFile, String description, boolean isHeroImage, int userId, int itemId, Date dateAdded){
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
}
