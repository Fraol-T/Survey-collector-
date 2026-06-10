package com.example.surveycollector.model.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.surveycollector.model.entity.Admin;

@Dao
public interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Admin admin);

    @Query("SELECT * FROM admins WHERE email = :email LIMIT 1")
    Admin getByEmail(String email);

    @Query("SELECT COUNT(*) FROM admins")
    int getCount();

    @Query("SELECT * FROM admins WHERE id = :id LIMIT 1")
    Admin getById(int id);
}
