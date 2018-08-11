package io.rektplorer.inventoryapp.roomdatabase.DAOs;


import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.rektplorer.inventoryapp.roomdatabase.Entities.Image;

@Dao
public interface ImageDAO{
    @Query("SELECT * FROM images")
    LiveData<List<Image>> getAll();

    @Query("SELECT * FROM images WHERE id LIKE :id LIMIT 1")
    LiveData<Image> getImageById(int id);

    @Query("SELECT * FROM images WHERE itemId LIKE :itemId")
    LiveData<List<Image>> getImagesByItemId(int itemId);

    @Query("SELECT * FROM images WHERE userId LIKE :userId")
    LiveData<List<Image>> getImagesByUserId(int userId);

    @Query("SELECT * FROM images WHERE isHeroImage LIKE 1")
    LiveData<List<Image>> getAllHeroImage();

    @Query("SELECT * FROM images WHERE itemId LIKE :itemId AND isHeroImage LIKE 1")
    LiveData<Image> getHeroImageByItemId(int itemId);

    @Query("SELECT * FROM images WHERE date_added LIKE :time LIMIT 1")
    Image getImageByTimeStamp(long time);

    @Insert
    void insertAll(Image... images);

    @Update
    void update(Image image);

    @Delete
    void delete(Image image);
}
