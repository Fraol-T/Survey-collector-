package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "responses",
        indices = {@Index(value = {"surveyId"}), @Index(value = {"respondentId"})},
        foreignKeys = {
            @ForeignKey(entity = Survey.class,
                        parentColumns = "id",
                        childColumns = "surveyId",
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Respondent.class,
                        parentColumns = "id",
                        childColumns = "respondentId",
                        onDelete = ForeignKey.SET_NULL)
        })
public class Response {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int surveyId;

    private Integer respondentId; // Nullable FK

    @NonNull
    private String submittedAt;

    private int synced = 0; // Default 0

    private String firebaseKey;

    public Response(int id, int surveyId, Integer respondentId, @NonNull String submittedAt, int synced, String firebaseKey) {
        this.id = id;
        this.surveyId = surveyId;
        this.respondentId = respondentId;
        this.submittedAt = submittedAt;
        this.synced = synced;
        this.firebaseKey = firebaseKey;
    }

    @Ignore
    public Response(int id, int surveyId, Integer respondentId, @NonNull String submittedAt, int synced) {
        this(id, surveyId, respondentId, submittedAt, synced, null);
    }

    @Ignore
    public Response(int surveyId, Integer respondentId, @NonNull String submittedAt, int synced) {
        this.surveyId = surveyId;
        this.respondentId = respondentId;
        this.submittedAt = submittedAt;
        this.synced = synced;
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

    public Integer getRespondentId() {
        return respondentId;
    }

    public void setRespondentId(Integer respondentId) {
        this.respondentId = respondentId;
    }

    @NonNull
    public String getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(@NonNull String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
