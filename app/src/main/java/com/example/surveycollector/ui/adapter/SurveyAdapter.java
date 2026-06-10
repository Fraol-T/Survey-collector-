package com.example.surveycollector.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.entity.Survey;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;

public class SurveyAdapter extends RecyclerView.Adapter<SurveyAdapter.SurveyViewHolder> {

    public interface OnSurveyActionListener {
        void onEdit(Survey survey);
        void onDelete(Survey survey);
        void onManageQuestions(Survey survey);
        void onGetLink(Survey survey);
        void onViewResponses(Survey survey);
    }

    private List<Survey> surveys = new ArrayList<>();
    private final OnSurveyActionListener listener;

    public SurveyAdapter(OnSurveyActionListener listener) {
        this.listener = listener;
    }

    public void setSurveys(List<Survey> surveys) {
        this.surveys = surveys;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey, parent, false);
        return new SurveyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SurveyViewHolder holder, int position) {
        Survey survey = surveys.get(position);
        holder.bind(survey);
    }

    @Override
    public int getItemCount() {
        return surveys.size();
    }

    class SurveyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final Chip chipRegistration;
        private final Chip chipStatus;
        private final ImageButton btnOverflow;

        SurveyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSurveyTitle);
            tvDescription = itemView.findViewById(R.id.tvSurveyDescription);
            chipRegistration = itemView.findViewById(R.id.chipRegistration);
            chipStatus = itemView.findViewById(R.id.chipStatus);
            btnOverflow = itemView.findViewById(R.id.btnOverflow);
        }

        void bind(Survey survey) {
            tvTitle.setText(survey.getTitle());

            String desc = survey.getDescription();
            if (desc != null && !desc.isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(desc);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Registration chip
            if (survey.getRequiresRegistration() == 1) {
                chipRegistration.setText("Registration Required");
            } else {
                chipRegistration.setText("Anonymous");
            }

            // Status chip
            if (survey.getIsActive() == 1) {
                chipStatus.setText("Active");
            } else {
                chipStatus.setText("Inactive");
            }

            // Overflow menu
            btnOverflow.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.inflate(R.menu.menu_survey_item);
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.menu_edit) {
                        listener.onEdit(survey);
                        return true;
                    } else if (id == R.id.menu_manage_questions) {
                        listener.onManageQuestions(survey);
                        return true;
                    } else if (id == R.id.menu_get_link) {
                        listener.onGetLink(survey);
                        return true;
                    } else if (id == R.id.menu_view_responses) {
                        listener.onViewResponses(survey);
                        return true;
                    } else if (id == R.id.menu_delete) {
                        listener.onDelete(survey);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}
