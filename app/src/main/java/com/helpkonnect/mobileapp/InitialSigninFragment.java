package com.helpkonnect.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;

import java.util.ArrayList;
import java.util.HashMap;

public class InitialSigninFragment extends Fragment {

    private TextView userQuestion;
    private RadioGroup answerOptionsGroup;
    private EditText customAnswer;
    private TextView nextOrFinish, previous, dyk;

    private InitialSignInQnA surveyData;
    private int currentQuestionIndex = 0;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ArrayList<HashMap<String, String>> userResponses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_initial_signin, container, false);

        userQuestion = rootView.findViewById(R.id.UserQuestion);
        answerOptionsGroup = rootView.findViewById(R.id.answerOptionsGroup);
        customAnswer = rootView.findViewById(R.id.customAnswer);
        nextOrFinish = rootView.findViewById(R.id.NextOrFinish);
        previous = rootView.findViewById(R.id.Previous);
        dyk = rootView.findViewById(R.id.DYKTextView);

        surveyData = new InitialSignInQnA();
        userResponses = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        displayCurrentQuestion();

        nextOrFinish.setOnClickListener(v -> {
            if (currentQuestionIndex < surveyData.getSurveyQuestions().size() - 1) {
                storeCurrentAnswer();
                currentQuestionIndex++;
                displayCurrentQuestion();
            } else {
                storeCurrentAnswer();
                submitAnswers();
            }
        });

        previous.setOnClickListener(v -> {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--;
                displayCurrentQuestion();
            }

        });

        return rootView;
    }

    private void displayCurrentQuestion() {
        HashMap<String, Object> currentQuestion = surveyData.getSurveyQuestions().get(currentQuestionIndex);
        userQuestion.setText((currentQuestionIndex + 1) + ". " + ((ArrayList<String>) currentQuestion.get("question")).get(0));

        answerOptionsGroup.removeAllViews();
        ArrayList<String> choices = (ArrayList<String>) currentQuestion.get("choices");

        // Load saved answers if available
        HashMap<String, String> savedAnswer = userResponses.size() > currentQuestionIndex ? userResponses.get(currentQuestionIndex) : null;

        for (String choice : choices) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setText(choice);
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && choice.equals("Other (Please specify):")) {
                    customAnswer.setVisibility(View.VISIBLE);
                } else if (isChecked) {
                    customAnswer.setVisibility(View.GONE);
                }
            });
            answerOptionsGroup.addView(radioButton);

            // Check the saved answer if exists
            if (savedAnswer != null && savedAnswer.get("answer").equals(choice)) {
                radioButton.setChecked(true);
            }
        }

        // Handle custom answer visibility
        if (savedAnswer != null && "Other (Please specify):".equals(savedAnswer.get("answer"))) {
            customAnswer.setVisibility(View.VISIBLE);
            customAnswer.setText(savedAnswer.get("customAnswer")); // Display the saved custom answer
        } else {
            customAnswer.setVisibility(View.GONE);
        }

        String dykFact = (String) currentQuestion.get("fact");
        dyk.setText(dykFact);

        if (currentQuestionIndex == surveyData.getSurveyQuestions().size() - 1) {
            nextOrFinish.setText("Submit");
        } else {
            nextOrFinish.setText("Next");
        }

        if (currentQuestionIndex == 0) {
            previous.setVisibility(View.GONE);
        } else {
            previous.setVisibility(View.VISIBLE);
        }
    }

    private void storeCurrentAnswer() {
        // Ensure there is an entry for the current question
        if (userResponses.size() <= currentQuestionIndex) {
            userResponses.add(new HashMap<>()); // Add a new response if none exists
        }

        HashMap<String, String> currentAnswer = userResponses.get(currentQuestionIndex);
        HashMap<String, Object> currentQuestion = surveyData.getSurveyQuestions().get(currentQuestionIndex);

        currentAnswer.put("question", ((ArrayList<String>) currentQuestion.get("question")).get(0));

        int selectedId = answerOptionsGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadioButton = answerOptionsGroup.findViewById(selectedId);
            if (selectedRadioButton != null) { // Check if selectedRadioButton is not null
                currentAnswer.put("answer", selectedRadioButton.getText().toString());
            }
        } else {
            currentAnswer.put("answer", "No selection");
        }

        // Store custom answer if visible
        if (customAnswer.getVisibility() == View.VISIBLE) {
            String customText = customAnswer.getText().toString().trim();
            currentAnswer.put("customAnswer", customText); // Store the custom answer separately
        } else {
            currentAnswer.put("customAnswer", ""); // Clear custom answer if not visible
        }
    }

    private void submitAnswers() {
        boolean hasUnansweredQuestions = false;

        for (HashMap<String, String> response : userResponses) {
            String answer = response.get("answer");
            if (answer == null || answer.equals("No selection")) {
                hasUnansweredQuestions = true;
                break;
            }
        }

        if (hasUnansweredQuestions) {
            dyk.setText("You left some questions unanswered.");
            dyk.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fab_shake_up));
        } else {
            String userId = mAuth.getCurrentUser().getUid();

            // Reference to the "answers" collection with each user's answers in a subcollection named "userAnswers" under their userId
            DocumentReference userDocRef = db.collection("answers").document(userId);
            CollectionReference userAnswersRef = userDocRef.collection("userAnswers");

            for (HashMap<String, String> response : userResponses) {
                String question = response.get("question");
                String answer = response.get("answer");
                String customAnswer = response.get("customAnswer");

                HashMap<String, Object> answerData = new HashMap<>();
                answerData.put("question", question);
                answerData.put("answer", answer);
                answerData.put("customAnswer", customAnswer);

                userAnswersRef.add(answerData)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("InitialSigninFragment", "Answer saved successfully for question: " + question);
                        })
                        .addOnFailureListener(e -> {
                            Log.d("InitialSigninFragment", "Failed to save answer for question: " + question + ", error: " + e.getMessage());
                        });
            }

            // After saving answers, update 'firstTimeLogin' field in the 'credentials' collection
            db.collection("credentials").document(userId)
                    .update("firstTimeLogin", false)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("InitialSigninFragment", "First-time login updated.");

                        // Navigate to MainScreenActivity
                        Intent toMainScreen = new Intent(getActivity(), MainScreenActivity.class);
                        startActivity(toMainScreen);
                        getActivity().finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.d("InitialSigninFragment", "Failed to update firstTimeLogin: " + e.getMessage());
                    });
        }
    }
}
