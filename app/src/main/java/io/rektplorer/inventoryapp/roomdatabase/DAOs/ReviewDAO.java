package io.rektplorer.inventoryapp.roomdatabase.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.rektplorer.inventoryapp.roomdatabase.Entities.Review;

@Dao
public interface ReviewDAO{

    @Query("SELECT * FROM reviews")
    LiveData<List<Review>> getAll();

    @Query("SELECT * FROM reviews WHERE itemId LIKE :id")
    LiveData<List<Review>> getReviewsByItemId(int id);

    @Query("SELECT * FROM reviews WHERE itemId LIKE :itemId AND userId LIKE :userId")
    LiveData<Review> getReviewByItemAndUserId(int itemId, int userId);

    @Insert
    void insertAll(Review... reviews);

    @Update
    void update(Review review);

    @Delete
    void delete(Review review);
}
