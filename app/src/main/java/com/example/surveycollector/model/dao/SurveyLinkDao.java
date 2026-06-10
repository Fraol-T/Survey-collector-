package com.example.surveycollector.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.surveycollector.model.entity.SurveyLink;

@Dao
public interface SurveyLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SurveyLink link);

    @Update
    void update(SurveyLink link);

    @Query("DELETE FROM survey_links WHERE surveyId = :surveyId")
    void deleteBySurveyId(int surveyId);

    @Query("SELECT * FROM survey_links WHERE surveyId = :surveyId LIMIT 1")
    LiveData<SurveyLink> getLinkBySurveyId(int surveyId);

    @Query("SELECT * FROM survey_links WHERE surveyId = :surveyId LIMIT 1")
    SurveyLink getLinkBySurveyIdSync(int surveyId);

    @Query("SELECT * FROM survey_links WHERE token = :token LIMIT 1")
    SurveyLink getLinkByToken(String token);
}
