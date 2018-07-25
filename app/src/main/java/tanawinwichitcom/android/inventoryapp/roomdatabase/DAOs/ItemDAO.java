package tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Item;
import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.Review;

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

    @Query("SELECT * FROM (SELECT * FROM items WHERE id > :itemId ORDER BY id limit 1)" +
            " UNION ALL " +
            "SELECT * FROM (SELECT * FROM items WHERE id < :itemId ORDER BY id desc limit 1)")
    int[] getBothNearestIds(int itemId);

    @Insert
    void insertAll(Item... items);

    @Update
    void update(Item item);

    @Delete
    void delete(Item item);
}
