package se.ju.ralu18pz.petfinder;

import java.util.ArrayList;

public class User {
    String firstName, lastName, email;
    /*
    ArrayList<Pet> pets;
    ArrayList<LostPost> lostposts;
    ArrayList<FoundPost> foundposts;
    */

    public User() {

    }

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        //pets = new ArrayList<Pet>();
        //lostposts = new ArrayList<LostPost>();
        //oundposts = new ArrayList<FoundPost>();
    }

    /*
    public User(String firstName, String lastName, String email, ArrayList<Pet> pets, ArrayList<LostPost> lostposts, ArrayList<FoundPost> foundposts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.pets = pets;
        this.lostposts = lostposts;
        this.foundposts = foundposts;
    }

    */

}
