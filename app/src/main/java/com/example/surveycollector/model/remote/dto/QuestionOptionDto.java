package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class QuestionOptionDto {
    @SerializedName("questionId")
    private String questionId;

    @SerializedName("text")
    private String text;

    @SerializedName("orderIndex")
    private int orderIndex;

    private transient String firebaseKey;

    public QuestionOptionDto() {}

    public QuestionOptionDto(String questionId, String text, int orderIndex) {
        this.questionId = questionId;
        this.text = text;
        this.orderIndex = orderIndex;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
