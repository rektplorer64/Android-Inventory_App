package tanawinwichitcom.android.inventoryapp.roomdatabase.DAOs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import tanawinwichitcom.android.inventoryapp.roomdatabase.Entities.User;

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
