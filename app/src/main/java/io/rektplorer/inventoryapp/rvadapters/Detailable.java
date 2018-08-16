package io.rektplorer.inventoryapp.rvadapters;

import androidx.recyclerview.selection.ItemDetailsLookup;

public interface Detailable{
    ItemDetailsLookup.ItemDetails<Long> getItemDetails();
}
