package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "survey_links",
        indices = {@Index(value = {"token"}, unique = true), @Index(value = {"surveyId"})},
        foreignKeys = @ForeignKey(entity = Survey.class,
                                  parentColumns = "id",
                                  childColumns = "surveyId",
                                  onDelete = ForeignKey.CASCADE))
public class SurveyLink {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int surveyId;

    @NonNull
    private String token;

    private int isActive = 1; // Default 1

    @NonNull
    private String createdAt;

    private String firebaseKey;

    public SurveyLink(int id, int surveyId, @NonNull String token, int isActive, @NonNull String createdAt, String firebaseKey) {
        this.id = id;
        this.surveyId = surveyId;
        this.token = token;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.firebaseKey = firebaseKey;
    }

    @Ignore
    public SurveyLink(int id, int surveyId, @NonNull String token, int isActive, @NonNull String createdAt) {
        this(id, surveyId, token, isActive, createdAt, null);
    }

    @Ignore
    public SurveyLink(int surveyId, @NonNull String token, int isActive, @NonNull String createdAt) {
        this.surveyId = surveyId;
        this.token = token;
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

    public int getSurveyId() {
        return surveyId;
    }

    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    public void setToken(@NonNull String token) {
        this.token = token;
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
