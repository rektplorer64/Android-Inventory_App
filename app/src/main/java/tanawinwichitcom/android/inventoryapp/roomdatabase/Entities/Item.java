package tanawinwichitcom.android.inventoryapp.roomdatabase.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.ColorInt;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import tanawinwichitcom.android.inventoryapp.roomdatabase.DateConverter;
import tanawinwichitcom.android.inventoryapp.roomdatabase.FileConverter;
import tanawinwichitcom.android.inventoryapp.roomdatabase.TagsConverter;

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

    @TypeConverters(TagsConverter.class)
    @ColumnInfo(name = "tags")
    private Set<String> tags;

    @Ignore
    public boolean showing;
    @Ignore
    public boolean selected;

    public Item(String name, int quantity, String description, @ColorInt int itemColorAccent, Set<String> tags, File imageFile, Date dateCreated, Date dateModified){
        this.name = name;
        this.quantity = quantity;
        this.description = description;
        this.imageFile = imageFile;
        this.dateCreated = dateCreated;
        this.dateModified = dateModified;
        this.itemColorAccent = itemColorAccent;
        this.tags = tags;
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
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

    public Set<String> getTags(){
        return tags;
    }

    public void setTags(Set<String> tags){
        this.tags = tags;
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
        if(quantity != item.quantity){
            return false;
        }
        if(itemColorAccent != item.itemColorAccent){
            return false;
        }
        if(!name.equals(item.name)){
            return false;
        }
        if(!description.equals(item.description)){
            return false;
        }

        if(imageFile == null || item.imageFile == null){
            return false;
        }

        if(!imageFile.equals(item.imageFile)){
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
