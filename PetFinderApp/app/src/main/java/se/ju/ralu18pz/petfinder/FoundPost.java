package se.ju.ralu18pz.petfinder;

public class FoundPost {

    String postImage, petType, petSex, petNeutered, petCollar, petStatus, petSituation, contactName, contactPhone, contactExtension;
    String[] petColors;
    int yearFound, monthFound, dayFound;
    double longitude, latitude;
    User user;


    public FoundPost() {

    }

    public FoundPost(String postImage, String petType, String petSex, String petNeutered,
                     String petCollar, String petStatus, String petSituation, String contactName,
                     String contactPhone, String contactExtension, String[] petColors,
                     int yearFound, int monthFound, int dayFound, double longitude, double latitude, User user) {

        this.postImage = postImage;
        this.petType = petType;
        this.petSex = petSex;
        this.petNeutered = petNeutered;
        this.petCollar = petCollar;
        this.petStatus = petStatus;
        this.petSituation = petSituation;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactExtension = contactExtension;
        this.petColors = petColors;
        this.yearFound = yearFound;
        this.monthFound = monthFound;
        this.dayFound = dayFound;
        this.longitude = longitude;
        this.latitude = latitude;
        this.user = user;
    }
}
