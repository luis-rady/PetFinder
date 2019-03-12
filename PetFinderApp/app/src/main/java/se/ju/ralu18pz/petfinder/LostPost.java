package se.ju.ralu18pz.petfinder;

public class LostPost {
    Pet petLost;
    User user;
    int year, month, day;
    double longitude, latitude;
    String circumstance;

    public LostPost() {

    }

    public LostPost(int year, int month, int day, double longitude, double latitude, String circumstance, Pet pet, User user) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.longitude = longitude;
        this.latitude = latitude;
        this.circumstance = circumstance;
        this.user = user;
        petLost = pet;
    }
}
