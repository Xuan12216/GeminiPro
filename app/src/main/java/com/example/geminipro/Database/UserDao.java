package com.example.geminipro.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import io.reactivex.Flowable;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users ORDER BY pin DESC, date DESC, id DESC")
    Flowable<List<User>> getAllUsersDesc();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Delete
    void deleteUser(User user);
}