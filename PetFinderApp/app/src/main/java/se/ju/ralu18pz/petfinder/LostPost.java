package se.ju.ralu18pz.petfinder;

public class LostPost { ;
    int year, month, day;
    double longitude, latitude;
    String circumstance, petId, userId;

    public LostPost() {

    }

    public LostPost(int year, int month, int day, double longitude, double latitude,
                    String circumstance, String petId, String userId) {

        this.year = year;
        this.month = month;
        this.day = day;
        this.longitude = longitude;
        this.latitude = latitude;
        this.circumstance = circumstance;
        this.petId = petId;
        this.userId = userId;
    }
}
