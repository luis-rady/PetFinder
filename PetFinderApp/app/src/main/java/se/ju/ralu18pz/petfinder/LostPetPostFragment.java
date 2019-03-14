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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class LostPetPostFragment extends Fragment implements OnMapReadyCallback {
    private static final int REQUEST_LOCATION = 246;
    private GoogleMap mMap;
    SupportMapFragment lostPostMapFragment;
    private TextView lostDate;
    private int markerCount;


    private MarkerOptions petMarker;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    static final LatLng current = new LatLng(57.778, 14.16);
    static final LatLng pet1 = new LatLng(57.778550, 14.161945);
    static final LatLng pet2 = new LatLng(57.776935, 14.1531);

    public LostPetPostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_lost_pet_post, container, false);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        markerCount = 0;
        lostPostMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.post_lost_pet_map);
        if(lostPostMapFragment == null) {
            FragmentManager fm= getFragmentManager();
            FragmentTransaction ft= fm.beginTransaction();
            lostPostMapFragment = SupportMapFragment.newInstance();
            ft.replace(R.id.post_lost_pet_map, lostPostMapFragment).commit();
        }
        lostPostMapFragment.getMapAsync(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        denyLocationPermissionFragment = new DenyLocationPermissionFragment();
        lostDate = getView().findViewById(R.id.lost_date_text);

        lostDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog, dateSetListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                dialog.show();
            }
        });

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month += 1;
                String date = month + "/" + dayOfMonth + "/" + year;
                lostDate.setText(date);
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            setFragment(denyLocationPermissionFragment);
        }
        else {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
        }

        //mMap.addMarker(new MarkerOptions().position(pet1).title("Simba"));
        //mMap.addMarker(new MarkerOptions().position(pet2).title("Scar"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current,15));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //map.clear();
                if(markerCount == 0) {
                    petMarker = new MarkerOptions().position(point);
                    mMap.addMarker(petMarker);
                    markerCount++;
                }
                else {
                    mMap.clear();
                    petMarker.position(point);
                    mMap.addMarker(petMarker);
                }
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
