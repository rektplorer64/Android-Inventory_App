package tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;

@Dao
public interface ItemDAO{

    @Query("SELECT * FROM items")
    LiveData<List<Item>> getAll();

    @Query("SELECT * FROM items WHERE name LIKE :name LIMIT 1")
    LiveData<Item> getItemByName(String name);

    @Query("SELECT * FROM items WHERE id LIKE :itemId LIMIT 1")
    LiveData<Item> getItemById(int itemId);

    @Query("SELECT MIN(id) FROM items")
    int getMinItemId();

    @Query("SELECT MAX(quantity) FROM items")
    int getMaxItemQuantity();

    @Query("SELECT MIN(quantity) FROM items")
    int getMinItemQuantity();

    @Insert
    void insertAll(Item... items);

    @Update
    void update(Item item);

    @Delete
    void delete(Item item);
}
