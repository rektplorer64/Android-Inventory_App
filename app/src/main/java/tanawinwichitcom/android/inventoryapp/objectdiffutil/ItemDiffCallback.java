package tanawinwichitcom.android.inventoryapp.objectdiffutil;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.ArrayList;
import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;

public class ItemDiffCallback extends DiffUtil.Callback{

    private List<Item> oldItemList;
    private List<Item> newItemList;

    public ItemDiffCallback(List<Item> oldItemList, List<Item> newItemList){
        if(oldItemList != null){
            this.oldItemList = oldItemList;
        }else{
            this.oldItemList = new ArrayList<>();
        }
        if(newItemList != null){
            this.newItemList = newItemList;
        }else{
            this.newItemList = new ArrayList<>();
        }
    }

    @Override
    public int getOldListSize(){
        return oldItemList.size();
    }

    @Override
    public int getNewListSize(){
        return newItemList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition){
        return oldItemList.get(oldItemPosition) == newItemList.get(newItemPosition);
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition){
        return oldItemList.get(oldItemPosition).equals(newItemList.get(newItemPosition));
    }
}
