package io.rektplorer.inventoryapp.roomdatabase.Entities;


import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.rektplorer.inventoryapp.roomdatabase.typeconverters.DateConverter;
import io.rektplorer.inventoryapp.roomdatabase.typeconverters.TagsConverter;

@Entity(tableName = "items")
public class Item{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    protected int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "quantity")
    private long quantity;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "rating")
    private Double rating;

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

    public Item(String name, long quantity, String description, @ColorInt int itemColorAccent, Set<String> tags, Date dateCreated, Date dateModified){
        this.name = name;
        this.quantity = quantity;
        this.description = description;
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

    public long getQuantity(){
        return quantity;
    }

    public void setQuantity(long quantity){
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

        return new EqualsBuilder()
                .append(id, item.id)
                .append(quantity, item.quantity)
                .append(itemColorAccent, item.itemColorAccent)
                .append(name, item.name)
                .append(description, item.description)
                .append(rating, item.rating)
                .append(dateCreated, item.dateCreated)
                .append(dateModified, item.dateModified)
                .append(tags, item.tags)
                .isEquals();
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(quantity)
                .append(description)
                .append(dateCreated)
                .append(dateModified)
                .append(itemColorAccent)
                .append(tags)
                .toHashCode();
    }
}
