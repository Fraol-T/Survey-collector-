package com.example.surveycollector.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SyncService extends Service {

    private static final String TAG = "SyncService";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "SyncService started");

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            ResponseDao responseDao = db.responseDao();
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            // 1. Get all unsynced responses
            List<Response> unsyncedResponses = responseDao.getUnsyncedResponses();
            Log.d(TAG, "Found " + unsyncedResponses.size() + " unsynced responses");

            for (Response response : unsyncedResponses) {
                // 2. Get answers for each response
                List<Answer> answers = responseDao.getAnswersForResponseSync(response.getId());

                // 3. POST response to Firebase
                try {
                    ResponseDto responseDto = new ResponseDto(
                            String.valueOf(response.getSurveyId()),
                            response.getRespondentId() != null
                                    ? String.valueOf(response.getRespondentId()) : null,
                            response.getSubmittedAt()
                    );
                    retrofit2.Response<FirebaseKey> fbResponse =
                            apiService.postResponse(responseDto).execute();

                    if (fbResponse.isSuccessful() && fbResponse.body() != null) {
                        String responseKey = fbResponse.body().getName();

                        // POST each answer
                        boolean allAnswersSynced = true;
                        for (Answer answer : answers) {
                            AnswerDto answerDto = new AnswerDto(
                                    responseKey,
                                    String.valueOf(answer.getQuestionId()),
                                    answer.getAnswerValue()
                            );
                            retrofit2.Response<FirebaseKey> answerResponse =
                                    apiService.postAnswer(answerDto).execute();
                            if (!answerResponse.isSuccessful()) {
                                allAnswersSynced = false;
                                Log.w(TAG, "Failed to sync answer for response " + response.getId());
                            }
                        }

                        // 4. Mark synced on success
                        if (allAnswersSynced) {
                            responseDao.markSyncedWithKey(response.getId(), responseKey);
                            Log.d(TAG, "Response " + response.getId() + " synced successfully");
                        }
                    } else {
                        Log.w(TAG, "Failed to POST response " + response.getId()
                                + ": " + fbResponse.code());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Network error syncing response " + response.getId(), e);
                }
            }

            // 5. Stop self after all processed
            Log.d(TAG, "Sync complete, stopping service");
            stopSelf(startId);
        });

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        Log.d(TAG, "SyncService destroyed");
    }
}
