package com.example.surveycollector.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.surveycollector.R;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.AdminDao;
import com.example.surveycollector.model.entity.Admin;
import com.example.surveycollector.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.ProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.FirebaseNetworkException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import androidx.annotation.NonNull;
import com.example.surveycollector.model.dao.SurveyDao;
import com.example.surveycollector.model.dao.QuestionDao;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import com.example.surveycollector.model.entity.SurveyLink;
import com.example.surveycollector.model.remote.ApiClient;
import com.example.surveycollector.model.remote.ApiService;
import com.example.surveycollector.model.remote.dto.FirebaseKey;
import com.example.surveycollector.model.remote.dto.SurveyDto;
import com.example.surveycollector.model.remote.dto.QuestionDto;
import com.example.surveycollector.model.remote.dto.QuestionOptionDto;
import com.example.surveycollector.model.remote.dto.SurveyLinkDto;
import com.example.surveycollector.util.NetworkErrorManager;
import java.io.IOException;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    
    private SessionManager sessionManager;
    private AppDatabase db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = SessionManager.getInstance(this);
        db = AppDatabase.getInstance(this);
        mAuth = FirebaseAuth.getInstance();

        // Check if already logged in
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);

        // Seed default admin if needed
        seedDefaultAdminIfNeeded();

        // Observe network errors
        NetworkErrorManager.getErrorEvent().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin(v);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void seedDefaultAdminIfNeeded() {
        AppDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                AdminDao adminDao = db.adminDao();
                if (adminDao.getCount() == 0) {
                    Admin defaultAdmin = new Admin("admin@sc.com", "admin123", new Date().toString());
                    adminDao.insert(defaultAdmin);
                }
                
                // Seed Default Admin to Firebase Auth in a non-blocking check
                mAuth.createUserWithEmailAndPassword("admin@sc.com", "admin123")
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            // Ignored if already exists
                        }
                    });
                
                seedDemoSurveyIfNeeded();
            }
        });
    }

    private void seedDemoSurveyIfNeeded() {
        SurveyDao surveyDao = db.surveyDao();
        if (surveyDao.getSurveyByTitleSync("Course Feedback") != null) {
            return; // Already seeded
        }

        // Get Admin ID
        Admin admin = db.adminDao().getByEmail("admin@sc.com");
        int adminId = (admin != null) ? admin.getId() : 1;

        // Create Survey
        Survey demoSurvey = new Survey(
                adminId,
                "Course Feedback",
                "Please provide feedback on your experience in this course.",
                1, // requiresRegistration = true
                1, // isActive = true
                new Date().toString()
        );
        long surveyId = surveyDao.insert(demoSurvey);
        demoSurvey.setId((int) surveyId);

        // Sync Survey to cloud
        ApiService apiService = ApiClient.getApiService();
        String surveyFirebaseKey = "";
        try {
            SurveyDto surveyDto = new SurveyDto(
                    demoSurvey.getAdminId(),
                    demoSurvey.getTitle(),
                    demoSurvey.getDescription(),
                    demoSurvey.getRequiresRegistration() == 1,
                    demoSurvey.getIsActive() == 1,
                    demoSurvey.getCreatedAt()
            );
            retrofit2.Response<FirebaseKey> response = apiService.postSurvey(surveyDto).execute();
            if (response.isSuccessful() && response.body() != null) {
                surveyFirebaseKey = response.body().getName();
                demoSurvey.setFirebaseKey(surveyFirebaseKey);
                surveyDao.update(demoSurvey);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Generate Link for this Survey (d4e2a6b0-807f-46ec-8dd4-887b793b8b0b)
        SurveyLink demoLink = new SurveyLink(
                (int) surveyId,
                "d4e2a6b0-807f-46ec-8dd4-887b793b8b0b",
                1, // isActive = true
                new Date().toString()
        );
        long linkId = db.surveyLinkDao().insert(demoLink);
        demoLink.setId((int) linkId);

        // Sync Link to cloud
        try {
            SurveyLinkDto linkDto = new SurveyLinkDto(
                    demoLink.getSurveyId(),
                    demoLink.getToken(),
                    demoLink.getIsActive() == 1,
                    demoLink.getCreatedAt()
            );
            retrofit2.Response<FirebaseKey> response = apiService.postSurveyLink(linkDto).execute();
            if (response.isSuccessful() && response.body() != null) {
                demoLink.setFirebaseKey(response.body().getName());
                db.surveyLinkDao().update(demoLink);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Seed Questions
        // Q1: 'How would you rate the course?' (SINGLE_CHOICE: Excellent/Good/Average/Poor)
        // Q2: 'What was the most useful topic?' (TEXT)
        // Q3: 'Would you recommend this course?' (SINGLE_CHOICE: Yes/No)

        QuestionDao questionDao = db.questionDao();

        // Q1
        Question q1 = new Question((int) surveyId, "How would you rate the course?", "SINGLE_CHOICE", 0);
        long q1Id = questionDao.insert(q1);
        q1.setId((int) q1Id);
        String q1FirebaseKey = syncQuestionToFirebase(apiService, q1);
        if (q1FirebaseKey != null) {
            q1.setFirebaseKey(q1FirebaseKey);
            questionDao.update(q1);
        }

        // Options for Q1
        String[] q1Options = {"Excellent", "Good", "Average", "Poor"};
        for (int i = 0; i < q1Options.length; i++) {
            QuestionOption opt = new QuestionOption((int) q1Id, q1Options[i], i);
            long optId = questionDao.insertOption(opt);
            opt.setId((int) optId);
            if (q1FirebaseKey != null) {
                String optFbKey = syncOptionToFirebase(apiService, opt, q1FirebaseKey);
                if (optFbKey != null) {
                    opt.setFirebaseKey(optFbKey);
                    questionDao.updateOption(opt);
                }
            }
        }

        // Q2
        Question q2 = new Question((int) surveyId, "What was the most useful topic?", "TEXT", 1);
        long q2Id = questionDao.insert(q2);
        q2.setId((int) q2Id);
        String q2FirebaseKey = syncQuestionToFirebase(apiService, q2);
        if (q2FirebaseKey != null) {
            q2.setFirebaseKey(q2FirebaseKey);
            questionDao.update(q2);
        }

        // Q3
        Question q3 = new Question((int) surveyId, "Would you recommend this course?", "SINGLE_CHOICE", 2);
        long q3Id = questionDao.insert(q3);
        q3.setId((int) q3Id);
        String q3FirebaseKey = syncQuestionToFirebase(apiService, q3);
        if (q3FirebaseKey != null) {
            q3.setFirebaseKey(q3FirebaseKey);
            questionDao.update(q3);
        }

        // Options for Q3
        String[] q3Options = {"Yes", "No"};
        for (int i = 0; i < q3Options.length; i++) {
            QuestionOption opt = new QuestionOption((int) q3Id, q3Options[i], i);
            long optId = questionDao.insertOption(opt);
            opt.setId((int) optId);
            if (q3FirebaseKey != null) {
                String optFbKey = syncOptionToFirebase(apiService, opt, q3FirebaseKey);
                if (optFbKey != null) {
                    opt.setFirebaseKey(optFbKey);
                    questionDao.updateOption(opt);
                }
            }
        }
    }

    private String syncQuestionToFirebase(ApiService apiService, Question q) {
        try {
            QuestionDto dto = new QuestionDto(
                    String.valueOf(q.getSurveyId()),
                    q.getText(),
                    q.getType(),
                    q.getOrderIndex()
            );
            retrofit2.Response<FirebaseKey> response = apiService.postQuestion(dto).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().getName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String syncOptionToFirebase(ApiService apiService, QuestionOption opt, String questionFirebaseKey) {
        try {
            QuestionOptionDto dto = new QuestionOptionDto(
                    questionFirebaseKey,
                    opt.getText(),
                    opt.getOrderIndex()
            );
            retrofit2.Response<FirebaseKey> response = apiService.postQuestionOption(dto).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body().getName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleLogin(View view) {
        final String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean hasError = false;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format");
            hasError = true;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            hasError = true;
        } else {
            tilPassword.setError(null);
        }

        if (hasError) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        if (btnRegister != null) btnRegister.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase authentication successful
                            AppDatabase.databaseWriteExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    AdminDao adminDao = db.adminDao();
                                    Admin admin = adminDao.getByEmail(email);
                                    final long localId;
                                    if (admin == null) {
                                        // User registered online but doesn't exist locally, cache them
                                        Admin newAdmin = new Admin(email, password, new Date().toString());
                                        localId = adminDao.insert(newAdmin);
                                    } else {
                                        // Exists locally, update password if it changed
                                        if (!admin.getPassword().equals(password)) {
                                            admin.setPassword(password);
                                            adminDao.insert(admin); // REPLACE conflict strategy
                                        }
                                        localId = admin.getId();
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setVisibility(View.GONE);
                                            btnLogin.setEnabled(true);
                                            if (btnRegister != null) btnRegister.setEnabled(true);

                                            sessionManager.saveSession((int) localId);
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            });
                        } else {
                            // Firebase auth failed
                            if (task.getException() instanceof FirebaseNetworkException) {
                                // Network failure, attempt offline fallback verification using local DB
                                AppDatabase.databaseWriteExecutor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        AdminDao adminDao = db.adminDao();
                                        final Admin admin = adminDao.getByEmail(email);

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressBar.setVisibility(View.GONE);
                                                btnLogin.setEnabled(true);
                                                if (btnRegister != null) btnRegister.setEnabled(true);

                                                if (admin != null && admin.getPassword().equals(password)) {
                                                    sessionManager.saveSession(admin.getId());
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Snackbar.make(view, "Invalid credentials (offline)", Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                // Online validation failed
                                progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                if (btnRegister != null) btnRegister.setEnabled(true);
                                String errorMsg = task.getException() != null ? task.getException().getMessage() : "Invalid credentials";
                                Snackbar.make(view, errorMsg, Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
