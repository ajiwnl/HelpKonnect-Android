package com.helpkonnect.mobileapp;

public class Event {
    private String name;
    private String description;
    private String date;
    private String timeStart;
    private String timeEnd;
    private String venue;
    private String facilityName;
    private String imageUrl;
    private int accommodationCount;
    private boolean done;

    public Event(String name, String description, String date, String timeStart, String timeEnd, String venue, String facilityName, String imageUrl, int accommodationCount, boolean done) {
        this.name = name;
        this.description = description;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.venue = venue;
        this.facilityName = facilityName;
        this.imageUrl = imageUrl;
        this.accommodationCount = accommodationCount;
        this.done = done;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTimeStart() { return timeStart; }
    public String getTimeEnd() { return timeEnd; }
    public String getVenue() { return venue; }
    public String getFacilityName() { return facilityName; }
    public String getImageUrl() { return imageUrl; }
    public int getAccommodationCount() { return accommodationCount; }
    public boolean isDone() { return done; }
}