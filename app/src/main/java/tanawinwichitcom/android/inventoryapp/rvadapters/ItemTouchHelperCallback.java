package tanawinwichitcom.android.inventoryapp.rvadapters;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

public class ItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback{

    public ItemTouchHelperCallback(int dragDirs, int swipeDirs){
        super(dragDirs, swipeDirs);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target){
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction){
        //TODO: implements this
    }
}
