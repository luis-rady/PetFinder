package se.ju.ralu18pz.petfinder;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpFragment extends Fragment {

    private ProgressBar progressBar;
    private Button registerButton;
    private HomeAfterLoginFragment homeAfterLoginFragment;

    // Form fields

    private EditText nameEditText;
    private EditText lastnamesEditText;
    private EditText emailEditText;
    private EditText passwordEditText;

    // Value of fields

    private String name;
    private String lastnames;
    private String email;
    private String password;

    private boolean validForm;
    private FirebaseFirestore db;

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        homeAfterLoginFragment = new HomeAfterLoginFragment();
        progressBar = getView().findViewById(R.id.sign_up_progressbar);
        registerButton = getView().findViewById(R.id.register_button);
        nameEditText = getView().findViewById(R.id.name_input);
        lastnamesEditText = getView().findViewById(R.id.lastnames_input);
        emailEditText = getView().findViewById(R.id.email_input_sign_up);
        passwordEditText = getView().findViewById(R.id.password_input_sign_up);

        db = FirebaseFirestore.getInstance();
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();

        progressBar.setVisibility(View.GONE);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = nameEditText.getText().toString().trim();
                lastnames = lastnamesEditText.getText().toString().trim();
                email = emailEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                validForm = formValidation(name, lastnames, email, password);

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    MainActivity.auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        User user = new User(name, lastnames, email);
                                        db.collection(MainActivity.USER_CLASS).document(task.getResult().getUser().getUid())
                                                .set(user)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getActivity(), getString(R.string.successful_sign_up), Toast.LENGTH_LONG).show();
                                                        setFragment(homeAfterLoginFragment);
                                                        MainActivity.mainNav.getMenu().getItem(0).setChecked(true);
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), getString(R.string.error_sign_up), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                    else {
                                        Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }

            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    private boolean formValidation(String name, String lastnames, String email, String password) {
        if(!isValidName(name)) {
            nameEditText.setError("Name is not valid");
            nameEditText.requestFocus();
        }

        if(!isValidLastname(lastnames)) {
            lastnamesEditText.setError("Lastnames are not valid");
            lastnamesEditText.requestFocus();
        }

        if(!isValidEmail(email)) {
            emailEditText.setError("Email is not valid");
            emailEditText.requestFocus();
        }

        if(!isValidPassword(password)) {
            passwordEditText.setError("Password is not valid, it should have at least 6 characters");
            passwordEditText.requestFocus();
        }

        if(!isValidName(name) || !isValidLastname(lastnames) || !isValidEmail(email) || !isValidPassword(password)) {
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

    private boolean isValidEmail(String email) {
        CharSequence c_email = email;
        if(email.length() == 0) {
            return false;
        }

        return Patterns.EMAIL_ADDRESS.matcher(c_email).matches();
    }

    private boolean isValidPassword(String password) {
        if(password.length() < 6) {
            return false;
        }

        return true;
    }

}
