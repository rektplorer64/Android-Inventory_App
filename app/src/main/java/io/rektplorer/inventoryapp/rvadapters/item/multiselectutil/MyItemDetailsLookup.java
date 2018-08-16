package io.rektplorer.inventoryapp.rvadapters.item.multiselectutil;


import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;
import io.rektplorer.inventoryapp.rvadapters.Detailable;

public class MyItemDetailsLookup extends ItemDetailsLookup<Long>{

    private final RecyclerView recyclerView;

    public MyItemDetailsLookup(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    @Override
    public ItemDetails<Long> getItemDetails(MotionEvent e){
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if(view != null){
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if(holder instanceof Detailable){
                return ((Detailable) holder).getItemDetails();
            }
        }
        return null;
    }
}
