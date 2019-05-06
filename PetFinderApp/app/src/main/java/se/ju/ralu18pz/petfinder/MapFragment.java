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
import android.widget.FrameLayout;

public class MapFragment extends Fragment {
    private Button lostMapButton;
    private Button foundMapButton;
    private Button moveTo;

    private LostPetsMapFragments lostPetsMapFragments;
    private FoundPetsMapFragment foundPetsMapFragment;

    private NoAuthorizationFragment noAuthorizationFragment;
    private PetLostSelectionFragment petLostSelectionFragment;
    private FoundPostPetFragment foundPostPetFragment;

    private FrameLayout map;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();

        lostMapButton = getView().findViewById(R.id.lost_pets_button);
        foundMapButton = getView().findViewById(R.id.found_pets_button);
        moveTo = getView().findViewById(R.id.move_to_pets_button);
        map = getView().findViewById(R.id.map_fragment);

        petLostSelectionFragment = new PetLostSelectionFragment();
        foundPostPetFragment = new FoundPostPetFragment();
        noAuthorizationFragment = new NoAuthorizationFragment();

        lostPetsMapFragments = new LostPetsMapFragments();
        foundPetsMapFragment = new FoundPetsMapFragment();

        setMapFragment(lostPetsMapFragments);
        moveTo.setText(getString(R.string.report_lost_pet));

        lostMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTo.setText(getString(R.string.report_lost_pet));
                setMapFragment(lostPetsMapFragments);
            }
        });

        foundMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTo.setText(getString(R.string.report_found_pet));
                setMapFragment(foundPetsMapFragment);
            }
        });

        moveTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(moveTo.getText() == getString(R.string.report_lost_pet)) {
                    if(MainActivity.currentUser == null) {
                        setFragment(noAuthorizationFragment);
                    }
                    else {
                        setFragment(petLostSelectionFragment);
                    }
                }
                else {
                    if(MainActivity.currentUser == null) {
                        setFragment(noAuthorizationFragment);
                    }
                    else {
                        setFragment(foundPostPetFragment);
                    }
                }
            }
        });

    }

    private void setMapFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map_fragment, fragment);
        fragmentTransaction.commit();
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
