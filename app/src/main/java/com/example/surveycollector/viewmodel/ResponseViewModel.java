package com.example.surveycollector.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.ResponseDao;
import com.example.surveycollector.model.entity.Answer;
import com.example.surveycollector.model.entity.Response;
import com.example.surveycollector.model.entity.Respondent;
import java.util.List;

public class ResponseViewModel extends AndroidViewModel {
    private final ResponseDao responseDao;
    private final LiveData<List<Response>> responsesForSurvey;

    public ResponseViewModel(@NonNull Application application, int surveyId) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        this.responseDao = db.responseDao();
        this.responsesForSurvey = responseDao.getResponsesForSurvey(surveyId);
    }

    public LiveData<List<Response>> getResponsesForSurvey() {
        return responsesForSurvey;
    }

    public LiveData<List<Answer>> getAnswersForResponse(int responseId) {
        return responseDao.getAnswersForResponse(responseId);
    }

    public LiveData<Respondent> getRespondentById(int respondentId) {
        return responseDao.getRespondentById(respondentId);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int surveyId;

        public Factory(Application application, int surveyId) {
            this.application = application;
            this.surveyId = surveyId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ResponseViewModel(application, surveyId);
        }
    }
}
