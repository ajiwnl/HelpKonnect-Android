package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class AssociateFragment extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ConstraintLayout associatedLayout;
    private TextView noAssociationMessage;
    private TextView facilityName, facilityDescription;

    private ImageView facilityImage;

    private CardView resources, events;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_associate, container, false);

        associatedLayout = rootView.findViewById(R.id.associatedLayout);
        noAssociationMessage = rootView.findViewById(R.id.noAssociationMessage);
        facilityName = rootView.findViewById(R.id.FacilityName);
        facilityDescription = rootView.findViewById(R.id.FacilityDescription);
        facilityImage = rootView.findViewById(R.id.facilityImage);

        resources = rootView.findViewById(R.id.ResourcesCardView);
        events = rootView.findViewById(R.id.EventsCardView);

        resources.setOnClickListener( v -> {
            Intent intent = new Intent(getActivity(), ManageResourcesActivity.class);
            intent.putExtra("facilityName", facilityName.getText());
            startActivity(intent);
        });
        
        events.setOnClickListener(v -> {
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentMethods.displayFragment(fragmentManager, R.id.FragmentContent, new CommunityFragment());
        });

        checkUserAssociation();

        return rootView;
    }

    private void checkUserAssociation() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("credentials").document(currentUserId)
                .get()
                .addOnSuccessListener(userDocument -> {
                    if (userDocument.exists()) {
                        String associatedFacilityId = userDocument.getString("associated");

                        if (associatedFacilityId != null && !associatedFacilityId.isEmpty()) {
                            fetchFacilityDetails(associatedFacilityId);
                        } else {
                            associatedLayout.setVisibility(View.GONE);
                            noAssociationMessage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("AssociateFragment", "User document does not exist");
                        associatedLayout.setVisibility(View.GONE);
                        noAssociationMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AssociateFragment", "Error fetching user association", e);
                    associatedLayout.setVisibility(View.GONE);
                    noAssociationMessage.setVisibility(View.VISIBLE);
                });
    }

    private void fetchFacilityDetails(String facilityId) {
        db.collection("credentials").document(facilityId)
                .get()
                .addOnSuccessListener(facilityDocument -> {
                    if (facilityDocument.exists()) {
                        facilityName.setText(facilityDocument.getString("facilityName"));
                        facilityDescription.setText(facilityDocument.getString("facilityDescription"));

                        String imageUrl = facilityDocument.getString("imageUrl");
                        if (imageUrl != null) {
                            Glide.with(this).load(imageUrl).into(facilityImage);
                        }

                        associatedLayout.setVisibility(View.VISIBLE);
                        noAssociationMessage.setVisibility(View.GONE);
                    } else {
                        Log.e("AssociateFragment", "Facility document does not exist");
                        associatedLayout.setVisibility(View.GONE);
                        noAssociationMessage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AssociateFragment", "Error fetching facility details", e);
                    associatedLayout.setVisibility(View.GONE);
                    noAssociationMessage.setVisibility(View.VISIBLE);
                });
    }
}