package se.ju.ralu18pz.petfinder;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class ProfileFragment extends Fragment {

    private Button signInButton;
    private Button signUpButton;

    private SignInFragment signInFragment;
    private SignUpFragment signUpFragment;

    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        signInButton = view.findViewById(R.id.sign_in_button);
        signUpButton = view.findViewById(R.id.sign_up_button);

        signUpFragment = new SignUpFragment();
        signInFragment = new SignInFragment();

        setFragment(signInFragment);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(signUpFragment);
            }
        });


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(signInFragment);
            }
        });
        return view;
    }


    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.profile_fragment, fragment);
        fragmentTransaction.commit();
    }
}
