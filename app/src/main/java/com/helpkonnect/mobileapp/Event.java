package com.helpkonnect.mobileapp;

import java.io.Serializable;

public class Event implements Serializable {
    private String name;
    private String description;
    private String date;
    private String timeStart;
    private String timeEnd;
    private String venue;
    private String accommodation;
    private String facilityName;
    private String imageUrl;

    public Event(String name, String description, String date, String timeStart, String timeEnd, String venue, String accommodation, String facilityName, String imageUrl) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.venue = venue;
        this.accommodation = accommodation;
        this.facilityName = facilityName;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTimeStart() { return timeStart; }
    public String getTimeEnd() { return timeEnd; }
    public String getVenue() { return venue; }
    public String getAccommodation() { return accommodation; }
    public String getFacilityName() { return facilityName; }
    public String getImageUrl() { return imageUrl; }
}