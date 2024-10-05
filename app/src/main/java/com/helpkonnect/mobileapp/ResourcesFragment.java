package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class ResourcesFragment extends Fragment {

    CardView musicPlayerContainer,selfcareContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_resources, container, false);


        musicPlayerContainer = rootView.findViewById(R.id.MusicPlayer);
        selfcareContainer = rootView.findViewById(R.id.SelfCare);

        musicPlayerContainer.setOnClickListener( v -> {
            Intent toMusicPlayer = new Intent(requireContext(), MusicPlayerActivity.class);
            startActivity(toMusicPlayer);
        });

        selfcareContainer.setOnClickListener( v -> {
            Intent toSelfCare = new Intent(requireContext(), SelfCareMaterialsActivity.class);
            startActivity(toSelfCare);
        });
        return rootView;
    }
}
