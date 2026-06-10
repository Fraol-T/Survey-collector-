package com.example.surveycollector.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.surveycollector.R;
import com.example.surveycollector.model.entity.Question;
import com.example.surveycollector.model.entity.QuestionOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    public interface OnQuestionActionListener {
        void onDeleteQuestion(Question question);
    }

    private List<Question> questions = new ArrayList<>();
    private final Map<Integer, List<QuestionOption>> optionsMap = new HashMap<>();
    private final OnQuestionActionListener listener;

    public QuestionAdapter(OnQuestionActionListener listener) {
        this.listener = listener;
    }

    public void setData(List<Question> questions, List<QuestionOption> options) {
        this.questions = questions != null ? questions : new ArrayList<>();
        this.optionsMap.clear();
        if (options != null) {
            for (QuestionOption opt : options) {
                int qId = opt.getQuestionId();
                if (!optionsMap.containsKey(qId)) {
                    optionsMap.put(qId, new ArrayList<>());
                }
                optionsMap.get(qId).add(opt);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.bind(questions.get(position));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestionText;
        private final TextView tvTypeBadge;
        private final ImageButton btnDelete;
        private final LinearLayout layoutOptions;
        private final LinearLayout layoutOptionsList;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionText = itemView.findViewById(R.id.tvQuestionText);
            tvTypeBadge = itemView.findViewById(R.id.tvTypeBadge);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutOptions = itemView.findViewById(R.id.layoutOptions);
            layoutOptionsList = itemView.findViewById(R.id.layoutOptionsList);
        }

        void bind(Question question) {
            tvQuestionText.setText(question.getText());

            String type = question.getType();
            tvTypeBadge.setText(type.replace("_", " "));

            int badgeColor;
            int textColor;
            if ("TEXT".equalsIgnoreCase(type)) {
                badgeColor = 0xFFE2E8F0; // slate-200
                textColor = 0xFF475569;  // slate-600
            } else if ("SINGLE_CHOICE".equalsIgnoreCase(type)) {
                badgeColor = 0xFFDBEAFE; // blue-100
                textColor = 0xFF2563EB;  // blue-600
            } else { // MULTIPLE_CHOICE
                badgeColor = 0xFFCCFBF1; // teal-100
                textColor = 0xFF0D9488;  // teal-600
            }

            tvTypeBadge.setBackgroundTintList(ColorStateList.valueOf(badgeColor));
            tvTypeBadge.setTextColor(textColor);

            List<QuestionOption> opts = optionsMap.get(question.getId());
            if (opts != null && !opts.isEmpty() && !"TEXT".equalsIgnoreCase(type)) {
                layoutOptions.setVisibility(View.VISIBLE);
                layoutOptionsList.removeAllViews();
                for (int i = 0; i < opts.size(); i++) {
                    TextView tvOpt = new TextView(itemView.getContext());
                    tvOpt.setText((i + 1) + ". " + opts.get(i).getText());
                    tvOpt.setTextColor(0xFF334155); // slate-700
                    tvOpt.setTextSize(14);
                    tvOpt.setPadding(0, 4, 0, 4);
                    layoutOptionsList.addView(tvOpt);
                }
            } else {
                layoutOptions.setVisibility(View.GONE);
                layoutOptionsList.removeAllViews();
            }

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteQuestion(question);
                }
            });
        }
    }
}
