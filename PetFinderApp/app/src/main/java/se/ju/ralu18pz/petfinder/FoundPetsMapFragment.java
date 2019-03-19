package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoundPetsMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentPosition;

    private FirebaseFirestore db;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;
    private NoAuthorizationFragment noAuthorizationFragment;
    private FoundPostPetFragment foundPostPetFragment;
    private PetInfoFoundPostFragment petInfoFoundPostFragment;

    private Button reportPetButton;


    public FoundPetsMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_found_pets_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.found_pets_map);
        if (mapFragment == null){
            FragmentManager fm= getFragmentManager();
            FragmentTransaction ft= fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.found_pets_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInputs();

        reportPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(MainActivity.currentUser == null) {
                    setFragment(noAuthorizationFragment);
                }
                else {
                    setFragment(foundPostPetFragment);
                }
            }
        });
    }

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db =FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        reportPetButton = getView().findViewById(R.id.report_found_pet_button);

        noAuthorizationFragment = new NoAuthorizationFragment();
        foundPostPetFragment = new FoundPostPetFragment();

    }

    private void setCurrentPosition() {
        if(!checkPermission()) {
            denyLocationPermissionFragment = new DenyLocationPermissionFragment();
            setFragment(denyLocationPermissionFragment);
            return;
        }

        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null) {
                            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                        else {
                            currentPosition = new LatLng(57.778, 14.16);
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14));

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        currentPosition = new LatLng(57.778, 14.16);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14));
                    }
                });
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!checkPermission()) {
            denyLocationPermissionFragment = new DenyLocationPermissionFragment();
            setFragment(denyLocationPermissionFragment);
            return;
        }

        mMap = googleMap;
        setCurrentPosition();
        mMap.setMyLocationEnabled(true);

        bringMarkers();
        CustomInfoWindowAdapter customInfoWindowAdapter = new CustomInfoWindowAdapter(getContext());
        mMap.setInfoWindowAdapter(customInfoWindowAdapter);

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                petInfoFoundPostFragment = new PetInfoFoundPostFragment();
                String[] snippet = marker.getSnippet().split("#", 2);

                db.collection(MainActivity.FOUND_COLLECTION)
                        .whereEqualTo("postImage", snippet[1])
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                FoundPost foundPost = new FoundPost();
                                for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    foundPost = documentSnapshot.toObject(FoundPost.class);
                                }

                                petInfoFoundPostFragment.foundPostWindowSelected = foundPost;
                                setFragment(petInfoFoundPostFragment);
                            }
                        });

            }
        });
    }

    private void bringMarkers() {
        db.collection(MainActivity.FOUND_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            FoundPost foundPost= documentSnapshot.toObject(FoundPost.class);
                            System.out.println("The contact name of the pet is -> " + foundPost.contactName);
                            LatLng foundLocation = new LatLng(foundPost.latitude, foundPost.longitude);

                            String title = "Is a " + foundPost.petType;
                            String snippet = "Found on " + foundPost.date + "#" + foundPost.postImage;

                            mMap.addMarker(new MarkerOptions().position(foundLocation).title(title).snippet(snippet));
                            System.out.println("The marker was created");
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

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
