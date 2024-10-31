package com.helpkonnect.mobileapp;

import com.google.firebase.Timestamp;

import java.util.Date;

public class Resource {
    private boolean approved;
    private String description;
    private String facilityName;
    private String fileURL;
    private String imageURL;
    private String name;
    private String type;
    private Timestamp time;

    public Resource() {
    }

    public Resource(boolean approved, String description, String facilityName, String fileURL,
                    String imageURL, String name, String type, Long timeMillis) {
        this.approved = approved;
        this.description = description;
        this.facilityName = facilityName;
        this.fileURL = fileURL;
        this.imageURL = imageURL;
        this.name = name;
        this.type = type;
    }

    public boolean isApproved() { return approved; }
    public String getDescription() { return description; }
    public String getFacilityName() { return facilityName; }
    public String getFileURL() { return fileURL; }
    public String getImageURL() { return imageURL; }
    public String getName() { return name; }
    public String getType() { return type; }
    public Timestamp getTime() { return time; }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public void setTimeFromLong(Long timeMillis) {
        if (timeMillis != null) {
            this.time = new Timestamp(new Date(timeMillis));
        }
    }
}
