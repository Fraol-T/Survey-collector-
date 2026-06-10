package com.example.surveycollector.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.surveycollector.R;
import com.example.surveycollector.model.AppDatabase;
import com.example.surveycollector.model.entity.SurveyLink;
import com.example.surveycollector.util.LinkUtil;
import com.example.surveycollector.viewmodel.SurveyLinkViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.snackbar.Snackbar;

public class SurveyLinkFragment extends Fragment {

    private int surveyId = -1;
    private SurveyLinkViewModel viewModel;
    private SurveyLink currentLinkObj;

    private TextView tvSurveyTitle;
    private TextView tvUrlText;
    private TextView tvActiveStatus;
    private MaterialButton btnCopy;
    private MaterialButton btnShare;
    private SwitchMaterial switchActive;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_survey_link, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            surveyId = getArguments().getInt("surveyId", -1);
        }

        if (surveyId == -1) {
            Toast.makeText(requireContext(), "Invalid Survey ID", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
            return;
        }

        tvSurveyTitle = view.findViewById(R.id.tvSurveyTitle);
        tvUrlText = view.findViewById(R.id.tvUrlText);
        tvActiveStatus = view.findViewById(R.id.tvActiveStatus);
        btnCopy = view.findViewById(R.id.btnCopyUrl);
        btnShare = view.findViewById(R.id.btnShareUrl);
        switchActive = view.findViewById(R.id.switchActive);
        toolbar = view.findViewById(R.id.toolbar);

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        // Fetch survey title on a background thread using application context
        // to avoid IllegalStateException if the fragment is detached
        final Context appContext = requireContext().getApplicationContext();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            com.example.surveycollector.model.entity.Survey survey = AppDatabase.getInstance(appContext).surveyDao().getSurveyById(surveyId);
            if (survey != null && isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (isAdded()) {
                        tvSurveyTitle.setText(survey.getTitle());
                    }
                });
            }
        });

        viewModel = new ViewModelProvider(this, new SurveyLinkViewModel.Factory(requireActivity().getApplication(), surveyId))
                .get(SurveyLinkViewModel.class);

        // Generate link if doesn't exist
        viewModel.generateLink(surveyId);

        viewModel.getCurrentLink().observe(getViewLifecycleOwner(), link -> {
            if (link != null) {
                currentLinkObj = link;
                String url = LinkUtil.buildUrl(link.getToken());
                tvUrlText.setText(url);

                boolean isActive = (link.getIsActive() == 1);
                switchActive.setChecked(isActive);
                if (isActive) {
                    tvActiveStatus.setText("Link is active and collecting responses");
                } else {
                    tvActiveStatus.setText("Link is disabled (respondents cannot open)");
                }
            }
        });

        btnCopy.setOnClickListener(v -> {
            if (currentLinkObj != null && isAdded()) {
                String url = LinkUtil.buildUrl(currentLinkObj.getToken());
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Survey URL", url);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view, "Link copied!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        btnShare.setOnClickListener(v -> {
            if (currentLinkObj != null && isAdded()) {
                String url = LinkUtil.buildUrl(currentLinkObj.getToken());
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, "Share Survey Link");
                startActivity(shareIntent);
            }
        });

        // Set click listener on switch to toggle link active state
        switchActive.setOnClickListener(v -> {
            if (currentLinkObj != null) {
                viewModel.toggleActive(currentLinkObj);
            }
        });
    }
}
