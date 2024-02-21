package com.example.geminipro.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE title = :title LIMIT 1")
    Single<User> getUserByTitle(String title);

    @Query("SELECT * FROM users ORDER BY pin DESC, date DESC, id DESC")
    Flowable<List<User>> getAllUsersDesc();

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);
}