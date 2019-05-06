package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class LostPetPostFragment extends Fragment implements OnMapReadyCallback {

    public static Pet lostPetSelected;

    private GoogleMap mMap;
    private SupportMapFragment lostPostMapFragment;
    private MarkerOptions petMarker;

    private TextView petName;
    private TextView lostMapLabel;
    private Button lostDateButton;
    private TextView lostDateText;
    private Spinner circumstanceInput;
    private EditText descriptionInput;
    private Button post;
    private ProgressBar progressBar;
    private EditText contactNameInput;
    private EditText contactPhoneInput;
    private EditText extensionInput;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    private LatLng currentPosition;
    private int markerCount;
    private String date, circumstance, description, contactName, contactPhone, contactExtension;
    private LatLng lostPoint;
    private boolean validForm;
    private int yearSelected, monthSelected, daySelected;

    private FirebaseFirestore db;
    private PetLostSelectionFragment petLostSelectionFragment;

    public LostPetPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_lost_pet_post, container, false);

        lostPostMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.post_lost_pet_map);
        if (lostPostMapFragment == null) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            lostPostMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.post_lost_pet_map, lostPostMapFragment).commit();
        }

        lostPostMapFragment.getMapAsync(this);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInputs();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValues();
                validForm = formValidation();

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    uploadPost();
                }
            }
        });
    }

    private void updatePet() {
        lostPetSelected.lost = true;
        db.collection(MainActivity.PET_CLASS).document(lostPetSelected.id)
                .set(lostPetSelected)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), getString(R.string.post_successful), Toast.LENGTH_LONG).show();
                        ((FragmentActivity) getContext()).getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadPost() {
        LostPost lostPost = new LostPost(date, circumstance, lostPetSelected.id, MainActivity.currentUser.getUid(), description, lostPoint.latitude, lostPoint.longitude, contactName, contactPhone, contactExtension);
        db.collection(MainActivity.LOST_COLLECTION)
                .add(lostPost)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        updatePet();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean formValidation() {
        boolean valid = true;

        if(date == getString(R.string.date_label_lost) || !validDate()) {
            lostDateText.setError("Date is invalid");
            lostDateText.requestFocus();
            valid = false;
        }

        if(lostPoint == null) {
            lostMapLabel.setError("Add a location of where it went lost");
            lostMapLabel.requestFocus();
            valid = false;
        }

        if(contactName == "" || contactName.length() == 0) {
            contactNameInput.setError("Contact name should not be empty");
            contactNameInput.requestFocus();
            valid = false;
        }

        if(contactPhone == "" || contactPhone.length() == 0) {
            contactPhoneInput.setError("Contact phone should not be empty");
            contactPhoneInput.requestFocus();
            valid = false;
        }

        if(contactExtension == "" || contactExtension.length() == 0) {
            extensionInput.setError("Enter the extension of your country");
            extensionInput.requestFocus();
            valid = false;
        }

        if(!valid) {
            return false;
        }

        return true;
    }

    private boolean validDate() {
        Calendar today = Calendar.getInstance();
        Calendar selectedDay = Calendar.getInstance();

        selectedDay.set(yearSelected, monthSelected, daySelected);
        if(selectedDay.after(today)) {
            return false;
        }

        return true;
    }

    private void getValues() {
        date = lostDateText.getText().toString();
        circumstance = circumstanceInput.getSelectedItem().toString().trim();
        description = descriptionInput.getText().toString().trim();
        contactName = contactNameInput.getText().toString().trim();
        contactPhone = contactPhoneInput.getText().toString().trim();
        contactExtension = extensionInput.getText().toString().trim();

        if(petMarker != null) {
            lostPoint = petMarker.getPosition();
        }
        else {
            lostPoint = null;
        }
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

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        petName = getView().findViewById(R.id.pet_lost_name);
        lostMapLabel = getView().findViewById(R.id.lost_map_label);
        lostDateButton = getView().findViewById(R.id.lost_date_button);
        lostDateText = getView().findViewById(R.id.lost_date_text);
        circumstanceInput = getView().findViewById(R.id.lost_spinner_pet_circumstance);
        descriptionInput = getView().findViewById(R.id.lost_description_input);
        post = getView().findViewById(R.id.lost_post_button);
        progressBar = getView().findViewById(R.id.lost_post_progressbar);
        contactNameInput = getView().findViewById(R.id.lost_post_name);
        contactPhoneInput = getView().findViewById(R.id.lost_post_contact);
        extensionInput = getView().findViewById(R.id.lost_post_extension);

        progressBar.setVisibility(View.GONE);
        petName.setText("Report " + lostPetSelected.name + "'s lost");
        markerCount = 0;
        setCalendar();
    }

    private void setCalendar() {
        lostDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog, dateSetListener, year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = (month + 1) + "/" + dayOfMonth + "/" + year;
                lostDateText.setText(date);

                yearSelected = year;
                monthSelected = month;
                daySelected = dayOfMonth;
            }
        };
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //map.clear();
                if (markerCount == 0) {
                    petMarker = new MarkerOptions().position(point);
                    mMap.addMarker(petMarker);
                    markerCount++;
                } else {
                    mMap.clear();
                    petMarker.position(point);
                    mMap.addMarker(petMarker);
                }
            }
        });
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
