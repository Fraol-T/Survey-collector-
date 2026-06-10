package com.example.surveycollector.model.repository;

import android.app.Application;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.ResponseDao;
import com.example.surveycollector.model.entity.Answer;
import com.example.surveycollector.model.entity.Response;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.AnswerDto;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.ResponseDto;
import java.io.IOException;
import java.util.List;

public class ResponseRepository {
    private final ResponseDao responseDao;
    private final ApiService apiService;

    public ResponseRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.responseDao = db.responseDao();
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public ResponseRepository(ResponseDao responseDao, ApiService apiService) {
        this.responseDao = responseDao;
        this.apiService = apiService;
    }

    public List<Response> getUnsyncedResponses() {
        return responseDao.getUnsyncedResponses();
    }

    public List<Answer> getAnswersForResponse(int responseId) {
        return responseDao.getAnswersForResponseSync(responseId);
    }

    public void syncResponse(final Response response, final List<Answer> answers) {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // POST the response to Firebase
                    ResponseDto responseDto = new ResponseDto(
                            String.valueOf(response.getSurveyId()),
                            response.getRespondentId() != null ? String.valueOf(response.getRespondentId()) : null,
                            response.getSubmittedAt()
                    );
                    retrofit2.Response<FirebaseKey> fbResponse = apiService.postResponse(responseDto).execute();

                    if (fbResponse.isSuccessful() && fbResponse.body() != null) {
                        String responseKey = fbResponse.body().getName();

                        // POST each answer with the Firebase response key
                        boolean allAnswersSynced = true;
                        for (Answer answer : answers) {
                            AnswerDto answerDto = new AnswerDto(
                                    responseKey,
                                    String.valueOf(answer.getQuestionId()),
                                    answer.getAnswerValue()
                            );
                            retrofit2.Response<FirebaseKey> answerResponse = apiService.postAnswer(answerDto).execute();
                            if (!answerResponse.isSuccessful()) {
                                allAnswersSynced = false;
                            }
                        }

                        // Mark as synced only if all answers were uploaded
                        if (allAnswersSynced) {
                            responseDao.markSyncedWithKey(response.getId(), responseKey);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void syncAllUnsynced() {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                List<Response> unsynced = responseDao.getUnsyncedResponses();
                for (Response response : unsynced) {
                    List<Answer> answers = responseDao.getAnswersForResponseSync(response.getId());
                    // Run sync inline (already on background thread)
                    try {
                        ResponseDto responseDto = new ResponseDto(
                                String.valueOf(response.getSurveyId()),
                                response.getRespondentId() != null ? String.valueOf(response.getRespondentId()) : null,
                                response.getSubmittedAt()
                        );
                        retrofit2.Response<FirebaseKey> fbResponse = apiService.postResponse(responseDto).execute();

                        if (fbResponse.isSuccessful() && fbResponse.body() != null) {
                            String responseKey = fbResponse.body().getName();

                            boolean allAnswersSynced = true;
                            for (Answer answer : answers) {
                                AnswerDto answerDto = new AnswerDto(
                                        responseKey,
                                        String.valueOf(answer.getQuestionId()),
                                        answer.getAnswerValue()
                                );
                                retrofit2.Response<FirebaseKey> answerResponse = apiService.postAnswer(answerDto).execute();
                                if (!answerResponse.isSuccessful()) {
                                    allAnswersSynced = false;
                                }
                            }

                            if (allAnswersSynced) {
                                responseDao.markSyncedWithKey(response.getId(), responseKey);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
