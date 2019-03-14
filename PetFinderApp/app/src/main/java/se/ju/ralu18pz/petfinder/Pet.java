package se.ju.ralu18pz.petfinder;

public class Pet {
    String IMAGE = "https://i.etsystatic.com/6914616/d/il/d55a2a/1564835627/il_340x270.1564835627_ky19.jpg?version=0";
    String name, type, sex, description, petImage, userId, neutered, collar;
    String[] colors;
    int years, months;

    public Pet() {

    }

    public Pet(String name, String type, String sex, String description, String petImage, String userId, String[] colors,
               String neutered, String collar, int years, int months) {

        this.name = name;
        this.type = type;
        this.sex = sex;
        this.description = description;
        this.petImage = petImage;
        this.userId = userId;
        this.colors = colors;
        this.neutered = neutered;
        this.collar = collar;
        this.years = years;
        this.months = months;
    }
}
