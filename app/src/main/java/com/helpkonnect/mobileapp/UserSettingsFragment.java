package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class UserSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_settings, container, false);

        // Set up the ImageView for editing profile
        ImageView editProfile = rootView.findViewById(R.id.editUserProfile);

        // Handle edit profile click
        editProfile.setOnClickListener(v ->
                Toast.makeText(rootView.getContext(), "Cannot Edit Yet", Toast.LENGTH_SHORT).show()
        );

        return rootView;
    }
}
