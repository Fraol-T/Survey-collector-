package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SurveyDto {
    @SerializedName("adminId")
    private int adminId;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("requiresRegistration")
    private boolean requiresRegistration;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    private transient String firebaseKey;

    public SurveyDto() {}

    public SurveyDto(int adminId, String title, String description, boolean requiresRegistration, boolean isActive, String createdAt) {
        this.adminId = adminId;
        this.title = title;
        this.description = description;
        this.requiresRegistration = requiresRegistration;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isRequiresRegistration() { return requiresRegistration; }
    public void setRequiresRegistration(boolean requiresRegistration) { this.requiresRegistration = requiresRegistration; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
