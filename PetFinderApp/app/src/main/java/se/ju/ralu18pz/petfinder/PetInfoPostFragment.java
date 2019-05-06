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
public class PetInfoPostFragment extends Fragment implements OnMapReadyCallback {

    public static Pet petWindowSelected;
    public static LostPost lostWindowPost;

    private LatLng petLocation;

    private GoogleMap mMap;
    SupportMapFragment mapFragment;

    private TextView owner;
    private TextView petName;
    private TextView petType;
    private TextView petSex;
    private TextView petColors;
    private TextView petCollar;
    private TextView petDescription;
    private TextView petAge;
    private TextView petDate;
    private TextView petNeutered;
    private ImageView petImage;
    private TextView contactName;
    private TextView contactPhone;
    private TextView contactExtension;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;

    private FirebaseFirestore db;


    public PetInfoPostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pet_info_post, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.pet_lost_window_map);
        if(mapFragment == null) {
            FragmentManager fm= getFragmentManager();
            FragmentTransaction ft= fm.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.pet_lost_window_map, mapFragment).commit();
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
        setOwner();
        petName.setText(petWindowSelected.name);
        Picasso.with(getActivity())
                .load(Uri.parse(petWindowSelected.petImageURL))
                .fit()
                .centerCrop()
                .into(petImage);

        petType.setText(getString(R.string.type_label) +" "+ petWindowSelected.type);
        petSex.setText(getString(R.string.sex_label) +" "+ petWindowSelected.sex);
        petCollar.setText(getString(R.string.collar_label) +" "+ petWindowSelected.collar);
        petDescription.setText(getString(R.string.description_label) +" "+ petWindowSelected.description);
        petAge.setText(getString(R.string.years_label) + ": " + petWindowSelected.years + " - " + getString(R.string.months_label) + ": " + petWindowSelected.months);
        petNeutered.setText(getString(R.string.neutered_label) +" "+ petWindowSelected.neutered);
        petDate.setText(getString(R.string.lost_on_label) +" "+ lostWindowPost.date);
        contactName.setText(getString(R.string.if_you_found) +" "+ lostWindowPost.contactName);
        contactPhone.setText(getString(R.string.phone_label) +" "+ lostWindowPost.contactPhone);
        contactExtension.setText(getString(R.string.with_extension) +" "+ lostWindowPost.contactExtension);

        String col = "";
        for (int i = 0; i < petWindowSelected.colors.size(); i++) {
            if(i == petWindowSelected.colors.size() -1) {
                col = col + petWindowSelected.colors.get(i);
            }
            else {
                col = col + petWindowSelected.colors.get(i) + " - ";
            }
        }

        petColors.setText(getString(R.string.color_label) +" "+ col);
    }

    private void setOwner() {
        db.collection(MainActivity.USER_CLASS)
                .document(petWindowSelected.userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User user = documentSnapshot.toObject(User.class);
                        owner.setText(getString(R.string.the_owner_of_the_pet_is) +" "+ user.firstName + " " + user.lastName);
                    }
                });
    }

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        petName = getView().findViewById(R.id.pet_lost_window_name);
        petImage = getView().findViewById(R.id.pet_lost_window_image);
        petType = getView().findViewById(R.id.pet_lost_window_type);
        petSex = getView().findViewById(R.id.pet_lost_window_sex);
        petColors = getView().findViewById(R.id.pet_lost_window_colors);
        petCollar = getView().findViewById(R.id.pet_lost_window_collar);
        petDescription = getView().findViewById(R.id.pet_lost_window_description);
        petAge = getView().findViewById(R.id.pet_lost_window_age);
        petDate = getView().findViewById(R.id.pet_lost_window_date);
        petNeutered = getView().findViewById(R.id.pet_lost_window_neutered);
        contactName = getView().findViewById(R.id.pet_lost_window_contact_name);
        contactPhone = getView().findViewById(R.id.pet_lost_window_contact_phone);
        contactExtension = getView().findViewById(R.id.pet_lost_window_contact_extension);
        owner = getView().findViewById(R.id.pet_lost_window_owner);
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

        petLocation = new LatLng(lostWindowPost.latitude, lostWindowPost.longitude);
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
