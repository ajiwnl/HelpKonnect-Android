package com.helpkonnect.mobileapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomepageFragment extends Fragment {
    private CardView getStarted;
    private TextView verticalTextTools, verticalTextJournal, verticalTextFacility, verticalTextCommunity;
    private CardView cardTools, cardJournal, cardFacility, cardCommunity;
    private TextView textDescriptionJournal, textDescriptionTools, textDescriptionFacility, textDescriptionCommunity;
    private Button exploreButtonJournal, exploreButtonTools, exploreButtonFacility, exploreButtonCommunity;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_homepage, container, false);

        getStarted = rootView.findViewById(R.id.GetStarted);
        getStarted.setOnClickListener(v -> {
            // Your logic for Get Started button
        });

        verticalTextTools = rootView.findViewById(R.id.verticalTextTools);
        verticalTextJournal = rootView.findViewById(R.id.verticalTextJournal);
        verticalTextFacility = rootView.findViewById(R.id.verticalTextFacility);
        verticalTextCommunity = rootView.findViewById(R.id.verticalTextCommunity);

        textDescriptionJournal = rootView.findViewById(R.id.textDescriptionJournal);
        textDescriptionTools = rootView.findViewById(R.id.textDescriptionTools);
        textDescriptionFacility = rootView.findViewById(R.id.textDescriptionFacility);
        textDescriptionCommunity = rootView.findViewById(R.id.textDescriptionCommunity);

        exploreButtonJournal = rootView.findViewById(R.id.exploreButtonJournal);
        exploreButtonTools = rootView.findViewById(R.id.exploreButtonTools);
        exploreButtonFacility = rootView.findViewById(R.id.exploreButtonFacility);
        exploreButtonCommunity = rootView.findViewById(R.id.exploreButtonCommunity);

        cardTools = rootView.findViewById(R.id.ToResources);
        cardJournal = rootView.findViewById(R.id.ToJournal);
        cardFacility = rootView.findViewById(R.id.ToFacility);
        cardCommunity = rootView.findViewById(R.id.ToCommunity);

        cardJournal.setOnClickListener(v -> toggleText(verticalTextJournal, cardJournal, textDescriptionJournal, exploreButtonJournal));
        cardTools.setOnClickListener(v -> toggleText(verticalTextTools,  cardTools, textDescriptionTools, exploreButtonTools));
        cardFacility.setOnClickListener(v -> toggleText(verticalTextFacility, cardFacility, textDescriptionFacility, exploreButtonFacility));
        cardCommunity.setOnClickListener(v -> toggleText(verticalTextCommunity, cardCommunity, textDescriptionCommunity, exploreButtonCommunity));

        return rootView;
    }


    private void toggleText(TextView textView,  CardView clickedCard, TextView description, Button exploreButton) {
        // Reset text for other TextViews
        resetText(textView);

        // Set the clicked TextView to display the full word
        textView.setText("");

        // Reset card widths
        setCardWidth(cardTools, 0);
        setCardWidth(cardJournal, 0);
        setCardWidth(cardFacility, 0);
        setCardWidth(cardCommunity, 0);

        // Expand the clicked card
        setCardWidth(clickedCard, 175); // Ensure you're converting dp to pixels

        // Update visibility for additional views
        updateVisibility(textView, description, exploreButton);
    }


    private void updateVisibility(TextView textView, TextView description, Button exploreButton) {

        resetLayoutConstraints(textView);

        textDescriptionJournal.setVisibility(View.GONE);
        exploreButtonJournal.setVisibility(View.GONE);

        textDescriptionTools.setVisibility(View.GONE);
        exploreButtonTools.setVisibility(View.GONE);

        textDescriptionFacility.setVisibility(View.GONE);
        exploreButtonFacility.setVisibility(View.GONE);

        textDescriptionCommunity.setVisibility(View.GONE);
        exploreButtonCommunity.setVisibility(View.GONE);

        if (textView.getId() == R.id.verticalTextJournal) {
            description.setVisibility(View.VISIBLE);
            exploreButton.setVisibility(View.VISIBLE);
            removeLayoutConstraints(textView);
        } else if (textView.getId() == R.id.verticalTextTools) {
            description.setVisibility(View.VISIBLE);
            exploreButton.setVisibility(View.VISIBLE);
            removeLayoutConstraints(textView);
        } else if (textView.getId() == R.id.verticalTextFacility) {
            description.setVisibility(View.VISIBLE);
            exploreButton.setVisibility(View.VISIBLE);
            removeLayoutConstraints(textView);
        } else if (textView.getId() == R.id.verticalTextCommunity) {
            description.setVisibility(View.VISIBLE);
            exploreButton.setVisibility(View.VISIBLE);
            removeLayoutConstraints(textView);
        }
    }


    private void resetLayoutConstraints(TextView textView) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        textView.setLayoutParams(params);
    }

    private void resetText(TextView clickedTextView) {
        if (clickedTextView != verticalTextTools) {
            verticalTextTools.setText("T\nO\nO\nL\nS");
        }
        if (clickedTextView != verticalTextJournal) {
            verticalTextJournal.setText("J\nO\nU\nR\nN\nA\nL");
        }
        if (clickedTextView != verticalTextFacility) {
            verticalTextFacility.setText("F\nA\nC\nI\nL\nI\nT\nY");
        }
        if (clickedTextView != verticalTextCommunity) {
            verticalTextCommunity.setText("C\nO\nM\nM\nU\nN\nI\nT\nY");
        }
    }

    private void setCardWidth(CardView cardView, int width) {

        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.width = width;
        cardView.setLayoutParams(params);
    }

    private void removeLayoutConstraints(TextView textView) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textView.getLayoutParams();
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        textView.setLayoutParams(params);
    }
}
