package com.example.surveycollector.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import com.example.surveycollector.ui.adapter.QuestionAdapter;
import com.example.surveycollector.viewmodel.QuestionViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

public class QuestionEditorActivity extends AppCompatActivity implements QuestionAdapter.OnQuestionActionListener {

    private int surveyId = -1;
    private QuestionViewModel viewModel;
    private QuestionAdapter adapter;

    private RecyclerView rvQuestions;
    private LinearLayout emptyState;
    private Toolbar toolbar;

    private List<Question> currentQuestions = new ArrayList<>();
    private List<QuestionOption> currentOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_editor);

        surveyId = getIntent().getIntExtra("surveyId", -1);
        if (surveyId == -1) {
            Toast.makeText(this, "Invalid Survey ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Questions");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvQuestions = findViewById(R.id.rvQuestions);
        emptyState = findViewById(R.id.emptyState);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddQuestion);

        adapter = new QuestionAdapter(this);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(adapter);

        // Retrieve survey title on a background thread
        final android.content.Context appContext = getApplicationContext();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.surveycollector.model.entity.Survey survey = AppDatabase.getInstance(appContext).surveyDao().getSurveyById(surveyId);
            if (survey != null && !isFinishing()) {
                runOnUiThread(() -> {
                    if (!isFinishing() && getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Questions — " + survey.getTitle());
                    }
                });
            }
        });

        viewModel = new ViewModelProvider(this, new QuestionViewModel.Factory(getApplication(), surveyId))
                .get(QuestionViewModel.class);

        viewModel.getQuestionsForSurvey().observe(this, questions -> {
            currentQuestions = questions;
            updateUI();
        });

        viewModel.getOptionsForSurvey().observe(this, options -> {
            currentOptions = options;
            updateUI();
        });

        fabAdd.setOnClickListener(v -> showAddQuestionDialog());
    }

    private void updateUI() {
        if (currentQuestions == null || currentQuestions.isEmpty()) {
            rvQuestions.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            rvQuestions.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
        adapter.setData(currentQuestions, currentOptions);
    }

    private void showAddQuestionDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_question, null);

        TextInputLayout tilQuestionText = dialogView.findViewById(R.id.tilQuestionText);
        TextInputEditText etQuestionText = dialogView.findViewById(R.id.etQuestionText);
        RadioGroup rgQuestionType = dialogView.findViewById(R.id.rgQuestionType);
        LinearLayout layoutOptionsContainer = dialogView.findViewById(R.id.layoutOptionsContainer);
        TextInputLayout tilOptionText = dialogView.findViewById(R.id.tilOptionText);
        TextInputEditText etOptionText = dialogView.findViewById(R.id.etOptionText);
        Button btnAddOption = dialogView.findViewById(R.id.btnAddOption);
        ChipGroup cgOptions = dialogView.findViewById(R.id.cgOptions);

        List<String> tempOptions = new ArrayList<>();

        // Handle Type toggle
        rgQuestionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbText) {
                layoutOptionsContainer.setVisibility(View.GONE);
            } else {
                layoutOptionsContainer.setVisibility(View.VISIBLE);
            }
        });

        // Handle Add Option chip
        btnAddOption.setOnClickListener(v -> {
            String optText = etOptionText.getText() != null ? etOptionText.getText().toString().trim() : "";
            if (optText.isEmpty()) {
                tilOptionText.setError("Option text cannot be empty");
                return;
            }
            tilOptionText.setError(null);

            if (tempOptions.contains(optText)) {
                tilOptionText.setError("Option already added");
                return;
            }

            tempOptions.add(optText);
            etOptionText.setText("");

            Chip chip = new Chip(this);
            chip.setText(optText);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(cv -> {
                cgOptions.removeView(chip);
                tempOptions.remove(optText);
            });
            cgOptions.addView(chip);
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        // Prevent autohide on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String qText = etQuestionText.getText() != null ? etQuestionText.getText().toString().trim() : "";
            if (qText.isEmpty()) {
                tilQuestionText.setError("Question text is required");
                return;
            }
            tilQuestionText.setError(null);

            int checkedType = rgQuestionType.getCheckedRadioButtonId();
            String qType = "TEXT";
            if (checkedType == R.id.rbSingleChoice) {
                qType = "SINGLE_CHOICE";
            } else if (checkedType == R.id.rbMultipleChoice) {
                qType = "MULTIPLE_CHOICE";
            }

            if (!"TEXT".equals(qType) && tempOptions.size() < 2) {
                Toast.makeText(this, "At least 2 options are required for choice questions", Toast.LENGTH_LONG).show();
                return;
            }

            // Save Question & Options to Room
            int orderIndex = currentQuestions != null ? currentQuestions.size() : 0;
            Question newQuestion = new Question(surveyId, qText, qType, orderIndex);

            if ("TEXT".equals(qType)) {
                viewModel.insertQuestion(newQuestion);
            } else {
                viewModel.insertQuestionWithOptions(newQuestion, tempOptions);
            }

            dialog.dismiss();
            Toast.makeText(this, "Question added successfully", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDeleteQuestion(Question question) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question? This will also delete all associated options and answers.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteQuestion(question);
                    Toast.makeText(this, "Question deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
