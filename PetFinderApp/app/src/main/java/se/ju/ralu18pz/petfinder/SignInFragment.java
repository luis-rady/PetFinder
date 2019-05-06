package se.ju.ralu18pz.petfinder;


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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInFragment extends Fragment {

    private ProgressBar progressBar;
    private Button login;
    private HomeAfterLoginFragment homeAfterLoginFragment;

    // Form fields
    private EditText emailInput;
    private EditText passwordInput;

    // Value of fields

    private String email;
    private String password;
    private boolean validForm;


    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = getView().findViewById(R.id.sign_in_progressbar);
        login = getView().findViewById(R.id.login_button);
        emailInput = getView().findViewById(R.id.email_input);
        passwordInput = getView().findViewById(R.id.password_input);


        homeAfterLoginFragment = new HomeAfterLoginFragment();

        progressBar.setVisibility(View.GONE);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailInput.getText().toString().trim();
                password = passwordInput.getText().toString().trim();

                validForm = formValidation(email, password);

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    MainActivity.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getActivity(), getString(R.string.successful_login), Toast.LENGTH_LONG).show();
                                        setFragment(homeAfterLoginFragment);
                                        MainActivity.mainNav.getMenu().getItem(0).setChecked(true);
                                    }
                                }
                            })
                            .addOnFailureListener(getActivity(), new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), getString(R.string.error_sign_in), Toast.LENGTH_LONG).show();
                                }
                            });
                }

            }
        });
    }

    private boolean formValidation(String email, String password) {
        if(!isValidEmail(email)) {
            emailInput.setError("Email is not valid");
            emailInput.requestFocus();
        }

        if(!isValidPassword(password)) {
            passwordInput.setError("Password is not valid, it should have at least 6 characters");
            passwordInput.requestFocus();
        }

        if(!isValidEmail(email) || !isValidPassword(password)) {
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


    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }
}
