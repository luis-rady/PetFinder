package se.ju.ralu18pz.petfinder;

import com.google.android.gms.maps.model.LatLng;

public class LostPost {
    double latitude, longitude;
    String circumstance, petId, userId, date, description, contactName, contactPhone, contactExtension;

    public LostPost() {

    }

    public LostPost(String date, String circumstance, String petId, String userId, String description,
                    double latitude, double longitude, String contactName, String contactPhone, String contactExtension) {
        this.date = date;
        this.circumstance = circumstance;
        this.petId = petId;
        this.userId = userId;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.contactExtension = contactExtension;
    }
}
