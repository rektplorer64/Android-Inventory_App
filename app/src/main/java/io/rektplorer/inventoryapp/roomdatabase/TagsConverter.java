package io.rektplorer.inventoryapp.roomdatabase;


import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import androidx.room.TypeConverter;

public class TagsConverter{

    @TypeConverter
    public static Set<String> tagsToStringSet(String tags){
        if(!tags.trim().isEmpty()){
            String tagList[] = tags.split(" ");
            return new TreeSet<>(Arrays.asList(tagList));
        }else{
            return new TreeSet<>();
        }
    }

    @TypeConverter
    public static String stringSetToTags(Set<String> tagSet){
        if(tagSet != null && !tagSet.isEmpty()){
            StringBuilder stringBuilder = new StringBuilder();
            for(String tag : tagSet){
                stringBuilder.append(tag).append(" ");
            }
            return stringBuilder.toString();
        }else{
            return "";
        }
    }
}
