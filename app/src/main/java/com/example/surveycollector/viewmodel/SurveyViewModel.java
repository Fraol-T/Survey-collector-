package com.example.surveycollector.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.model.repository.SurveyRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SurveyViewModel extends AndroidViewModel {
    private final SurveyRepository repository;
    private final LiveData<List<Survey>> allSurveys;
    private final ExecutorService executorService;

    public SurveyViewModel(@NonNull Application application) {
        super(application);
        repository = new SurveyRepository(application);
        allSurveys = repository.getAllSurveys();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Survey>> getAllSurveys() {
        return allSurveys;
    }

    public LiveData<Survey> getSurveyById(int id) {
        MutableLiveData<Survey> result = new MutableLiveData<>();
        executorService.execute(() -> {
            Survey s = repository.getSurveyById(id);
            result.postValue(s);
        });
        return result;
    }

    public void insert(Survey survey) {
        executorService.execute(() -> repository.insert(survey));
    }

    public void update(Survey survey) {
        executorService.execute(() -> repository.update(survey));
    }

    public void delete(Survey survey) {
        executorService.execute(() -> repository.delete(survey));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
