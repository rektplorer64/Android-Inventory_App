package tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil;

import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import androidx.recyclerview.widget.RecyclerView;
import tanawinwichitcom.android.inventoryapp.rvadapters.item.ItemAdapter;

public class ItemEntityDetailsLookup extends ItemDetailsLookup{

    private final RecyclerView recyclerView;

    public ItemEntityDetailsLookup(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    @Override
    public ItemDetails getItemDetails(MotionEvent e){
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder holder = recyclerView.getChildViewHolder(view);
            if (holder instanceof ItemAdapter.ItemViewHolder) {
                return ((ItemAdapter.ItemViewHolder) holder).getItemDetails();
            }
        }
        return null;
    }
}
