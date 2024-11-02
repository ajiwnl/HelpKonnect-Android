package com.helpkonnect.mobileapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InitialSignInQnA {
    private ArrayList<HashMap<String, Object>> surveyQuestions;

    public InitialSignInQnA() {
        surveyQuestions = new ArrayList<>();

        // Adding the first question with a fact
        HashMap<String, Object> question1 = new HashMap<>();
        question1.put("question", new ArrayList<>(Arrays.asList("What is your age range?")));
        question1.put("choices", new ArrayList<>(Arrays.asList(
                "18–24",
                "25–34",
                "35–44",
                "45–54",
                "55–64",
                "65+",
                "Prefer not to say"
        )));
        question1.put("fact", "Did you know? The average lifespan varies significantly across different age groups.");
        surveyQuestions.add(question1);

        // Adding the second question with a fact
        HashMap<String, Object> question2 = new HashMap<>();
        question2.put("question", new ArrayList<>(Arrays.asList("Do you have a strong preference for in-person therapy, teletherapy, or a mix of both?")));
        question2.put("choices", new ArrayList<>(Arrays.asList(
                "In-person",
                "Teletherapy",
                "A mix of both",
                "No preference"
        )));
        question2.put("fact", "Did you know? Teletherapy can be as effective as in-person therapy for many individuals.");
        surveyQuestions.add(question2);

        // Adding the third question with a fact
        HashMap<String, Object> question3 = new HashMap<>();
        question3.put("question", new ArrayList<>(Arrays.asList("When it comes to a therapist, do you have a preference for a man, woman, non-binary therapist, or are you comfortable with any gender?")));
        question3.put("choices", new ArrayList<>(Arrays.asList(
                "Man",
                "Woman",
                "Non-binary",
                "No preference",
                "Other (Please specify):"
        )));
        question3.put("fact", "Did you know? Research shows that a strong therapist-client match can improve therapy outcomes.");
        surveyQuestions.add(question3);

        // Adding the fourth question with a fact
        HashMap<String, Object> question4 = new HashMap<>();
        question4.put("question", new ArrayList<>(Arrays.asList("How do you feel about facilities with holistic options like art therapy, meditation, and mindfulness?")));
        question4.put("choices", new ArrayList<>(Arrays.asList(
                "Very interested",
                "Somewhat interested",
                "Not interested",
                "Other (Please specify):"
        )));
        question4.put("fact", "Did you know? Holistic therapies can enhance traditional psychotherapy approaches.");
        surveyQuestions.add(question4);

        // Adding the fifth question with a fact
        HashMap<String, Object> question5 = new HashMap<>();
        question5.put("question", new ArrayList<>(Arrays.asList("Do you prefer a therapy environment that respects religious or spiritual beliefs, or a more secular setting?")));
        question5.put("choices", new ArrayList<>(Arrays.asList(
                "Religious or spiritual environment",
                "Secular environment",
                "No preference",
                "Other (Please specify):"
        )));
        question5.put("fact", "Did you know? Many people find comfort in therapy settings that align with their beliefs.");
        surveyQuestions.add(question5);

        // Adding the sixth question with a fact
        HashMap<String, Object> question6 = new HashMap<>();
        question6.put("question", new ArrayList<>(Arrays.asList("Are you interested in facilities that focus on specific aspects like identity, gender, sexual orientation, or other areas meaningful to you?")));
        question6.put("choices", new ArrayList<>(Arrays.asList(
                "Yes, I prefer specialized focus",
                "No, general focus is fine",
                "No preference",
                "Other (Please specify):"
        )));
        question6.put("fact", "Did you know? Specialized therapy can address unique experiences and challenges faced by individuals.");
        surveyQuestions.add(question6);

        // Adding the seventh question with a fact
        HashMap<String, Object> question7 = new HashMap<>();
        question7.put("question", new ArrayList<>(Arrays.asList("Do you feel more comfortable in a smaller, intimate group setting or a larger, diverse group for therapy sessions?")));
        question7.put("choices", new ArrayList<>(Arrays.asList(
                "Small, intimate groups",
                "Larger, diverse groups",
                "No preference",
                "Other (Please specify):"
        )));
        question7.put("fact", "Did you know? Group therapy can provide support and insights from peers.");
        surveyQuestions.add(question7);

        // Adding the eighth question with a fact
        HashMap<String, Object> question8 = new HashMap<>();
        question8.put("question", new ArrayList<>(Arrays.asList("How do you feel about discussing sensitive topics with others around your age versus with a mixed age group?")));
        question8.put("choices", new ArrayList<>(Arrays.asList(
                "Prefer peers around my age",
                "Prefer a mixed age group",
                "No strong preference",
                "Other (Please specify):"
        )));
        question8.put("fact", "Did you know? Talking about sensitive issues with peers can create a sense of shared experience.");
        surveyQuestions.add(question8);

        // Adding the ninth question with a fact
        HashMap<String, Object> question9 = new HashMap<>();
        question9.put("question", new ArrayList<>(Arrays.asList("What’s more important to you in a therapy setting: structure and routine, or flexibility and spontaneity?")));
        question9.put("choices", new ArrayList<>(Arrays.asList(
                "Highly structured",
                "Mostly structured, with some flexibility",
                "Prefer flexibility and spontaneity",
                "No preference"
        )));
        question9.put("fact", "Did you know? Different therapy approaches suit different individuals and their needs.");
        surveyQuestions.add(question9);

        // Adding the tenth question with a fact
        HashMap<String, Object> question10 = new HashMap<>();
        question10.put("question", new ArrayList<>(Arrays.asList("Is there a particular type of support you feel you need most: emotional, behavioral, or educational (e.g., learning coping skills)?")));
        question10.put("choices", new ArrayList<>(Arrays.asList(
                "Emotional support",
                "Behavioral support",
                "Educational support (e.g., coping skills)",
                "All of the above",
                "Other (Please specify):"
        )));
        question10.put("fact", "Did you know? Identifying your support needs can enhance the effectiveness of therapy.");
        surveyQuestions.add(question10);
    }

    public ArrayList<HashMap<String, Object>> getSurveyQuestions() {
        return surveyQuestions;
    }
}
