package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "admins", indices = {@Index(value = {"email"}, unique = true)})
public class Admin {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String email;

    @NonNull
    private String password;

    @NonNull
    private String createdAt;

    public Admin(int id, @NonNull String email, @NonNull String password, @NonNull String createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    @Ignore
    public Admin(@NonNull String email, @NonNull String password, @NonNull String createdAt) {
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull String createdAt) {
        this.createdAt = createdAt;
    }
}
