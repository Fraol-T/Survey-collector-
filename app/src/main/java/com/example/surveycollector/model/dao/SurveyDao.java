package com.example.surveycollector.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.surveycollector.model.entity.Survey;
import java.util.List;

@Dao
public interface SurveyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Survey survey);

    @Update
    void update(Survey survey);

    @Delete
    void delete(Survey survey);

    @Query("SELECT * FROM surveys ORDER BY id DESC")
    LiveData<List<Survey>> getAllSurveys();

    @Query("SELECT * FROM surveys WHERE id = :id LIMIT 1")
    Survey getSurveyById(int id);

    @Query("SELECT COUNT(*) FROM surveys")
    LiveData<Integer> getSurveyCount();

    @Query("SELECT COUNT(*) FROM surveys WHERE isActive = 1")
    LiveData<Integer> getActiveSurveyCount();

    @Query("SELECT * FROM surveys ORDER BY createdAt DESC LIMIT :limit")
    LiveData<List<Survey>> getRecentSurveys(int limit);

    @Query("SELECT * FROM surveys WHERE title = :title LIMIT 1")
    Survey getSurveyByTitleSync(String title);
}
