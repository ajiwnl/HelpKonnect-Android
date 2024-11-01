package com.helpkonnect.mobileapp;

import java.util.HashMap;
import java.util.Map;

public class EmotionSummary {
    private String day;
    private Map<String, Float> emotions; // Store emotions for that day

    public EmotionSummary(String day) {
        this.day = day;
        this.emotions = new HashMap<>();
    }

    public String getDay() {
        return day;
    }

    public Map<String, Float> getEmotions() {
        return emotions;
    }

    public void addEmotion(String emotion, Float value) {
        emotions.put(emotion, emotions.getOrDefault(emotion, 0f) + value);
    }
}
