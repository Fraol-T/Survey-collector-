package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class SurveyLinkDto {
    @SerializedName("surveyId")
    private int surveyId;

    @SerializedName("token")
    private String token;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("createdAt")
    private String createdAt;

    private transient String firebaseKey;

    public SurveyLinkDto() {}

    public SurveyLinkDto(int surveyId, String token, boolean isActive, String createdAt) {
        this.surveyId = surveyId;
        this.token = token;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getSurveyId() { return surveyId; }
    public void setSurveyId(int surveyId) { this.surveyId = surveyId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
