package com.example.surveycollector.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import com.example.surveycollector.model.entity.Answer;
import com.example.surveycollector.model.entity.Response;
import com.example.surveycollector.model.entity.Respondent;
import java.util.List;

@Dao
public interface ResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Response response);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAnswers(List<Answer> answers);

    @Transaction
    default void insertResponseAndAnswers(Response response, List<Answer> answers) {
        long responseId = insert(response);
        for (Answer answer : answers) {
            answer.setResponseId((int) responseId);
        }
        insertAnswers(answers);
    }

    @Query("SELECT * FROM responses WHERE surveyId = :surveyId ORDER BY id DESC")
    LiveData<List<Response>> getResponsesForSurvey(int surveyId);

    @Query("SELECT * FROM responses WHERE synced = 0 ORDER BY id ASC")
    List<Response> getUnsyncedResponses();

    @Query("UPDATE responses SET synced = 1 WHERE id = :responseId")
    void markSynced(int responseId);

    @Query("UPDATE responses SET synced = 1, firebaseKey = :firebaseKey WHERE id = :responseId")
    void markSyncedWithKey(int responseId, String firebaseKey);

    @Query("SELECT * FROM responses WHERE firebaseKey = :firebaseKey LIMIT 1")
    Response getResponseByFirebaseKeySync(String firebaseKey);

    @Query("SELECT * FROM respondents WHERE firebaseKey = :firebaseKey LIMIT 1")
    Respondent getRespondentByFirebaseKeySync(String firebaseKey);

    @Query("SELECT * FROM answers WHERE responseId = :responseId")
    LiveData<List<Answer>> getAnswersForResponse(int responseId);

    @Query("SELECT * FROM answers WHERE responseId = :responseId")
    List<Answer> getAnswersForResponseSync(int responseId);

    // Respondent operations (grouped here as there are 5 DAOs required)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRespondent(Respondent respondent);

    @Query("SELECT * FROM respondents WHERE id = :id LIMIT 1")
    LiveData<Respondent> getRespondentById(int id);

    @Query("SELECT * FROM respondents WHERE id = :id LIMIT 1")
    Respondent getRespondentByIdSync(int id);

    @Query("SELECT COUNT(*) FROM responses")
    LiveData<Integer> getResponseCount();
}
