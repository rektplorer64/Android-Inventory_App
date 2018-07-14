package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.ColorInt;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DateConverter;
import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.FileConverter;

@Entity(tableName = "items")
public class Item{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "rating")
    private Double rating;

    @TypeConverters(FileConverter.class)
    @ColumnInfo(name = "imagePath")
    private File imageFile;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "date_created")
    private Date dateCreated;

    @TypeConverters(DateConverter.class)
    @ColumnInfo(name = "date_modified")
    private Date dateModified;

    @ColumnInfo(name = "color")
    private int itemColorAccent;

    @ColumnInfo(name = "tags")
    private String tags;

    public Item(String name, int quantity, String description, @ColorInt int itemColorAccent, String tags, File imageFile, Date dateCreated, Date dateModified){
        this.name = name;
        this.quantity = quantity;
        this.description = description;
        this.imageFile = imageFile;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.itemColorAccent = itemColorAccent;
        this.tags = tags;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setTags(String tags){
        this.tags = tags;
    }

    public int getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getQuantity(){
        return quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public Double getRating(){
        return rating;
    }

    public void setRating(Double rating){
        this.rating = rating;
    }

    public File getImageFile(){
        return imageFile;
    }

    public void setImageFile(File imageFile){
        this.imageFile = imageFile;
    }

    public Date getDateCreated(){
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated){
        this.dateCreated = dateCreated;
    }

    public Date getDateModified(){
        return dateModified;
    }

    public void setDateModified(Date dateModified){
        this.dateModified = dateModified;
    }

    public int getItemColorAccent(){
        return itemColorAccent;
    }

    public void setItemColorAccent(int itemColorAccent){
        this.itemColorAccent = itemColorAccent;
    }

    public String getTags(){
        return tags;
    }

    public HashSet<String> getTagsSet(){
        String[] spitted = this.tags.split(" ");
        return new HashSet<>(Arrays.asList(spitted));
    }

    public void addTag(String tag){
        StringBuilder stringBuilder = new StringBuilder(this.tags);
        stringBuilder.append(" ").append(tags);
        this.tags = stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o){
        if(this == o){
            return true;
        }
        if(o == null || getClass() != o.getClass()){
            return false;
        }

        Item item = (Item) o;

        if(id != item.id){
            return false;
        }
        return dateCreated.equals(item.dateCreated);
    }

    @Override
    public int hashCode(){
        int result = id;
        result = 31 * result + dateCreated.hashCode();
        return result;
    }
}
