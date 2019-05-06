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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;



public class LostPetsMapFragments extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LatLng currentPosition;

    private FirebaseFirestore db;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;
    private PetInfoPostFragment petInfoPostFragment;
    private PetLostSelectionFragment petLostSelectionFragment;
    private NoAuthorizationFragment noAuthorizationFragment;


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
        setInputs();
    }

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        petLostSelectionFragment = new PetLostSelectionFragment();
        noAuthorizationFragment = new NoAuthorizationFragment();

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
                petInfoPostFragment = new PetInfoPostFragment();

                String[] snippet = marker.getSnippet().split("#", 2);

                db.collection(MainActivity.PET_CLASS)
                        .whereEqualTo("petImageURL", snippet[1])
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Pet pet = new Pet();
                                for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                    pet = documentSnapshot.toObject(Pet.class);
                                }

                                petInfoPostFragment.petWindowSelected = pet;

                                db.collection(MainActivity.LOST_COLLECTION)
                                        .whereEqualTo("petId", pet.id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                LostPost lostPost = new LostPost();
                                                for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                    lostPost = queryDocumentSnapshot.toObject(LostPost.class);
                                                }
                                                petInfoPostFragment.lostWindowPost = lostPost;
                                                setFragment(petInfoPostFragment);
                                            }
                                        });
                            }
                        });
            }
        });

    }

    private void bringMarkers() {
        db.collection(MainActivity.LOST_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            final LostPost lostPost = documentSnapshot.toObject(LostPost.class);
                            final LatLng lostLocation = new LatLng(lostPost.latitude, lostPost.longitude);

                            db.collection(MainActivity.PET_CLASS)
                                    .document(lostPost.petId)
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            Pet pet = documentSnapshot.toObject(Pet.class);
                                            String snippet = getString(R.string.lost_on_label) +" "+ lostPost.date + "#" + pet.petImageURL;
                                            mMap.addMarker(new MarkerOptions().position(lostLocation).title(pet.name).snippet(snippet));
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
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
        FragmentTransaction fragmentTransaction = getParentFragment().getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
