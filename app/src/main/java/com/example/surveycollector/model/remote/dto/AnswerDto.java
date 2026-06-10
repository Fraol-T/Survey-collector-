package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class AnswerDto {
    @SerializedName("responseId")
    private String responseId;

    @SerializedName("questionId")
    private String questionId;

    @SerializedName("answerValue")
    private String answerValue;

    private transient String firebaseKey;

    public AnswerDto() {}

    public AnswerDto(String responseId, String questionId, String answerValue) {
        this.responseId = responseId;
        this.questionId = questionId;
        this.answerValue = answerValue;
    }

    public String getResponseId() { return responseId; }
    public void setResponseId(String responseId) { this.responseId = responseId; }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
