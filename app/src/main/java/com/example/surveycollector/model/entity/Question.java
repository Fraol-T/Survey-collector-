package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "questions",
        indices = {@Index(value = {"surveyId"})},
        foreignKeys = @ForeignKey(entity = Survey.class,
                                  parentColumns = "id",
                                  childColumns = "surveyId",
                                  onDelete = ForeignKey.CASCADE))
public class Question {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int surveyId;

    @NonNull
    private String text;

    @NonNull
    private String type; // TEXT / SINGLE_CHOICE / MULTIPLE_CHOICE

    private int orderIndex = 0;

    public Question(int id, int surveyId, @NonNull String text, @NonNull String type, int orderIndex) {
        this.id = id;
        this.surveyId = surveyId;
        this.text = text;
        this.type = type;
        this.orderIndex = orderIndex;
    }

    @Ignore
    public Question(int surveyId, @NonNull String text, @NonNull String type, int orderIndex) {
        this.surveyId = surveyId;
        this.text = text;
        this.type = type;
        this.orderIndex = orderIndex;
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
    public String getText() {
        return text;
    }

    public void setText(@NonNull String text) {
        this.text = text;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    private String firebaseKey;

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
