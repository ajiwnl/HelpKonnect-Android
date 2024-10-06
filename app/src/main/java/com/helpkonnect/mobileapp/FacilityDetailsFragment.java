package com.helpkonnect.mobileapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

public class FacilityDetailsFragment extends Fragment {

    private ImageButton backButton;
    private TextSwitcher listTitleSwitcher;
    private String[] ListTitles = {"Professionals", "Comments"};
    private String currentFacility;
    private int currentIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_facility_details, container, false);

        //For Switching from Comments to Professional, vice versa.
        listTitleSwitcher = rootView.findViewById(R.id.ListChanger);
        backButton = rootView.findViewById(R.id.FacilityBackButton);

        backButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });


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

        //For Facility Details Display
        Bundle args = getArguments();
        if (args != null) {
            int logo = args.getInt("logo");
            String name = args.getString("name");
            String location = args.getString("location");
            float rating = args.getFloat("rating");
            currentFacility = name;

            ImageView facilityImage = rootView.findViewById(R.id.facilityImage);
            TextView facilityName = rootView.findViewById(R.id.FacilityName);
            TextView facilityLocation = rootView.findViewById(R.id.FacilityLocation);
            RatingBar facilityRatingBar = rootView.findViewById(R.id.FacilityRating);
            //Set Data
            facilityImage.setImageResource(logo);
            facilityName.setText(name);
            facilityLocation.setText(location);
            facilityRatingBar.setRating(rating);
        }

        //For Professionals List
        //Sample Data
        List<ProfessionalAdapter.Professional> professionals = new ArrayList<>();
        professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "Jane Doe", "Counselor", "Helping you find your way."));
        professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "John Smith", "Therapist", "Supporting mental wellness."));
        professionals.add(new ProfessionalAdapter.Professional(R.drawable.userprofileicon, "Emily White", "Life Coach", "Empowering you to reach your goals."));

        RecyclerView recyclerView = rootView.findViewById(R.id.FacilityListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        ProfessionalAdapter adapter = new ProfessionalAdapter(professionals, professional -> {

            ProfessionalDetailsFragment detailsFragment = new ProfessionalDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("image", professional.getImage());
            bundle.putString("name", professional.getName());
            bundle.putString("profession", professional.getProfession());
            bundle.putString("facility", currentFacility);
            detailsFragment.setArguments(bundle);
            //Change to Details Fragment
            FragmentMethods.displayFragment(requireActivity().getSupportFragmentManager(), R.id.fragmentContent, detailsFragment);
        });

        recyclerView.setAdapter(adapter);

        //For Facility Comment List


        //For Create Comment

        return rootView;
    }

    private void switchList(RecyclerView recyclerView, RecyclerView.Adapter adapter){

    }


}