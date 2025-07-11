package com.he161696.kingbarber.model;

public class Barbers {
    private int BarberId;
    private String FullName;
    private String Email;
    private String Password;
    private float AverageRating;
    private int RatingCount;
    private String Image_barber;

    public Barbers() {
    }

    public Barbers(int barberId, String image_barber, float averageRating, int ratingCount, String password, String email, String fullName) {
        BarberId = barberId;
        Image_barber = image_barber;
        AverageRating = averageRating;
        RatingCount = ratingCount;
        Password = password;
        Email = email;
        FullName = fullName;
    }

    public int getBarberId() {
        return BarberId;
    }

    public void setBarberId(int barberId) {
        BarberId = barberId;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public float getAverageRating() {
        return AverageRating;
    }

    public void setAverageRating(float averageRating) {
        AverageRating = averageRating;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public int getRatingCount() {
        return RatingCount;
    }

    public void setRatingCount(int ratingCount) {
        RatingCount = ratingCount;
    }

    public String getImage_barber() {
        return Image_barber;
    }

    public void setImage_barber(String image_barber) {
        Image_barber = image_barber;
    }
}
