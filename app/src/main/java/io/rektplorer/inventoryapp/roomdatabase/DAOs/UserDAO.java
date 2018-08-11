package io.rektplorer.inventoryapp.roomdatabase.DAOs;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.rektplorer.inventoryapp.roomdatabase.Entities.User;

@Dao
public interface UserDAO{

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAll();

    @Query("SELECT * FROM users WHERE name LIKE :name LIMIT 1")
    LiveData<User> findByName(String name);

    @Query("SELECT * FROM users WHERE id LIKE :id LIMIT 1")
    LiveData<User> findUserById(int id);

    @Insert
    void insertAll(User... users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
