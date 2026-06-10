package com.example.surveycollector.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.surveycollector.R;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.util.SessionManager;
import com.example.surveycollector.viewmodel.SurveyViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Date;

public class CreateSurveyFragment extends Fragment {

    private TextInputLayout tilTitle;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private SwitchMaterial switchRegistration;
    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private TextView tvFormTitle;

    private SurveyViewModel viewModel;
    private int surveyId = -1;
    private Survey existingSurvey = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_survey, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tilTitle = view.findViewById(R.id.tilSurveyTitle);
        etTitle = view.findViewById(R.id.etSurveyTitle);
        etDescription = view.findViewById(R.id.etSurveyDescription);
        switchRegistration = view.findViewById(R.id.switchRegistration);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
        tvFormTitle = view.findViewById(R.id.tvFormTitle);

        viewModel = new ViewModelProvider(this).get(SurveyViewModel.class);

        // Check if editing
        if (getArguments() != null) {
            surveyId = getArguments().getInt("surveyId", -1);
        }

        if (surveyId > 0) {
            tvFormTitle.setText("Edit Survey");
            viewModel.getSurveyById(surveyId).observe(getViewLifecycleOwner(), survey -> {
                if (survey != null && existingSurvey == null) {
                    existingSurvey = survey;
                    etTitle.setText(survey.getTitle());
                    etDescription.setText(survey.getDescription());
                    switchRegistration.setChecked(survey.getRequiresRegistration() == 1);
                }
            });
        }

        btnSave.setOnClickListener(v -> handleSave());
        btnCancel.setOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    private void handleSave() {
        if (!isAdded()) return;

        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        boolean requiresRegistration = switchRegistration.isChecked();

        if (title.isEmpty()) {
            tilTitle.setError("Title is required");
            return;
        } else {
            tilTitle.setError(null);
        }

        int adminId = SessionManager.getInstance(requireContext()).getAdminId();

        if (existingSurvey != null) {
            // Update mode
            existingSurvey.setTitle(title);
            existingSurvey.setDescription(description);
            existingSurvey.setRequiresRegistration(requiresRegistration ? 1 : 0);
            viewModel.update(existingSurvey);
        } else {
            // Create mode
            Survey newSurvey = new Survey(
                    adminId,
                    title,
                    description,
                    requiresRegistration ? 1 : 0,
                    1, // isActive = true
                    new Date().toString()
            );
            viewModel.insert(newSurvey);
        }

        if (isAdded()) {
            Navigation.findNavController(requireView()).popBackStack();
        }
    }
}
