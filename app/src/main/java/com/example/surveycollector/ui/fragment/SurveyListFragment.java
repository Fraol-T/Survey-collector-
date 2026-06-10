package com.example.surveycollector.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.ui.activity.QuestionEditorActivity;
import com.example.surveycollector.ui.activity.ResponseViewerActivity;
import com.example.surveycollector.ui.adapter.SurveyAdapter;
import com.example.surveycollector.viewmodel.SurveyViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SurveyListFragment extends Fragment implements SurveyAdapter.OnSurveyActionListener {

    private SurveyViewModel viewModel;
    private SurveyAdapter adapter;
    private RecyclerView rvSurveys;
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSurveys = view.findViewById(R.id.rvSurveys);
        emptyState = view.findViewById(R.id.emptyState);
        FloatingActionButton fab = view.findViewById(R.id.fabAddSurvey);

        adapter = new SurveyAdapter(this);
        rvSurveys.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSurveys.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        viewModel.getAllSurveys().observe(getViewLifecycleOwner(), surveys -> {
            if (surveys == null || surveys.isEmpty()) {
                rvSurveys.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                rvSurveys.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
            }
            adapter.setSurveys(surveys != null ? surveys : new java.util.ArrayList<>());
        });

        // FAB -> create mode (no surveyId)
        fab.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            Bundle args = new Bundle();
            args.putInt("surveyId", -1);
            navController.navigate(R.id.action_surveyListFragment_to_createSurveyFragment, args);
        });
    }

    @Override
    public void onEdit(Survey survey) {
        if (!isAdded()) return;
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putInt("surveyId", survey.getId());
        navController.navigate(R.id.action_surveyListFragment_to_createSurveyFragment, args);
    }

    @Override
    public void onDelete(Survey survey) {
        if (!isAdded()) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Survey")
                .setMessage("Are you sure you want to delete \"" + survey.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.delete(survey))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onManageQuestions(Survey survey) {
        if (!isAdded()) return;
        Intent intent = new Intent(requireContext(), QuestionEditorActivity.class);
        intent.putExtra("surveyId", survey.getId());
        startActivity(intent);
    }

    @Override
    public void onGetLink(Survey survey) {
        if (!isAdded()) return;
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putInt("surveyId", survey.getId());
        navController.navigate(R.id.action_surveyListFragment_to_surveyLinkFragment, args);
    }

    @Override
    public void onViewResponses(Survey survey) {
        if (!isAdded()) return;
        Intent intent = new Intent(requireContext(), ResponseViewerActivity.class);
        intent.putExtra("surveyId", survey.getId());
        intent.putExtra("surveyTitle", survey.getTitle());
        startActivity(intent);
    }
}
