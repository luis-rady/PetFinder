package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class EditUserFragment extends Fragment {

    private EditText emailInput;
    private EditText nameInput;
    private EditText lastNameInput;
    private EditText passwordInput;

    private Button editUser;
    private Button cancel;
    private ProgressBar progressBar;

    private FirebaseFirestore db;

    private boolean validForm;
    private String name;
    private String lastnames;
    private String email;
    private String password;

    private UserFragment userFragment;

    public EditUserFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();

        nameInput = getView().findViewById(R.id.update_name_input);
        lastNameInput = getView().findViewById(R.id.update_lastnames_input);
        emailInput = getView().findViewById(R.id.email_input_edit);
        passwordInput = getView().findViewById(R.id.password_input_edit);
        editUser = getView().findViewById(R.id.update_user_button);
        cancel = getView().findViewById(R.id.cancel_update_button);
        progressBar = getView().findViewById(R.id.update_user_progress_bar);

        emailInput.setFocusable(false);

        db.collection(MainActivity.USER_CLASS).document(MainActivity.currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User userData = documentSnapshot.toObject(User.class);
                        nameInput.setText(userData.firstName);
                        lastNameInput.setText(userData.lastName);
                        emailInput.setText(userData.email);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                    }
                });

        userFragment = new UserFragment();
        progressBar.setVisibility(View.GONE);

        editUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameInput.getText().toString().trim();
                lastnames = lastNameInput.getText().toString().trim();
                password = passwordInput.getText().toString().trim();
                email = emailInput.getText().toString().trim();

                validForm = formValidation(name, lastnames, password);

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    db.collection(MainActivity.USER_CLASS).document(MainActivity.currentUser.getUid())
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    User currentUser = documentSnapshot.toObject(User.class);
                                    currentUser.firstName = name;
                                    currentUser.lastName = lastnames;
                                    currentUser.email = email;

                                    db.collection(MainActivity.USER_CLASS).document(MainActivity.currentUser.getUid())
                                            .set(currentUser)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    MainActivity.currentUser.updatePassword(password)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(getActivity(), getString(R.string.user_updated_label), Toast.LENGTH_LONG).show();
                                                                    setFragment(userFragment);
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
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(userFragment);
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    private boolean formValidation(String name, String lastnames, String password) {
        if(!isValidName(name)) {
            nameInput.setError("Name is not valid");
            nameInput.requestFocus();
        }

        if(!isValidLastname(lastnames)) {
            lastNameInput.setError("Lastnames are not valid");
            lastNameInput.requestFocus();
        }

        if(!isValidPassword(password)) {
            passwordInput.setError("Password is not valid, it should have at least 6 characters");
            passwordInput.requestFocus();
        }

        if(!isValidName(name) || !isValidLastname(lastnames) || !isValidPassword(password)) {
            return false;
        }

        return true;
    }

    private boolean isValidName(String name) {
        if(name.length() < 3) {
            return false;
        }

        return true;
    }

    private boolean isValidLastname(String lastName) {
        if(lastName.length() < 2) {
            return false;
        }

        return true;
    }

    private boolean isValidPassword(String password) {
        if(password.length() < 6) {
            return false;
        }

        return true;
    }
}
