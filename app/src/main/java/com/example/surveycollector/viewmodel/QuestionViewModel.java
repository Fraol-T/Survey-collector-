package com.example.surveycollector.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.QuestionDao;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.QuestionDto;
import com.example.surveycollector.model.remote.dto.QuestionOptionDto;
import com.example.surveycollector.util.NetworkErrorManager;
import java.io.IOException;

public class QuestionViewModel extends AndroidViewModel {
    private final QuestionDao questionDao;
    private final LiveData<List<Question>> questionsForSurvey;
    private final LiveData<List<QuestionOption>> optionsForSurvey;
    private final ExecutorService executorService;
    private final ApiService apiService;

    public QuestionViewModel(@NonNull Application application, int surveyId) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        questionDao = db.questionDao();
        questionsForSurvey = questionDao.getQuestionsForSurvey(surveyId);
        optionsForSurvey = questionDao.getOptionsForSurvey(surveyId);
        executorService = Executors.newSingleThreadExecutor();
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public LiveData<List<Question>> getQuestionsForSurvey() {
        return questionsForSurvey;
    }

    public LiveData<List<QuestionOption>> getOptionsForSurvey() {
        return optionsForSurvey;
    }

    public void insertQuestion(Question question) {
        executorService.execute(() -> {
            long localId = questionDao.insert(question);
            question.setId((int) localId);

            // Sync to Firebase
            try {
                QuestionDto dto = new QuestionDto(
                        String.valueOf(question.getSurveyId()),
                        question.getText(),
                        question.getType(),
                        question.getOrderIndex()
                );
                retrofit2.Response<FirebaseKey> response = apiService.postQuestion(dto).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String firebaseKey = response.body().getName();
                    question.setFirebaseKey(firebaseKey);
                    questionDao.update(question);
                } else {
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                NetworkErrorManager.postError("Network error. Changes saved locally.");
            }
        });
    }

    public void insertQuestionWithOptions(Question question, List<String> optionTexts) {
        executorService.execute(() -> {
            long questionId = questionDao.insert(question);
            question.setId((int) questionId);

            String questionFirebaseKey = null;
            try {
                QuestionDto dto = new QuestionDto(
                        String.valueOf(question.getSurveyId()),
                        question.getText(),
                        question.getType(),
                        question.getOrderIndex()
                );
                retrofit2.Response<FirebaseKey> response = apiService.postQuestion(dto).execute();
                if (response.isSuccessful() && response.body() != null) {
                    questionFirebaseKey = response.body().getName();
                    question.setFirebaseKey(questionFirebaseKey);
                    questionDao.update(question);
                } else {
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                NetworkErrorManager.postError("Network error. Changes saved locally.");
            }

            for (int i = 0; i < optionTexts.size(); i++) {
                QuestionOption option = new QuestionOption((int) questionId, optionTexts.get(i), i);
                long optionId = questionDao.insertOption(option);
                option.setId((int) optionId);

                if (questionFirebaseKey != null) {
                    try {
                        QuestionOptionDto optDto = new QuestionOptionDto(
                                questionFirebaseKey,
                                option.getText(),
                                option.getOrderIndex()
                        );
                        retrofit2.Response<FirebaseKey> response = apiService.postQuestionOption(optDto).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            option.setFirebaseKey(response.body().getName());
                            questionDao.updateOption(option);
                        } else {
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

    public void updateQuestion(Question question) {
        executorService.execute(() -> {
            questionDao.update(question);
            // Updating a question in Firebase could be implemented if there was a putQuestion API,
            // but for MVP, we only need to sync inserts/deletions.
        });
    }

    public void deleteQuestion(Question question) {
        executorService.execute(() -> {
            // Delete options from Firebase first
            List<QuestionOption> options = questionDao.getOptionsForQuestionSync(question.getId());
            for (QuestionOption option : options) {
                if (option.getFirebaseKey() != null) {
                    try {
                        retrofit2.Response<Void> response = apiService.deleteQuestionOption(option.getFirebaseKey()).execute();
                        if (!response.isSuccessful()) {
                            NetworkErrorManager.postError("Network error. Changes saved locally.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                }
            }

            // Delete question from Firebase
            if (question.getFirebaseKey() != null) {
                try {
                    retrofit2.Response<Void> response = apiService.deleteQuestion(question.getFirebaseKey()).execute();
                    if (!response.isSuccessful()) {
                        NetworkErrorManager.postError("Network error. Changes saved locally.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    NetworkErrorManager.postError("Network error. Changes saved locally.");
                }
            }

            // Local DB cascades delete
            questionDao.delete(question);
        });
    }

    public void insertOption(QuestionOption option) {
        executorService.execute(() -> questionDao.insertOption(option));
    }

    public void deleteOption(QuestionOption option) {
        executorService.execute(() -> questionDao.deleteOption(option));
    }

    public LiveData<List<QuestionOption>> getOptionsForQuestion(int questionId) {
        return questionDao.getOptionsForQuestion(questionId);
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
            return (T) new QuestionViewModel(application, surveyId);
        }
    }
}
