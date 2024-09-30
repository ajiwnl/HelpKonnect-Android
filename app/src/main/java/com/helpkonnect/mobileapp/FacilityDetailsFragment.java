package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class FacilityDetailsFragment extends Fragment {

    private TextSwitcher listTitleSwitcher;
    private String[] ListTitles = {"Professionals", "Comments"};
    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_facility_details, container, false);

        //For Switching from Comments to Professional, vice versa.
        listTitleSwitcher = rootView.findViewById(R.id.ListChanger);
        listTitleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView textView = new TextView(getContext());
                textView.setTextSize(10);
                textView.setTextColor(getResources().getColor(R.color.black));
                textView.setText(ListTitles[currentIndex]);
                return textView;
            }
        });
        listTitleSwitcher.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % ListTitles.length;
            listTitleSwitcher.setText(ListTitles[currentIndex]);
        });

        //For Facility Image Display
        Bundle args = getArguments();
        if (args != null) {
            int logo = args.getInt("logo");
            String name = args.getString("name");
            String location = args.getString("location");
            float rating = args.getFloat("rating");

            ImageView facilityImage = rootView.findViewById(R.id.facilityImage);
            TextView facilityName = rootView.findViewById(R.id.FacilityName);
            TextView facilityLocation = rootView.findViewById(R.id.FacilityLocation);
            RatingBar facilityRatingBar = rootView.findViewById(R.id.FacilityRating);

            facilityImage.setImageResource(logo);
            facilityName.setText(name);
            facilityLocation.setText(location);
            facilityRatingBar.setRating(rating);
        }


        return rootView;
    }
}