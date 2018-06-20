package tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.DAOs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.RoomDatabaseUtility.Entities.Review;

@Dao
public interface ReviewDAO{

    @Query("SELECT * FROM reviews")
    LiveData<List<Review>> getAll();

    @Query("SELECT * FROM reviews WHERE id LIKE :id LIMIT 1")
    Review findById(int id);

    @Insert
    void insertAll(Review... reviews);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);
}
