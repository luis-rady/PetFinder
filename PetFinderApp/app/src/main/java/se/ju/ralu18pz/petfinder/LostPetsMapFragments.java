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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LostPetsMapFragments extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private PetInfoFragment petInfoFragment;
    private Button reportPetButton;
    private PetLostSelectionFragment petLostSelectionFragment;
    private NoAuthorizationFragment noAuthorizationFragment;

    static final LatLng current = new LatLng(57.778, 14.16);
    static final LatLng pet1 = new LatLng(57.778550, 14.161945);
    static final LatLng pet2 = new LatLng(57.776935, 14.1531);

    public LostPetsMapFragments() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_lost_pets_map_fragments, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.lost_pets_map);
        if (mapFragment == null){
            FragmentManager fm= getFragmentManager();
            FragmentTransaction ft= fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.lost_pets_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        reportPetButton = getView().findViewById(R.id.report_lost_pet_button);
        petLostSelectionFragment = new PetLostSelectionFragment();
        noAuthorizationFragment = new NoAuthorizationFragment();

        MainActivity.currentUser = MainActivity.auth.getCurrentUser();

        reportPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.currentUser == null) {
                  setFragment(noAuthorizationFragment);
                }
                else {
                    setFragment(petLostSelectionFragment);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.addMarker(new MarkerOptions().position(pet1).title("Simba"));
        mMap.addMarker(new MarkerOptions().position(pet2).title("Scar"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,15));

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(Marker arg0) {
                View v = null;
                try {

                    // Getting view from the layout file info_window_layout
                    v = getLayoutInflater().inflate(R.layout.custom_infowindow, null);

                    // Getting reference to the TextView to set latitude

                    TextView addressTxt = (TextView) v.findViewById(R.id.addressTxt);
                    addressTxt.setText(arg0.getTitle());

                } catch (Exception ev) {
                    System.out.print(ev.getMessage());
                }

                return v;

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                petInfoFragment = new PetInfoFragment();
                setFragment(petInfoFragment);
            }
        });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

}
