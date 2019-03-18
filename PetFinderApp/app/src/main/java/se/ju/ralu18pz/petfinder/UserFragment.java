package se.ju.ralu18pz.petfinder;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    private HomeFragment homeFragment;
    private EditUserFragment editUserFragment;
    private SeePostsFragment seePostsFragment;

    private Button logoutButton;
    private Button editInfo;
    private Button deleteAccount;
    private Button seeLostPosts;
    private Button seeFoundPosts;

    private TextView name;
    private TextView email;
    private TextView petsCount;
    private TextView lostPostsCount;
    private TextView foundPostsCount;

    private FirebaseFirestore db;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInputs();
        getCountOfPets();
        getCountOfLostPosts();
        getCountOfFoundPosts();

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(editUserFragment);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAction();
            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccountAction();
            }
        });

        seeLostPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragmentToPosts(seePostsFragment, 0);
            }
        });

        seeFoundPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragmentToPosts(seePostsFragment, 1);
            }
        });

    }

    private void deleteAccountAction() {
        deletePostsOfUser();
        deletePetsOfUser();
        db.collection(MainActivity.USER_CLASS).document(MainActivity.currentUser.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MainActivity.currentUser.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getActivity(), getString(R.string.user_delete_label), Toast.LENGTH_LONG).show();
                                        setFragment(homeFragment);
                                        MainActivity.mainNav.getMenu().getItem(0).setChecked(true);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deletePetsOfUser() {
        db.collection(MainActivity.PET_CLASS)
                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            db.collection(MainActivity.PET_CLASS)
                                    .document(document.getId())
                                    .delete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deletePostsOfUser() {
        deleteLostPosts();
        deleteFoundPosts();
        db.collection(MainActivity.LOST_COLLECTION)
                .document(MainActivity.currentUser.getUid())
                .delete();
    }

    private void deleteFoundPosts() {
        db.collection(MainActivity.FOUND_COLLECTION)
                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            db.collection(MainActivity.FOUND_COLLECTION)
                                    .document(documentSnapshot.getId())
                                    .delete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteLostPosts() {
        db.collection(MainActivity.LOST_COLLECTION)
                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            db.collection(MainActivity.LOST_COLLECTION)
                                    .document(documentSnapshot.getId())
                                    .delete();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logoutAction() {
        MainActivity.auth.signOut();
        Toast.makeText(getActivity(), getString(R.string.successful_sign_out), Toast.LENGTH_LONG).show();
        setFragment(homeFragment);
        MainActivity.mainNav.getMenu().getItem(0).setChecked(true);
    }

    private void getCountOfFoundPosts() {
        db.collection(MainActivity.FOUND_COLLECTION)
                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            foundPostsCount.setText("You have " + task.getResult().size() + " posts of pet found in this moment");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getCountOfLostPosts() {
        db.collection(MainActivity.LOST_COLLECTION)
                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            lostPostsCount.setText("There are " + task.getResult().size() + " of your pets lost in this moment");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getCountOfPets() {
        db.collection(MainActivity.USER_CLASS).document(MainActivity.currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User currentUser = documentSnapshot.toObject(User.class);
                        name.setText(currentUser.firstName + " " + currentUser.lastName);
                        email.setText("Email: " + currentUser.email);
                        db.collection(MainActivity.PET_CLASS)
                                .whereEqualTo("userId", MainActivity.currentUser.getUid())
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()) {
                                            petsCount.setText("Pets: " + String.valueOf(task.getResult().size()));
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        logoutButton = getView().findViewById(R.id.logout_button);
        editInfo = getView().findViewById(R.id.edit_info_button);
        deleteAccount = getView().findViewById(R.id.delete_account_button);
        name = getView().findViewById(R.id.welcome_user_profile);
        email = getView().findViewById(R.id.email_profile_text);
        petsCount = getView().findViewById(R.id.pets_profile_text);
        lostPostsCount = getView().findViewById(R.id.lost_posts_profile_text);
        foundPostsCount = getView().findViewById(R.id.found_posts_profile_text);
        seeLostPosts = getView().findViewById(R.id.see_lost_posts_button);
        seeFoundPosts = getView().findViewById(R.id.see_found_posts_button);

        homeFragment = new HomeFragment();
        editUserFragment = new EditUserFragment();
        seePostsFragment = new SeePostsFragment();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    private void setFragmentToPosts(Fragment fragment, int value) {
        Bundle bundle = new Bundle();
        bundle.putInt("type", value);
        fragment.setArguments(bundle);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
