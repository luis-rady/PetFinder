package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class PetInfoFoundPostFragment extends Fragment implements OnMapReadyCallback {

    public static FoundPost foundPostWindowSelected;

    private LatLng petLocation;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private TextView createdBy;
    private TextView postStatus;
    private TextView postSituation;
    private TextView postType;
    private TextView postSex;
    private TextView postColors;
    private TextView postCollar;
    private TextView postDescription;
    private TextView postDate;
    private TextView postNeutered;
    private ImageView postImage;
    private TextView contactName;
    private TextView contactPhone;
    private TextView contactExtension;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;

    FirebaseFirestore db;

    public PetInfoFoundPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pet_info_found_post, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.pet_found_window_map);
        if(mapFragment == null) {
            FragmentManager fm= getFragmentManager();
            FragmentTransaction ft= fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.pet_found_window_map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setInputs();
        setValues();
    }

    private void setValues() {
        setContentCreator();
        Picasso.with(getContext())
                .load(Uri.parse(foundPostWindowSelected.postImage))
                .fit()
                .centerCrop()
                .into(postImage);

        postStatus.setText("The pet is: " + foundPostWindowSelected.petStatus);
        postSituation.setText("The situation of the pet is: " + foundPostWindowSelected.petSituation);
        postType.setText("The pet is a: " + foundPostWindowSelected.petType);
        postSex.setText("The sex of the pet is: " + foundPostWindowSelected.petSex);
        postCollar.setText("The pet has a collar: " +foundPostWindowSelected.petCollar);
        postDescription.setText("Description: " + foundPostWindowSelected.description);
        postDate.setText("Day it was found: " + foundPostWindowSelected.date);
        postNeutered.setText("The pet is neutered: " + foundPostWindowSelected.petNeutered);
        contactName.setText("If is your pet contact: " + foundPostWindowSelected.contactName);
        contactPhone.setText("Phone: " + foundPostWindowSelected.contactPhone);
        contactExtension.setText(" with extension: " + foundPostWindowSelected.contactExtension);

        String col = "";
        for (int i = 0; i < foundPostWindowSelected.petColors.size(); i++) {
            if(i == foundPostWindowSelected.petColors.size() -1) {
                col = col + foundPostWindowSelected.petColors.get(i);
            }
            else {
                col = col + foundPostWindowSelected.petColors.get(i) + " - ";
            }
        }

        postColors.setText("Colors of the pet: " + col);
    }

    private void setContentCreator() {
        db.collection(MainActivity.USER_CLASS)
                .document(foundPostWindowSelected.userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);

                        createdBy.setText("Post created by: " + user.firstName + " " + user.lastName);
                    }
                });
    }

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        postStatus = getView().findViewById(R.id.pet_found_window_status);
        postSituation = getView().findViewById(R.id.pet_found_window_situation);
        postType = getView().findViewById(R.id.pet_found_window_type);
        postSex = getView().findViewById(R.id.pet_found_window_sex);
        postCollar = getView().findViewById(R.id.pet_found_window_collar);
        postDescription = getView().findViewById(R.id.pet_found_window_description);
        postDate = getView().findViewById(R.id.pet_found_window_date);
        postColors = getView().findViewById(R.id.pet_found_window_colors);
        postNeutered = getView().findViewById(R.id.pet_found_window_neutered);
        postImage = getView().findViewById(R.id.pet_found_window_image);
        contactName = getView().findViewById(R.id.pet_found_window_contact_name);
        contactPhone = getView().findViewById(R.id.pet_found_window_contact_phone);
        contactExtension = getView().findViewById(R.id.pet_found_window_contact_extension);
        createdBy = getView().findViewById(R.id.pet_found_window_creator);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (!checkPermission()) {
            denyLocationPermissionFragment = new DenyLocationPermissionFragment();
            setFragment(denyLocationPermissionFragment);
            return;
        }

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        petLocation = new LatLng(foundPostWindowSelected.latitude, foundPostWindowSelected.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(petLocation, 14));
        mMap.addMarker(new MarkerOptions().position(petLocation));
    }

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
