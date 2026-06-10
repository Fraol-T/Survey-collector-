package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "surveys",
        indices = {@Index(value = {"adminId"})},
        foreignKeys = @ForeignKey(entity = Admin.class,
                                  parentColumns = "id",
                                  childColumns = "adminId",
                                  onDelete = ForeignKey.CASCADE))
public class Survey {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int adminId;

    @NonNull
    private String title;

    private String description;

    private int requiresRegistration = 0; // Default 0

    private int isActive = 1; // Default 1

    @NonNull
    private String createdAt;

    private String firebaseKey;

    public Survey(int id, int adminId, @NonNull String title, String description, int requiresRegistration, int isActive, @NonNull String createdAt, String firebaseKey) {
        this.id = id;
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.requiresRegistration = requiresRegistration;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.firebaseKey = firebaseKey;
    }

    @Ignore
    public Survey(int id, int adminId, @NonNull String title, String description, int requiresRegistration, int isActive, @NonNull String createdAt) {
        this(id, adminId, title, description, requiresRegistration, isActive, createdAt, null);
    }

    @Ignore
    public Survey(int adminId, @NonNull String title, String description, int requiresRegistration, int isActive, @NonNull String createdAt) {
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.requiresRegistration = requiresRegistration;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.firebaseKey = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRequiresRegistration() {
        return requiresRegistration;
    }

    public void setRequiresRegistration(int requiresRegistration) {
        this.requiresRegistration = requiresRegistration;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    @NonNull
    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull String createdAt) {
        this.createdAt = createdAt;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
