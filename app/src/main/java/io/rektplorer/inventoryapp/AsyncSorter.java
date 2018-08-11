package io.rektplorer.inventoryapp;

import android.content.Context;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.rektplorer.inventoryapp.roomdatabase.Entities.Item;
import io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference;

import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.COLOR_ACCENT;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.DATE_CREATED;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.DATE_MODIFIED;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.DESCRIPTION;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.ID;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.NAME;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.QUANTITY;
import static io.rektplorer.inventoryapp.searchpreferencehelper.SortPreference.RATING;

public class AsyncSorter extends AsyncTaskLoader<List<Item>>{

    private final List<Item> itemList;
    private final SortPreference sortPref;

    public AsyncSorter(@NonNull Context context, List<Item> itemList, SortPreference sortPref){
        super(context);
        this.itemList = itemList;
        this.sortPref = sortPref;
    }

    @Nullable
    @Override
    public List<Item> loadInBackground(){
        return sort(itemList, sortPref);
    }

    @CheckResult
    public static List<Item> sort(List<Item> itemList, final SortPreference sortPref){
        if(itemList == null){
            return new ArrayList<>();
        }

        Collections.sort(itemList, new Comparator<Item>(){
            @Override
            public int compare(Item o1, Item o2){
                String string1;
                String string2;
                switch(sortPref.getField()){
                    case ID:
                        return Integer.compare(o1.getId(), o2.getId());
                    case NAME:
                        string1 = o1.getName().toLowerCase();
                        string2 = o2.getName().toLowerCase();
                        if(sortPref.isStringLength()){
                            return Integer.compare(string1.length(), string2.length());
                        }else{
                            return string2.compareTo(string1);
                        }
                    case DESCRIPTION:
                        string1 = o1.getDescription();
                        string2 = o2.getDescription();
                        if(sortPref.isStringLength()){
                            return Integer.compare(string1.length(), string2.length());
                        }else{
                            return string1.compareTo(string2);
                        }
                    case DATE_CREATED:
                        try{
                            return Long.compare(o1.getDateCreated().getTime(), o2.getDateCreated().getTime());
                        }catch(NullPointerException e){
                            e.printStackTrace();
                            return 0;
                        }
                    case DATE_MODIFIED:
                        try{
                            return Long.compare(o1.getDateModified().getTime(), o2.getDateModified().getTime());
                        }catch(NullPointerException e){
                            e.printStackTrace();
                            return 0;
                        }
                    case COLOR_ACCENT:
                        return Integer.compare(o1.getItemColorAccent(), o2.getItemColorAccent());
                    case QUANTITY:
                        return Long.compare(o1.getQuantity(), o2.getQuantity());
                    case RATING:
                        try{
                            return Double.compare(o1.getRating(), o2.getRating());
                        }catch(NullPointerException e){
                            e.printStackTrace();
                            return 0;
                        }
                }
                return 0;
            }
        });

        if(!sortPref.isInAscendingOrder()){
            Collections.reverse(itemList);
        }
        return itemList;
    }
}
