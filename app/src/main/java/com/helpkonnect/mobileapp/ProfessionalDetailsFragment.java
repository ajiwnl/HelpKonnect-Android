package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ProfessionalDetailsFragment extends Fragment {

    private ImageView professionalImage;
    private TextView professionalName;
    private TextView associatedFacility;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_professional_details, container, false);

        //Setting the Professional Data
        professionalImage = rootView.findViewById(R.id.ProfessionalImage);
        professionalName = rootView.findViewById(R.id.ProfessionalName);
        associatedFacility = rootView.findViewById(R.id.AssociatedFacility);
        if (getArguments() != null) {
            int imageResId = getArguments().getInt("image");
            String name = getArguments().getString("name");
            String facility = getArguments().getString("facility");
            professionalImage.setImageResource(imageResId);
            professionalName.setText(name);
            associatedFacility.setText("Associated with "+ facility);
        }

        return rootView;
    }
}
