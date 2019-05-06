package se.ju.ralu18pz.petfinder;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class FoundPost {

    String postImage, petType, petSex, petNeutered, petCollar, petStatus,
            petSituation, contactName, contactPhone, contactExtension, userId, date, description;
    ArrayList<String> petColors;
    double latitude, longitude;


    public FoundPost() {

    }

    public FoundPost(String postImage, String petType, String description, String petSex, String petNeutered,
                     String petCollar, String petStatus, String petSituation, String contactName,
                     String contactPhone, String contactExtension, ArrayList<String> petColors,
                     String date, double latitude, double longitude, String userId) {

        this.postImage = postImage;
        this.petType = petType;
        this.petSex = petSex;
        this.description = description;
        this.petNeutered = petNeutered;
        this.petCollar = petCollar;
        this.petStatus = petStatus;
        this.petSituation = petSituation;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactExtension = contactExtension;
        this.petColors = petColors;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }
}
