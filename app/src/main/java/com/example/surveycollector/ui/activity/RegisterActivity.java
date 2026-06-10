package com.example.surveycollector.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.surveycollector.R;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.dao.AdminDao;
import com.example.surveycollector.model.entity.Admin;
import com.example.surveycollector.util.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilRegisterEmail;
    private TextInputLayout tilRegisterPassword;
    private TextInputLayout tilRegisterConfirmPassword;
    private TextInputEditText etRegisterEmail;
    private TextInputEditText etRegisterPassword;
    private TextInputEditText etRegisterConfirmPassword;
    private MaterialButton btnRegisterSubmit;
    private MaterialButton btnBackToLogin;
    private ProgressBar progressBarRegister;

    private FirebaseAuth mAuth;
    private AppDatabase db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = AppDatabase.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        tilRegisterEmail = findViewById(R.id.tilRegisterEmail);
        tilRegisterPassword = findViewById(R.id.tilRegisterPassword);
        tilRegisterConfirmPassword = findViewById(R.id.tilRegisterConfirmPassword);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        progressBarRegister = findViewById(R.id.progressBarRegister);

        btnRegisterSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration(v);
            }
        });

        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void handleRegistration(View view) {
        final String email = etRegisterEmail.getText() != null ? etRegisterEmail.getText().toString().trim() : "";
        final String password = etRegisterPassword.getText() != null ? etRegisterPassword.getText().toString().trim() : "";
        String confirmPassword = etRegisterConfirmPassword.getText() != null ? etRegisterConfirmPassword.getText().toString().trim() : "";

        boolean hasError = false;

        if (email.isEmpty()) {
            tilRegisterEmail.setError("Email is required");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilRegisterEmail.setError("Invalid email address format");
            hasError = true;
        } else {
            tilRegisterEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilRegisterPassword.setError("Password is required");
            hasError = true;
        } else if (password.length() < 6) {
            tilRegisterPassword.setError("Password must be at least 6 characters");
            hasError = true;
        } else {
            tilRegisterPassword.setError(null);
        }

        if (confirmPassword.isEmpty()) {
            tilRegisterConfirmPassword.setError("Confirm password is required");
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            tilRegisterConfirmPassword.setError("Passwords do not match");
            hasError = true;
        } else {
            tilRegisterConfirmPassword.setError(null);
        }

        if (hasError) {
            return;
        }

        progressBarRegister.setVisibility(View.VISIBLE);
        btnRegisterSubmit.setEnabled(false);
        btnBackToLogin.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Firebase Registration successful, now insert locally in Room
                            AppDatabase.databaseWriteExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    AdminDao adminDao = db.adminDao();
                                    // Verify user doesn't already exist locally (safety check)
                                    Admin existing = adminDao.getByEmail(email);
                                    final long localId;
                                    if (existing == null) {
                                        Admin newAdmin = new Admin(email, password, new Date().toString());
                                        localId = adminDao.insert(newAdmin);
                                    } else {
                                        localId = existing.getId();
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBarRegister.setVisibility(View.GONE);
                                            btnRegisterSubmit.setEnabled(true);
                                            btnBackToLogin.setEnabled(true);

                                            sessionManager.saveSession((int) localId);
                                            Toast.makeText(RegisterActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();

                                            // Go to MainActivity and clear back stack
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        } else {
                            // Firebase registration failed
                            progressBarRegister.setVisibility(View.GONE);
                            btnRegisterSubmit.setEnabled(true);
                            btnBackToLogin.setEnabled(true);
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Snackbar.make(view, errorMsg, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
