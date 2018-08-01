package tanawinwichitcom.android.inventoryapp.roomdatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import androidx.room.TypeConverter;

public class TagsConverter{

    @TypeConverter
    public static Set<String> tagsToStringSet(String tags){
        String tagList[] =  tags.split(" ");
        return new TreeSet<>(Arrays.asList(tagList));
    }

    @TypeConverter
    public static String stringSetToTags(Set<String> tagSet){
        StringBuilder stringBuilder = new StringBuilder();
        for(String tag : tagSet){
            stringBuilder.append(tag).append(" ");
        }
        return stringBuilder.toString();
    }
}
