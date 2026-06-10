package com.example.surveycollector.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import java.util.List;

@Dao
public interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Question question);

    @Update
    void update(Question question);

    @Delete
    void delete(Question question);

    @Query("SELECT * FROM questions WHERE surveyId = :surveyId ORDER BY orderIndex ASC")
    LiveData<List<Question>> getQuestionsForSurvey(int surveyId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertOption(QuestionOption option);

    @Update
    void updateOption(QuestionOption option);

    @Delete
    void deleteOption(QuestionOption option);

    @Query("SELECT * FROM question_options WHERE questionId = :questionId ORDER BY orderIndex ASC")
    LiveData<List<QuestionOption>> getOptionsForQuestion(int questionId);

    @Query("SELECT qo.* FROM question_options qo " +
           "INNER JOIN questions q ON qo.questionId = q.id " +
           "WHERE q.surveyId = :surveyId " +
           "ORDER BY qo.orderIndex ASC")
    LiveData<List<QuestionOption>> getOptionsForSurvey(int surveyId);

    @Query("SELECT * FROM questions WHERE surveyId = :surveyId ORDER BY orderIndex ASC")
    List<Question> getQuestionsForSurveySync(int surveyId);

    @Query("SELECT * FROM questions WHERE firebaseKey = :firebaseKey LIMIT 1")
    Question getQuestionByFirebaseKeySync(String firebaseKey);

    @Query("SELECT * FROM question_options WHERE questionId = :questionId ORDER BY orderIndex ASC")
    List<QuestionOption> getOptionsForQuestionSync(int questionId);
}
