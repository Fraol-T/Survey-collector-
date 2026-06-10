package com.example.surveycollector.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.ResponseDao;
import com.example.surveycollector.model.dao.SurveyDao;
import com.example.surveycollector.model.entity.Survey;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final LiveData<Integer> totalSurveys;
    private final LiveData<Integer> totalResponses;
    private final LiveData<Integer> activeSurveys;
    private final LiveData<List<Survey>> recentSurveys;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        SurveyDao surveyDao = db.surveyDao();
        ResponseDao responseDao = db.responseDao();

        this.totalSurveys = surveyDao.getSurveyCount();
        this.activeSurveys = surveyDao.getActiveSurveyCount();
        this.recentSurveys = surveyDao.getRecentSurveys(3);
        this.totalResponses = responseDao.getResponseCount();
    }

    public LiveData<Integer> getTotalSurveys() {
        return totalSurveys;
    }

    public LiveData<Integer> getTotalResponses() {
        return totalResponses;
    }

    public LiveData<Integer> getActiveSurveys() {
        return activeSurveys;
    }

    public LiveData<List<Survey>> getRecentSurveys() {
        return recentSurveys;
    }
}
