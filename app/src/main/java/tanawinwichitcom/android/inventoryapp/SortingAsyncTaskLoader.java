package tanawinwichitcom.android.inventoryapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.rvadapters.ItemAdapter.ItemListElementWrapper;
import tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference;

import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.COLOR_ACCENT;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DATE_CREATED;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DATE_MODIFIED;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.DESCRIPTION;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.ID;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.NAME;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.QUANTITY;
import static tanawinwichitcom.android.inventoryapp.searchpreferencehelper.SortPreference.RATING;

public class SortingAsyncTaskLoader extends AsyncTaskLoader<List<ItemListElementWrapper>>{

    private final List<ItemListElementWrapper> itemList;
    private final SortPreference sortPref;

    public SortingAsyncTaskLoader(@NonNull Context context, List<ItemListElementWrapper> itemList, SortPreference sortPref){
        super(context);
        this.itemList = itemList;
        this.sortPref = sortPref;
    }

    @Nullable
    @Override
    public List<ItemListElementWrapper> loadInBackground(){
        Collections.sort(itemList, new Comparator<ItemListElementWrapper>(){
            @Override
            public int compare(ItemListElementWrapper o1, ItemListElementWrapper o2){
                Item item1 = o1.getItem();
                Item item2 = o2.getItem();
                String string1;
                String string2;
                switch(sortPref.getField()){
                    case ID:
                        return Integer.compare(item1.getId(), item2.getId());
                    case NAME:
                        string1 = item1.getName().toLowerCase();
                        string2 = item2.getName().toLowerCase();
                        if(sortPref.isStringLength()){
                            return Integer.compare(string1.length(), string2.length());
                        }else{
                            return string1.compareTo(string2);
                        }
                    case DESCRIPTION:
                        string1 = item1.getDescription();
                        string2 = item2.getDescription();
                        if(sortPref.isStringLength()){
                            return Integer.compare(string1.length(), string2.length());
                        }else{
                            return string1.compareTo(string2);
                        }
                    case DATE_CREATED:
                        try{
                            return Long.compare(item1.getDateCreated().getTime(), item2.getDateCreated().getTime());
                        }catch(NullPointerException e){
                            e.printStackTrace();
                            return 0;
                        }
                    case DATE_MODIFIED:
                        try{
                            return Long.compare(item1.getDateModified().getTime(), item2.getDateModified().getTime());
                        }catch(NullPointerException e){
                            e.printStackTrace();
                            return 0;
                        }
                    case COLOR_ACCENT:
                        return Integer.compare(item1.getItemColorAccent(), item2.getItemColorAccent());
                    case QUANTITY:
                        return Integer.compare(item1.getQuantity(), item2.getQuantity());
                    case RATING:
                        try{
                            return Double.compare(item1.getRating(), item2.getRating());
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
