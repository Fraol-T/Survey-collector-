package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "respondents")
public class Respondent {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String email;

    @NonNull
    private String registeredAt;

    private String firebaseKey;

    public Respondent(int id, @NonNull String name, @NonNull String email, @NonNull String registeredAt, String firebaseKey) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.registeredAt = registeredAt;
        this.firebaseKey = firebaseKey;
    }

    @Ignore
    public Respondent(int id, @NonNull String name, @NonNull String email, @NonNull String registeredAt) {
        this(id, name, email, registeredAt, null);
    }

    @Ignore
    public Respondent(@NonNull String name, @NonNull String email, @NonNull String registeredAt) {
        this.name = name;
        this.email = email;
        this.registeredAt = registeredAt;
        this.firebaseKey = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(@NonNull String registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
