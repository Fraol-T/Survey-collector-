package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "answers",
        indices = {@Index(value = {"responseId"}), @Index(value = {"questionId"})},
        foreignKeys = {
            @ForeignKey(entity = Response.class,
                        parentColumns = "id",
                        childColumns = "responseId",
                        onDelete = ForeignKey.CASCADE),
            @ForeignKey(entity = Question.class,
                        parentColumns = "id",
                        childColumns = "questionId",
                        onDelete = ForeignKey.CASCADE)
        })
public class Answer {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int responseId;

    private int questionId;

    @NonNull
    private String answerValue;

    public Answer(int id, int responseId, int questionId, @NonNull String answerValue) {
        this.id = id;
        this.responseId = responseId;
        this.questionId = questionId;
        this.answerValue = answerValue;
    }

    @Ignore
    public Answer(int responseId, int questionId, @NonNull String answerValue) {
        this.responseId = responseId;
        this.questionId = questionId;
        this.answerValue = answerValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    @NonNull
    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(@NonNull String answerValue) {
        this.answerValue = answerValue;
    }
}
