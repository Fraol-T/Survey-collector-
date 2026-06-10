package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class QuestionDto {
    @SerializedName("surveyId")
    private String surveyId;

    @SerializedName("text")
    private String text;

    @SerializedName("type")
    private String type;

    @SerializedName("orderIndex")
    private int orderIndex;

    private transient String firebaseKey;

    public QuestionDto() {}

    public QuestionDto(String surveyId, String text, String type, int orderIndex) {
        this.surveyId = surveyId;
        this.text = text;
        this.type = type;
        this.orderIndex = orderIndex;
    }

    public String getSurveyId() { return surveyId; }
    public void setSurveyId(String surveyId) { this.surveyId = surveyId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
