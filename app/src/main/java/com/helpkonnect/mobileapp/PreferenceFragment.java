package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class PreferenceFragment extends Fragment {
    private ImageView backButton;
    private SwitchCompat prefSwitch1,prefSwitch2,prefSwitch3;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_preference, container, false);
        backButton = rootView.findViewById(R.id.prefBackButton);
        prefSwitch1 = rootView.findViewById(R.id.prefTitleSwitch1);
        prefSwitch2 = rootView.findViewById(R.id.prefTitleSwitch2);
        prefSwitch3 = rootView.findViewById(R.id.prefTitleSwitch3);

        prefSwitch1.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });
        prefSwitch2.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });
        prefSwitch3.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        backButton.setOnClickListener(v->{
            FragmentMethods.displayFragment(getActivity().getSupportFragmentManager(),R.id.FragmentContent,new UserSettingsFragment());
        });

        return rootView;
    }
}