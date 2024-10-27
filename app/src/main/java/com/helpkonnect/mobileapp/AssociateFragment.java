package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AssociateFragment extends Fragment {

    private CardView resources;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_associate, container, false);

        resources = rootView.findViewById(R.id.ResourcesCardView);

        resources.setOnClickListener( v -> {
            Intent intent = new Intent(getActivity(), ManageResourcesActivity.class);
            startActivity(intent);

        });



        return rootView;
    }
}