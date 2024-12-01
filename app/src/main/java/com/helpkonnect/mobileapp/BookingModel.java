package com.helpkonnect.mobileapp;

public class BookingModel {
    private String bookingTitle;
    private String bookingDate;
    private String bookingDetails;
    private String bookingStartTime;
    private String bookingDuration;
    private String bookingPrice;

    public BookingModel(){
        // Required for Firestore deserialization
    }

    public BookingModel(String bookingTitle, String bookingDate, String bookingDetails,
                        String bookingStartTime, String bookingDuration, String bookingPrice) {
        this.bookingTitle = bookingTitle;
        this.bookingDate = bookingDate;
        this.bookingDetails = bookingDetails;
        this.bookingStartTime = bookingStartTime;
        this.bookingDuration = bookingDuration;
        this.bookingPrice = bookingPrice;
    }

    public String getBookingTitle() {
        return bookingTitle;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingDetails() {
        return bookingDetails;
    }

    public String getBookingStartTime() {
        return bookingStartTime;
    }

    public String getBookingDuration() {
        return bookingDuration;
    }

    public String getBookingPrice() {
        return bookingPrice;
    }

    public void setBookingPrice(String bookingPrice) {
        this.bookingPrice = bookingPrice;
    }

    public void setBookingTitle(String bookingTitle) {
        this.bookingTitle = bookingTitle;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setBookingStartTime(String bookingStartTime) {
        this.bookingStartTime = bookingStartTime;
    }

    public void setBookingDuration(String bookingDuration) {
        this.bookingDuration = bookingDuration;
    }

    public void setBookingDetails(String ProfessionalName) {
        this.bookingDetails = ProfessionalName;
    }
}


