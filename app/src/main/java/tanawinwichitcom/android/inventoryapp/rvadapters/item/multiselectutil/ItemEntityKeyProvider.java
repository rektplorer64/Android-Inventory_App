package tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil;

import java.util.List;

import androidx.recyclerview.selection.ItemKeyProvider;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;

public class ItemEntityKeyProvider extends ItemKeyProvider{

    private final List<Item> itemList;

    public ItemEntityKeyProvider(int scope, List<Item> itemList){
        super(scope);
        this.itemList = itemList;
    }

    @Override
    public Object getKey(int position){
        return itemList.get(position);
    }

    @Override
    public int getPosition(Object key){
        return itemList.indexOf(key);
    }
}
