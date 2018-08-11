package io.rektplorer.inventoryapp.searchpreferencehelper;


import android.content.Context;
import android.content.SharedPreferences;

import java.lang.annotation.Retention;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import io.rektplorer.inventoryapp.R;
import io.rektplorer.inventoryapp.rvadapters.item.ItemAdapter;
import io.rektplorer.inventoryapp.rvadapters.itemdecoration.MarginItemDecoration;
import io.rektplorer.inventoryapp.rvadapters.itemdecoration.PaddingLeftDecoration;

import static java.lang.annotation.RetentionPolicy.CLASS;

public class ListLayoutPreference{

    @Retention(CLASS)
    @IntDef({FULL_CARD_LAYOUT, NORMAL_LIST_LAYOUT, SMALL_CARD_LAYOUT, COMPACT_LIST_LAYOUT})
    public @interface ListLayoutMode{
    }

    public static final int FULL_CARD_LAYOUT = 0;
    public static final int NORMAL_LIST_LAYOUT = 1;
    public static final int SMALL_CARD_LAYOUT = 2;
    public static final int COMPACT_LIST_LAYOUT = 3;

    private int listLayoutMode;
    private boolean gridMode;
    private int totalColumn;

    public ListLayoutPreference(@ListLayoutMode int listLayoutMode){
        this.listLayoutMode = listLayoutMode;
    }

    public int getListLayoutMode(){
        return listLayoutMode;
    }

    public boolean isGridMode(){
        return gridMode;
    }

    public void setGridMode(boolean gridMode){
        this.gridMode = gridMode;
    }

    public int getTotalColumn(){
        return totalColumn;
    }

    public void setTotalColumn(int totalColumn){
        this.totalColumn = totalColumn;
    }

    public void setListLayoutMode(int listLayoutMode){
        this.listLayoutMode = listLayoutMode;
    }

    public static void setupRecyclerView(ListLayoutPreference preference, RecyclerView recycler, ItemAdapter adapter){
        Context context = recycler.getContext();
        clearAllItemDecoration(recycler);
        switch(preference.getListLayoutMode()){
            case NORMAL_LIST_LAYOUT:
                if(!preference.isGridMode()){
                    recycler.addItemDecoration(new PaddingLeftDecoration(context, PaddingLeftDecoration.LIST_NORMAL_MARGIN));
                }
                adapter.setLayoutMode(ListLayoutPreference.NORMAL_LIST_LAYOUT, recycler);
                break;
            case SMALL_CARD_LAYOUT:
                recycler.addItemDecoration(new MarginItemDecoration(context, 4, preference.getTotalColumn()));
                adapter.setLayoutMode(ListLayoutPreference.SMALL_CARD_LAYOUT, recycler);
                break;
            case COMPACT_LIST_LAYOUT:
                if(!preference.isGridMode()){
                    recycler.addItemDecoration(new PaddingLeftDecoration(context, PaddingLeftDecoration.LIST_COMPACT_MARGIN));
                }
                adapter.setLayoutMode(ListLayoutPreference.COMPACT_LIST_LAYOUT, recycler);
                break;
            case FULL_CARD_LAYOUT:
                recycler.addItemDecoration(new MarginItemDecoration(context, 4, preference.getTotalColumn()));
                adapter.setLayoutMode(ListLayoutPreference.FULL_CARD_LAYOUT, recycler);
                break;
        }

        if(preference.isGridMode()){
            recycler.setLayoutManager(new StaggeredGridLayoutManager(preference.getTotalColumn(), StaggeredGridLayoutManager.VERTICAL));
        }else{
            recycler.setLayoutManager(new LinearLayoutManager(context));
        }
        // recycler.swapAdapter(adapter, true);
    }

    public static void saveToSharedPreference(Context c, ListLayoutPreference viewModePreference){
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.pref_list_layout), Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.clear();

        e.putInt(c.getString(R.string.pref_title_view_mode), viewModePreference.getListLayoutMode());
        e.putBoolean(c.getString(R.string.pref_title_in_grid), viewModePreference.isGridMode());
        e.putInt(c.getString(R.string.pref_title_column_grid), viewModePreference.getTotalColumn());

        e.commit();
    }

    public static ListLayoutPreference loadFromSharedPreference(Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences(c.getString(R.string.pref_list_layout), Context.MODE_PRIVATE);
        ListLayoutPreference viewModePref = new ListLayoutPreference(sharedPreferences.getInt(c.getString(R.string.pref_title_view_mode), COMPACT_LIST_LAYOUT));

        viewModePref.setGridMode(sharedPreferences.getBoolean(c.getString(R.string.pref_title_in_grid), false));
        viewModePref.setTotalColumn(sharedPreferences.getInt(c.getString(R.string.pref_title_column_grid), 1));

        return viewModePref;
    }

    public static void clearAllItemDecoration(RecyclerView recycler){
        for(int i = 0; i < recycler.getItemDecorationCount(); i++){
            recycler.removeItemDecorationAt(i);
        }
    }
}
