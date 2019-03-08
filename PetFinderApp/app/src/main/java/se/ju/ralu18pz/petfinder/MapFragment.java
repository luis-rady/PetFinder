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

public class MapFragment extends Fragment {
    private Button lostMapButton;
    private Button foundMapButton;

    private LostPetsMapFragments lostPetsMapFragments;
    private FoundPetsMapFragment foundPetsMapFragment;

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

        lostMapButton = getView().findViewById(R.id.lost_pets_button);
        foundMapButton = getView().findViewById(R.id.found_pets_button);

        lostPetsMapFragments = new LostPetsMapFragments();
        foundPetsMapFragment = new FoundPetsMapFragment();

        setMapFragment(lostPetsMapFragments);

        lostMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMapFragment(lostPetsMapFragments);
            }
        });

        foundMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMapFragment(foundPetsMapFragment);
            }
        });

    }

    private void setMapFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map_fragment, fragment);
        fragmentTransaction.commit();
    }
}
