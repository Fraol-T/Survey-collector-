package com.example.surveycollector.model.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "question_options",
        indices = {@Index(value = {"questionId"})},
        foreignKeys = @ForeignKey(entity = Question.class,
                                  parentColumns = "id",
                                  childColumns = "questionId",
                                  onDelete = ForeignKey.CASCADE))
public class QuestionOption {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int questionId;

    @NonNull
    private String text;

    private int orderIndex = 0;

    public QuestionOption(int id, int questionId, @NonNull String text, int orderIndex) {
        this.id = id;
        this.questionId = questionId;
        this.text = text;
        this.orderIndex = orderIndex;
    }

    @Ignore
    public QuestionOption(int questionId, @NonNull String text, int orderIndex) {
        this.questionId = questionId;
        this.text = text;
        this.orderIndex = orderIndex;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(@NonNull String text) {
        this.text = text;
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
