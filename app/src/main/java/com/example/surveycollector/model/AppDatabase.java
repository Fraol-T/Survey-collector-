package com.example.surveycollector.model;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.surveycollector.model.dao.AdminDao;
import com.example.surveycollector.model.dao.SurveyDao;
import com.example.surveycollector.model.dao.QuestionDao;
import com.example.surveycollector.model.dao.ResponseDao;
import com.example.surveycollector.model.dao.SurveyLinkDao;
import com.example.surveycollector.model.entity.Admin;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import com.example.surveycollector.model.entity.Respondent;
import com.example.surveycollector.model.entity.Response;
import com.example.surveycollector.model.entity.Answer;
import com.example.surveycollector.model.entity.SurveyLink;

@Database(entities = {
        Admin.class,
        Survey.class,
        Question.class,
        QuestionOption.class,
        Respondent.class,
        Response.class,
        Answer.class,
        SurveyLink.class
}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public static final java.util.concurrent.ExecutorService databaseWriteExecutor =
            java.util.concurrent.Executors.newFixedThreadPool(4);

    public abstract AdminDao adminDao();
    public abstract SurveyDao surveyDao();
    public abstract QuestionDao questionDao();
    public abstract ResponseDao responseDao();
    public abstract SurveyLinkDao surveyLinkDao();

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "survey_collector_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
