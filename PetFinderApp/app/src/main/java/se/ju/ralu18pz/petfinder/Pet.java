package se.ju.ralu18pz.petfinder;

import java.util.ArrayList;

public class Pet {
    String id, name, type, sex, description, petImageURL, userId, neutered, collar, years, months;
    ArrayList<String> colors;
    boolean lost;

    public Pet() {

    }

    public Pet(String id, String name, String type, String sex, String description, String petImage, String userId, ArrayList<String> colors,
               String neutered, String collar, String years, String months) {

        this.id = id;
        this.name = name;
        this.type = type;
        this.sex = sex;
        this.description = description;
        this.petImageURL = petImage;
        this.userId = userId;
        this.colors = colors;
        this.neutered = neutered;
        this.collar = collar;
        this.years = years;
        this.months = months;
        lost = false;
    }
}
