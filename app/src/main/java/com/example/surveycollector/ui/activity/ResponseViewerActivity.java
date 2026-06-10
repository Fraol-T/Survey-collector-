package com.example.surveycollector.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.entity.Answer;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.Response;
import com.example.surveycollector.model.entity.Respondent;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.AnswerDto;
import com.example.surveycollector.model.remote.dto.RespondentDto;
import com.example.surveycollector.model.remote.dto.ResponseDto;
import com.example.surveycollector.viewmodel.ResponseViewModel;
import android.widget.ProgressBar;
import com.example.surveycollector.util.NetworkErrorManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;

public class ResponseViewerActivity extends AppCompatActivity {

    private static final String TAG = "ResponseViewerActivity";

    private int surveyId = -1;
    private String surveyTitle = "";
    private ResponseViewModel viewModel;
    private ResponseAdapter adapter;

    private RecyclerView rvResponses;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private List<Response> currentResponses = new ArrayList<>();
    private Map<Integer, Respondent> respondentCache = new HashMap<>();
    private List<Question> questionsList = new ArrayList<>();

    private boolean isSyncing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response_viewer);

        surveyId = getIntent().getIntExtra("surveyId", -1);
        surveyTitle = getIntent().getStringExtra("surveyTitle");

        if (surveyId == -1) {
            Toast.makeText(this, "Invalid Survey", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle((surveyTitle != null ? surveyTitle : "Survey") + " — Responses");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvResponses = findViewById(R.id.rvResponses);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        adapter = new ResponseAdapter();
        rvResponses.setLayoutManager(new LinearLayoutManager(this));
        rvResponses.setAdapter(adapter);

        // Observe network errors
        NetworkErrorManager.getErrorEvent().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel = new ViewModelProvider(this, new ResponseViewModel.Factory(getApplication(), surveyId))
                .get(ResponseViewModel.class);

        viewModel.getResponsesForSurvey().observe(this, responses -> {
            currentResponses = responses;
            loadDataAndCache();
        });

        // Auto-sync from Firebase on first open
        syncResponsesFromFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_response_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            syncResponsesFromFirebase();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Syncs responses from Firebase Realtime Database into the local Room database.
     *
     * The strategy:
     * 1. Look up the local survey's firebaseKey to identify which remote responses belong to it.
     * 2. Fetch all responses, respondents, and answers from Firebase in parallel.
     * 3. Filter responses that match our survey's firebaseKey.
     * 4. For each new response (not yet in local DB by firebaseKey):
     *    a. If it references a respondent, insert/lookup that respondent locally.
     *    b. Insert the response into Room.
     *    c. Map question firebaseKeys -> local question IDs and insert answers.
     * 5. Room LiveData will auto-update the UI once new rows are inserted.
     */
    private void syncResponsesFromFirebase() {
        if (isSyncing) {
            Toast.makeText(this, "Sync already in progress…", Toast.LENGTH_SHORT).show();
            return;
        }
        isSyncing = true;
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Syncing responses from cloud…", Toast.LENGTH_SHORT).show();

        // The web form stores surveyId as the local Room integer ID (e.g. "1", "2"),
        // NOT the Firebase push key. So we filter responses by String.valueOf(surveyId).
        final String surveyIdStr = String.valueOf(surveyId);

        // Fetch responses, respondents, and answers from Firebase
        ApiService api = ApiClient.getApiService();

        // We need all three calls to complete. Use a simple counter approach.
        final Map<String, ResponseDto>[] responsesHolder = new Map[]{null};
        final Map<String, RespondentDto>[] respondentsHolder = new Map[]{null};
        final Map<String, AnswerDto>[] answersHolder = new Map[]{null};
        final int[] completedCalls = {0};
        final boolean[] hasError = {false};

        Callback<Map<String, ResponseDto>> responsesCallback = new Callback<Map<String, ResponseDto>>() {
            @Override
            public void onResponse(Call<Map<String, ResponseDto>> call, retrofit2.Response<Map<String, ResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    responsesHolder[0] = response.body();
                } else {
                    responsesHolder[0] = new HashMap<>();
                }
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }

            @Override
            public void onFailure(Call<Map<String, ResponseDto>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch responses", t);
                hasError[0] = true;
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }
        };

        Callback<Map<String, RespondentDto>> respondentsCallback = new Callback<Map<String, RespondentDto>>() {
            @Override
            public void onResponse(Call<Map<String, RespondentDto>> call, retrofit2.Response<Map<String, RespondentDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    respondentsHolder[0] = response.body();
                } else {
                    respondentsHolder[0] = new HashMap<>();
                }
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }

            @Override
            public void onFailure(Call<Map<String, RespondentDto>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch respondents", t);
                hasError[0] = true;
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }
        };

        Callback<Map<String, AnswerDto>> answersCallback = new Callback<Map<String, AnswerDto>>() {
            @Override
            public void onResponse(Call<Map<String, AnswerDto>> call, retrofit2.Response<Map<String, AnswerDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    answersHolder[0] = response.body();
                } else {
                    answersHolder[0] = new HashMap<>();
                }
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }

            @Override
            public void onFailure(Call<Map<String, AnswerDto>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch answers", t);
                hasError[0] = true;
                checkAndProcessSync(surveyIdStr, responsesHolder, respondentsHolder, answersHolder, completedCalls, hasError);
            }
        };

        // Fire all three requests
        api.getResponses().enqueue(responsesCallback);
        api.getRespondents().enqueue(respondentsCallback);
        api.getAnswers().enqueue(answersCallback);
    }

    /**
     * Called each time a Firebase fetch completes. Once all 3 are done, processes the data.
     */
    private synchronized void checkAndProcessSync(
            String surveyIdStr,
            Map<String, ResponseDto>[] responsesHolder,
            Map<String, RespondentDto>[] respondentsHolder,
            Map<String, AnswerDto>[] answersHolder,
            int[] completedCalls,
            boolean[] hasError) {

        completedCalls[0]++;
        if (completedCalls[0] < 3) return; // Wait for all 3 calls

        if (hasError[0]) {
            runOnUiThread(() -> {
                isSyncing = false;
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to sync some data. Please try again.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        // Process on background thread
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                processFirebaseData(surveyIdStr, responsesHolder[0], respondentsHolder[0], answersHolder[0]);
                runOnUiThread(() -> {
                    isSyncing = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Responses synced successfully!", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error processing sync data", e);
                runOnUiThread(() -> {
                    isSyncing = false;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error processing synced data.", Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Processes fetched Firebase data and inserts new records into Room.
     */
    private void processFirebaseData(
            String surveyIdStr,
            Map<String, ResponseDto> remoteResponses,
            Map<String, RespondentDto> remoteRespondents,
            Map<String, AnswerDto> remoteAnswers) {

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());

        Log.d(TAG, "Looking for responses with surveyId = \"" + surveyIdStr + "\" among " + remoteResponses.size() + " remote responses");

        // Filter responses that belong to this survey
        // The web form stores surveyId as the local Room ID string (e.g. "1", "2")
        Map<String, ResponseDto> surveyResponses = new HashMap<>();
        for (Map.Entry<String, ResponseDto> entry : remoteResponses.entrySet()) {
            String responseFirebaseKey = entry.getKey();
            ResponseDto dto = entry.getValue();
            Log.d(TAG, "Remote response [" + responseFirebaseKey + "] has surveyId=\"" + dto.getSurveyId() + "\"");
            if (surveyIdStr.equals(dto.getSurveyId())) {
                dto.setFirebaseKey(responseFirebaseKey);
                surveyResponses.put(responseFirebaseKey, dto);
            }
        }

        Log.d(TAG, "Matched " + surveyResponses.size() + " responses for surveyId=\"" + surveyIdStr + "\"");

        if (surveyResponses.isEmpty()) {
            Log.d(TAG, "No remote responses found for this survey.");
            return;
        }

        // Build a map of respondent firebase keys to DTOs
        Map<String, RespondentDto> respondentMap = new HashMap<>();
        if (remoteRespondents != null) {
            for (Map.Entry<String, RespondentDto> entry : remoteRespondents.entrySet()) {
                RespondentDto dto = entry.getValue();
                dto.setFirebaseKey(entry.getKey());
                respondentMap.put(entry.getKey(), dto);
            }
        }

        // Build a map of answer firebase keys grouped by their response firebase key
        Map<String, List<AnswerDto>> answersByResponseKey = new HashMap<>();
        if (remoteAnswers != null) {
            for (Map.Entry<String, AnswerDto> entry : remoteAnswers.entrySet()) {
                AnswerDto dto = entry.getValue();
                dto.setFirebaseKey(entry.getKey());
                String responseKey = dto.getResponseId();
                if (responseKey != null && surveyResponses.containsKey(responseKey)) {
                    answersByResponseKey.computeIfAbsent(responseKey, k -> new ArrayList<>()).add(dto);
                }
            }
        }

        // Process each response
        for (Map.Entry<String, ResponseDto> entry : surveyResponses.entrySet()) {
            String responseFirebaseKey = entry.getKey();
            ResponseDto responseDto = entry.getValue();

            // Check if this response already exists locally
            Response existing = db.responseDao().getResponseByFirebaseKeySync(responseFirebaseKey);
            if (existing != null) {
                Log.d(TAG, "Response already exists locally: " + responseFirebaseKey);
                continue; // Skip duplicate
            }

            // Resolve respondent
            Integer localRespondentId = null;
            String respondentFbKey = responseDto.getRespondentId();
            if (respondentFbKey != null && !respondentFbKey.isEmpty()) {
                // Check if respondent already exists in local DB
                Respondent localRespondent = db.responseDao().getRespondentByFirebaseKeySync(respondentFbKey);
                if (localRespondent != null) {
                    localRespondentId = localRespondent.getId();
                } else {
                    // Insert respondent from Firebase data
                    RespondentDto respondentDto = respondentMap.get(respondentFbKey);
                    if (respondentDto != null) {
                        Respondent newRespondent = new Respondent(
                                respondentDto.getName() != null ? respondentDto.getName() : "Unknown",
                                respondentDto.getEmail() != null ? respondentDto.getEmail() : "unknown@email.com",
                                respondentDto.getRegisteredAt() != null ? respondentDto.getRegisteredAt() : ""
                        );
                        newRespondent.setFirebaseKey(respondentFbKey);
                        long insertedId = db.responseDao().insertRespondent(newRespondent);
                        localRespondentId = (int) insertedId;
                    }
                }
            }

            // Insert the response
            Response newResponse = new Response(
                    surveyId,
                    localRespondentId,
                    responseDto.getSubmittedAt() != null ? responseDto.getSubmittedAt() : "",
                    1 // synced = 1, it came from the cloud
            );
            newResponse.setFirebaseKey(responseFirebaseKey);
            long newResponseId = db.responseDao().insert(newResponse);

            // Insert answers for this response
            List<AnswerDto> answerDtos = answersByResponseKey.get(responseFirebaseKey);
            if (answerDtos != null && !answerDtos.isEmpty()) {
                List<Answer> answersToInsert = new ArrayList<>();
                for (AnswerDto answerDto : answerDtos) {
                    // Map question firebase key to local question ID
                    String questionFbKey = answerDto.getQuestionId();
                    if (questionFbKey != null) {
                        Question localQuestion = db.questionDao().getQuestionByFirebaseKeySync(questionFbKey);
                        if (localQuestion != null) {
                            Answer answer = new Answer(
                                    (int) newResponseId,
                                    localQuestion.getId(),
                                    answerDto.getAnswerValue() != null ? answerDto.getAnswerValue() : ""
                            );
                            answersToInsert.add(answer);
                        } else {
                            Log.w(TAG, "Could not find local question for firebase key: " + questionFbKey);
                        }
                    }
                }
                if (!answersToInsert.isEmpty()) {
                    db.responseDao().insertAnswers(answersToInsert);
                }
            }

            Log.d(TAG, "Inserted response " + newResponseId + " with firebase key " + responseFirebaseKey);
        }
    }

    private void loadDataAndCache() {
        progressBar.setVisibility(View.VISIBLE);
        rvResponses.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        final android.content.Context appContext = getApplicationContext();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(appContext);

            // 1. Fetch questions
            List<Question> questions = db.questionDao().getQuestionsForSurveySync(surveyId);

            // 2. Fetch respondents referenced by these responses
            Map<Integer, Respondent> cache = new HashMap<>();
            for (Response r : currentResponses) {
                if (r.getRespondentId() != null) {
                    Respondent resp = db.responseDao().getRespondentByIdSync(r.getRespondentId());
                    if (resp != null) {
                        cache.put(r.getRespondentId(), resp);
                    }
                }
            }

            if (!isFinishing()) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        this.questionsList = questions;
                        this.respondentCache = cache;
                        progressBar.setVisibility(View.GONE);
                        updateUI();
                    }
                });
            }
        });
    }

    private void updateUI() {
        if (currentResponses == null || currentResponses.isEmpty()) {
            rvResponses.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvResponses.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            adapter.setData(currentResponses, respondentCache, questionsList);
        }
    }

    // Response Adapter nested inside the activity
    private class ResponseAdapter extends RecyclerView.Adapter<ResponseAdapter.ResponseViewHolder> {

        private List<Response> responses = new ArrayList<>();
        private Map<Integer, Respondent> respondents = new HashMap<>();
        private List<Question> questions = new ArrayList<>();
        private final Set<Integer> expandedResponseIds = new HashSet<>();
        private final Map<Integer, List<Answer>> answersCache = new HashMap<>();

        public void setData(List<Response> responses, Map<Integer, Respondent> respondents, List<Question> questions) {
            this.responses = responses != null ? responses : new ArrayList<>();
            this.respondents = respondents != null ? respondents : new HashMap<>();
            this.questions = questions != null ? questions : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ResponseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_response, parent, false);
            return new ResponseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ResponseViewHolder holder, int position) {
            holder.bind(responses.get(position));
        }

        @Override
        public int getItemCount() {
            return responses.size();
        }

        class ResponseViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvNumber;
            private final TextView tvRespondent;
            private final TextView tvTimestamp;
            private final ImageButton btnExpand;
            private final LinearLayout layoutAnswersContainer;
            private final LinearLayout layoutAnswersList;

            public ResponseViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNumber = itemView.findViewById(R.id.tvResponseNumber);
                tvRespondent = itemView.findViewById(R.id.tvRespondentName);
                tvTimestamp = itemView.findViewById(R.id.tvResponseTimestamp);
                btnExpand = itemView.findViewById(R.id.btnExpand);
                layoutAnswersContainer = itemView.findViewById(R.id.layoutAnswersContainer);
                layoutAnswersList = itemView.findViewById(R.id.layoutAnswersList);
            }

            public void bind(Response response) {
                tvNumber.setText("Response #" + response.getId());
                tvTimestamp.setText("Submitted: " + response.getSubmittedAt());

                if (response.getRespondentId() == null) {
                    tvRespondent.setText("Anonymous");
                } else {
                    Respondent resp = respondents.get(response.getRespondentId());
                    if (resp != null) {
                        tvRespondent.setText(resp.getName() + " (" + resp.getEmail() + ")");
                    } else {
                        tvRespondent.setText("Anonymous (Unknown Respondent)");
                    }
                }

                boolean isExpanded = expandedResponseIds.contains(response.getId());
                if (isExpanded) {
                    layoutAnswersContainer.setVisibility(View.VISIBLE);
                    btnExpand.setRotation(180f);
                    
                    // Render Answers
                    renderAnswers(response);
                } else {
                    layoutAnswersContainer.setVisibility(View.GONE);
                    btnExpand.setRotation(0f);
                }

                View.OnClickListener toggleClick = v -> {
                    if (isExpanded) {
                        expandedResponseIds.remove(response.getId());
                    } else {
                        expandedResponseIds.add(response.getId());
                    }
                    notifyItemChanged(getAdapterPosition());
                };

                btnExpand.setOnClickListener(toggleClick);
                itemView.setOnClickListener(toggleClick);
            }

            private void renderAnswers(Response response) {
                int rId = response.getId();
                if (answersCache.containsKey(rId)) {
                    populateAnswersView(answersCache.get(rId));
                } else {
                    layoutAnswersList.removeAllViews();
                    TextView tvLoading = new TextView(itemView.getContext());
                    tvLoading.setText("Loading answers...");
                    tvLoading.setTextColor(0xFF64748B);
                    tvLoading.setTextSize(14);
                    layoutAnswersList.addView(tvLoading);

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        List<Answer> answers = AppDatabase.getInstance(itemView.getContext())
                                .responseDao().getAnswersForResponseSync(rId);
                        runOnUiThread(() -> {
                            answersCache.put(rId, answers);
                            if (expandedResponseIds.contains(rId)) {
                                populateAnswersView(answers);
                            }
                        });
                    });
                }
            }

            private void populateAnswersView(List<Answer> answers) {
                layoutAnswersList.removeAllViews();
                if (answers == null || answers.isEmpty()) {
                    TextView tvNoAnswers = new TextView(itemView.getContext());
                    tvNoAnswers.setText("No answers submitted for this response.");
                    tvNoAnswers.setTextColor(0xFF64748B);
                    tvNoAnswers.setTextSize(14);
                    layoutAnswersList.addView(tvNoAnswers);
                    return;
                }

                for (Answer ans : answers) {
                    View row = LayoutInflater.from(itemView.getContext())
                            .inflate(R.layout.item_response_answer, layoutAnswersList, false);
                    TextView tvQ = row.findViewById(R.id.tvQuestionText);
                    TextView tvA = row.findViewById(R.id.tvAnswerValue);

                    String qText = "Question (ID: " + ans.getQuestionId() + ")";
                    for (Question q : questions) {
                        if (q.getId() == ans.getQuestionId()) {
                            qText = q.getText();
                            break;
                        }
                    }

                    tvQ.setText(qText);
                    tvA.setText(ans.getAnswerValue());
                    layoutAnswersList.addView(row);
                }
            }
        }
    }
}
