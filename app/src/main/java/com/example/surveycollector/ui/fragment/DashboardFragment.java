package com.example.surveycollector.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.entity.Survey;
import com.example.surveycollector.ui.activity.ResponseViewerActivity;
import com.example.surveycollector.viewmodel.DashboardViewModel;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView tvTotalSurveys;
    private TextView tvTotalResponses;
    private TextView tvActiveSurveys;
    private RecyclerView rvRecentSurveys;
    private TextView tvEmptyRecent;
    private RecentSurveysAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTotalSurveys = view.findViewById(R.id.tvTotalSurveys);
        tvTotalResponses = view.findViewById(R.id.tvTotalResponses);
        tvActiveSurveys = view.findViewById(R.id.tvActiveSurveys);
        rvRecentSurveys = view.findViewById(R.id.rvRecentSurveys);
        tvEmptyRecent = view.findViewById(R.id.tvEmptyRecent);

        rvRecentSurveys.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RecentSurveysAdapter(survey -> {
            Intent intent = new Intent(requireContext(), ResponseViewerActivity.class);
            intent.putExtra("surveyId", survey.getId());
            intent.putExtra("surveyTitle", survey.getTitle());
            startActivity(intent);
        });
        rvRecentSurveys.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        viewModel.getTotalSurveys().observe(getViewLifecycleOwner(), count -> 
            tvTotalSurveys.setText(String.valueOf(count != null ? count : 0))
        );

        viewModel.getTotalResponses().observe(getViewLifecycleOwner(), count -> 
            tvTotalResponses.setText(String.valueOf(count != null ? count : 0))
        );

        viewModel.getActiveSurveys().observe(getViewLifecycleOwner(), count -> 
            tvActiveSurveys.setText(String.valueOf(count != null ? count : 0))
        );

        viewModel.getRecentSurveys().observe(getViewLifecycleOwner(), surveys -> {
            if (surveys == null || surveys.isEmpty()) {
                rvRecentSurveys.setVisibility(View.GONE);
                tvEmptyRecent.setVisibility(View.VISIBLE);
            } else {
                rvRecentSurveys.setVisibility(View.VISIBLE);
                tvEmptyRecent.setVisibility(View.GONE);
                adapter.setSurveys(surveys);
            }
        });
    }

    private static class RecentSurveysAdapter extends RecyclerView.Adapter<RecentSurveysAdapter.RecentViewHolder> {

        public interface OnRecentSurveyActionListener {
            void onViewResponses(Survey survey);
        }

        private List<Survey> surveys = new ArrayList<>();
        private final OnRecentSurveyActionListener listener;

        public RecentSurveysAdapter(OnRecentSurveyActionListener listener) {
            this.listener = listener;
        }

        public void setSurveys(List<Survey> surveys) {
            this.surveys = surveys != null ? surveys : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dashboard_recent, parent, false);
            return new RecentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecentViewHolder holder, int position) {
            holder.bind(surveys.get(position));
        }

        @Override
        public int getItemCount() {
            return surveys.size();
        }

        class RecentViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle;
            private final TextView tvDesc;
            private final Chip chipView;

            public RecentViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvSurveyTitle);
                tvDesc = itemView.findViewById(R.id.tvSurveyDescription);
                chipView = itemView.findViewById(R.id.chipViewResponses);
            }

            public void bind(Survey survey) {
                tvTitle.setText(survey.getTitle());
                tvDesc.setText(survey.getDescription());
                chipView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onViewResponses(survey);
                    }
                });
            }
        }
    }
}
