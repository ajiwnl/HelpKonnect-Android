package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.content.Context;
import android.content.SharedPreferences;



public class PreferenceFragment extends Fragment {
    private ImageView backButton;
    private SwitchCompat prefSwitch1,prefSwitch2,prefSwitch3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_preference, container, false);

        backButton = rootView.findViewById(R.id.prefBackButton);
        prefSwitch1 = rootView.findViewById(R.id.journalSwitch);
        prefSwitch2 = rootView.findViewById(R.id.analysisSwitch);
        prefSwitch3 = rootView.findViewById(R.id.resSwitch);

        // SharedPreferences to save and load the switch states
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Load saved states
        prefSwitch1.setChecked(sharedPreferences.getBoolean("journal_notification", true));
        prefSwitch2.setChecked(sharedPreferences.getBoolean("analysis_notification", true));
        prefSwitch3.setChecked(sharedPreferences.getBoolean("resources_notification", true));

        prefSwitch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("journal_notification", isChecked).apply();
        });
        prefSwitch2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("analysis_notification", isChecked).apply();
        });
        prefSwitch3.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("resources_notification", isChecked).apply();
        });

        backButton.setOnClickListener(v -> {
            FragmentMethods.displayFragment(getActivity().getSupportFragmentManager(),R.id.FragmentContent,new UserSettingsFragment());
        });

        return rootView;
    }
}