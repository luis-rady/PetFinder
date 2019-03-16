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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {

    private HomeFragment homeFragment;
    private EditUserFragment editUserFragment;
    private Button logoutButton;
    private Button editInfo;
    private Button deleteAccount;

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

        logoutButton = getView().findViewById(R.id.logout_button);
        editInfo = getView().findViewById(R.id.edit_info_button);
        deleteAccount = getView().findViewById(R.id.delete_account_button);

        name = getView().findViewById(R.id.welcome_user_profile);
        email = getView().findViewById(R.id.email_profile_text);
        petsCount = getView().findViewById(R.id.pets_profile_text);
        lostPostsCount = getView().findViewById(R.id.lost_posts_profile_text);
        foundPostsCount = getView().findViewById(R.id.found_posts_profile_text);

        homeFragment = new HomeFragment();
        editUserFragment = new EditUserFragment();

        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();


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
                        //lostPostsCount.setText("Lost posts made: " + currentUser.lostposts.size());
                        //foundPostsCount.setText("Found posts made: " + currentUser.foundposts.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                    }
                });



        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(editUserFragment);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.auth.signOut();
                Toast.makeText(getActivity(), getString(R.string.successful_sign_out), Toast.LENGTH_LONG).show();
                setFragment(homeFragment);
                MainActivity.mainNav.getMenu().getItem(0).setChecked(true);

            }
        });

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }
}
