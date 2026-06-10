package com.example.surveycollector.model.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.SurveyDao;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.SurveyDto;
import com.example.surveycollector.util.NetworkErrorManager;
import java.io.IOException;
import java.util.List;

public class SurveyRepository {
    private final SurveyDao surveyDao;
    private final ApiService apiService;

    public SurveyRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.surveyDao = db.surveyDao();
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public SurveyRepository(SurveyDao surveyDao, ApiService apiService) {
        this.surveyDao = surveyDao;
        this.apiService = apiService;
    }

    public LiveData<List<Survey>> getAllSurveys() {
        return surveyDao.getAllSurveys();
    }

    public Survey getSurveyById(int id) {
        return surveyDao.getSurveyById(id);
    }

    public void syncToCloud(final Survey s) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                SurveyDto dto = new SurveyDto(
                        s.getAdminId(),
                        s.getTitle(),
                        s.getDescription(),
                        s.getRequiresRegistration() == 1,
                        s.getIsActive() == 1,
                        s.getCreatedAt()
                );
                try {
                    retrofit2.Response<FirebaseKey> response = apiService.postSurvey(dto).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String firebaseKey = response.body().getName();
                        s.setFirebaseKey(firebaseKey);
                        surveyDao.update(s);
                    } else {
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            }
        });
    }

    public void insert(final Survey survey) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                long newId = surveyDao.insert(survey);
                survey.setId((int) newId);
                
                // After local save, sync to cloud
                SurveyDto dto = new SurveyDto(
                        survey.getAdminId(),
                        survey.getTitle(),
                        survey.getDescription(),
                        survey.getRequiresRegistration() == 1,
                        survey.getIsActive() == 1,
                        survey.getCreatedAt()
                );
                try {
                    retrofit2.Response<FirebaseKey> response = apiService.postSurvey(dto).execute();
                    if (response.isSuccessful() && response.body() != null) {
                        String firebaseKey = response.body().getName();
                        survey.setFirebaseKey(firebaseKey);
                        surveyDao.update(survey);
                    } else {
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            }
        });
    }

    public void update(final Survey survey) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                surveyDao.update(survey);
                if (survey.getFirebaseKey() != null) {
                    SurveyDto dto = new SurveyDto(
                            survey.getAdminId(),
                            survey.getTitle(),
                            survey.getDescription(),
                            survey.getRequiresRegistration() == 1,
                            survey.getIsActive() == 1,
                            survey.getCreatedAt()
                    );
                    try {
                        retrofit2.Response<com.example.surveycollector.model.remote.dto.SurveyDto> response = apiService.putSurvey(survey.getFirebaseKey(), dto).execute();
                        if (!response.isSuccessful()) {
                            NetworkErrorManager.postError("Network error. Changes saved locally.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                }
            }
        });
    }

    public void delete(final Survey survey) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                surveyDao.delete(survey);
                if (survey.getFirebaseKey() != null) {
                    try {
                        retrofit2.Response<Void> response = apiService.deleteSurvey(survey.getFirebaseKey()).execute();
                        if (!response.isSuccessful()) {
                            NetworkErrorManager.postError("Network error. Changes saved locally.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                }
            }
        });
    }
}
