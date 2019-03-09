package se.ju.ralu18pz.petfinder;

import java.util.ArrayList;

public class User {
    String firstName, lastName, email, description, profileImage;
    ArrayList<Pet> pets;
    ArrayList<LostPost> lostposts;
    ArrayList<FoundPost> foundposts;


    public User() {

    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        description = "";
        profileImage = "https://d2x5ku95bkycr3.cloudfront.net/App_Themes/Common/images/profile/0_200.png";
        pets = new ArrayList<Pet>();
        lostposts = new ArrayList<LostPost>();
        foundposts = new ArrayList<FoundPost>();
    }

}
