package tanawinwichitcom.android.inventoryapp.rvadapters.item.multiselectutil;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.selection.SelectionTracker;

public class ItemActionModeController implements android.view.ActionMode.Callback{

    private final Context context;
    private final SelectionTracker selectionTracker;

    public ItemActionModeController(Context context, SelectionTracker selectionTracker){
        this.context = context;
        this.selectionTracker = selectionTracker;
    }

    @Override
    public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu){
        return false;
    }

    @Override
    public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu){
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem){
        return false;
    }

    @Override
    public void onDestroyActionMode(android.view.ActionMode actionMode){
        selectionTracker.clearSelection();
    }
}
