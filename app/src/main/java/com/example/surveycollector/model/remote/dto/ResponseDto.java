package com.example.surveycollector.model.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ResponseDto {
    @SerializedName("surveyId")
    private String surveyId;

    @SerializedName("respondentId")
    private String respondentId; // Nullable

    @SerializedName("submittedAt")
    private String submittedAt;

    private transient String firebaseKey;

    public ResponseDto() {}

    public ResponseDto(String surveyId, String respondentId, String submittedAt) {
        this.surveyId = surveyId;
        this.respondentId = respondentId;
        this.submittedAt = submittedAt;
    }

    public String getSurveyId() { return surveyId; }
    public void setSurveyId(String surveyId) { this.surveyId = surveyId; }

    public String getRespondentId() { return respondentId; }
    public void setRespondentId(String respondentId) { this.respondentId = respondentId; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getFirebaseKey() { return firebaseKey; }
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; }
}
