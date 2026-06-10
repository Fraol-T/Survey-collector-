package com.example.surveycollector.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.SurveyLinkDao;
import com.example.surveycollector.model.entity.SurveyLink;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.SurveyLinkDto;
import com.example.surveycollector.util.LinkUtil;
import com.example.surveycollector.util.NetworkErrorManager;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurveyLinkViewModel extends AndroidViewModel {
    private final SurveyLinkDao surveyLinkDao;
    private final ApiService apiService;
    private final LiveData<SurveyLink> currentLink;
    private final ExecutorService executorService;
    private final int surveyId;

    public SurveyLinkViewModel(@NonNull Application application, int surveyId) {
        super(application);
        this.surveyId = surveyId;
        AppDatabase db = AppDatabase.getInstance(application);
        this.surveyLinkDao = db.surveyLinkDao();
        this.apiService = ApiClient.getClient().create(ApiService.class);
        this.currentLink = surveyLinkDao.getLinkBySurveyId(surveyId);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<SurveyLink> getCurrentLink() {
        return currentLink;
    }

    public void generateLink(int surveyId) {
        executorService.execute(() -> {
            SurveyLink existing = surveyLinkDao.getLinkBySurveyIdSync(surveyId);
            if (existing == null) {
                String token = LinkUtil.generateToken();
                String createdAt = new Date().toString();
                SurveyLink newLink = new SurveyLink(surveyId, token, 1, createdAt);
                long newId = surveyLinkDao.insert(newLink);
                newLink.setId((int) newId);

                // Sync to cloud
                syncLinkToCloud(newLink);
            }
        });
    }

    public void toggleActive(SurveyLink link) {
        executorService.execute(() -> {
            int newActive = (link.getIsActive() == 1) ? 0 : 1;
            link.setIsActive(newActive);
            surveyLinkDao.update(link);

            // Sync status to cloud
            syncLinkUpdateToCloud(link);
        });
    }

    private void syncLinkToCloud(SurveyLink link) {
        SurveyLinkDto dto = new SurveyLinkDto(
                link.getSurveyId(),
                link.getToken(),
                link.getIsActive() == 1,
                link.getCreatedAt()
        );
        try {
            retrofit2.Response<FirebaseKey> response = apiService.postSurveyLink(dto).execute();
            if (response.isSuccessful() && response.body() != null) {
                String firebaseKey = response.body().getName();
                link.setFirebaseKey(firebaseKey);
                surveyLinkDao.update(link);
            } else {
                NetworkErrorManager.postError("Network error. Changes saved locally.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            NetworkErrorManager.postError("Network error. Changes saved locally.");
        }
    }

    private void syncLinkUpdateToCloud(SurveyLink link) {
        if (link.getFirebaseKey() != null) {
            SurveyLinkDto dto = new SurveyLinkDto(
                    link.getSurveyId(),
                    link.getToken(),
                    link.getIsActive() == 1,
                    link.getCreatedAt()
            );
            try {
                retrofit2.Response<com.example.surveycollector.model.remote.dto.SurveyLinkDto> response = apiService.patchSurveyLink(link.getFirebaseKey(), dto).execute();
                if (!response.isSuccessful()) {
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                NetworkErrorManager.postError("Network error. Changes saved locally.");
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
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
            return (T) new SurveyLinkViewModel(application, surveyId);
        }
    }
}
