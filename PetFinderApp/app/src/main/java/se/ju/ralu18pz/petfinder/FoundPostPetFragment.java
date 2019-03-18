    package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class FoundPostPetFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment foundPostMapFragment;
    private MarkerOptions petMarker;

    private DenyLocationPermissionFragment denyLocationPermissionFragment;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    private LatLng currentPosition;
    private int markerCount;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private MapFragment mapFragment;

    private int REQUEST_CAMERA = 1;
    private int SELECT_FILE = 0;
    private int WRITE_EXTERNAL = 2;

    private ImageView postImage;
    private Spinner typeSpinner;
    private Spinner sexSpinner;
    private Spinner neuteredSpinner;
    private Spinner collarSpinner;
    private Spinner statusSpinner;
    private Spinner situationSpinner;
    private Button foundDateButton;
    private TextView foundDateText;
    private Button post;
    private EditText contactNameInput;
    private EditText contactPhoneInput;
    private EditText extensionInput;
    private TextView checkboxError;
    private EditText descriptionInput;
    private TextView mapLabel;

    private CheckBox blackColor;
    private CheckBox brownColor;
    private CheckBox whiteColor;
    private CheckBox goldenColor;
    private CheckBox greyColor;
    private CheckBox tanColor;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private Uri file;

    private int yearSelected, monthSelected, daySelected;
    private String type, sex, neutered, collar, status, situation, date, contactName, contactPhone, contactExtension, description;
    private ArrayList<String> colors;
    private LatLng foundPoint;
    private boolean validForm;

    public FoundPostPetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_found_post_pet, container, false);

        foundPostMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.post_found_pet_map);
        if(foundPostMapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            foundPostMapFragment = SupportMapFragment.newInstance();
            fragmentTransaction.replace(R.id.post_found_pet_map, foundPostMapFragment).commit();
        }

        foundPostMapFragment.getMapAsync(this);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setInputs();

        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInputValues();

                validForm = formValidation();

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    uploadData();
                }
            }
        });

        postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

    }

    private void uploadData() {
        progressBar.setVisibility(View.VISIBLE);
        final StorageReference foundPostRef = storageReference.child("users/" + MainActivity.currentUser.getUid() + "/foundPosts/" + System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));
        foundPostRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        foundPostRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        FoundPost foundPost = new FoundPost(uri.toString(), type, description, sex, neutered,
                                                collar, status, situation, contactName, contactPhone,
                                                contactExtension, colors, date, foundPoint.latitude, foundPoint.longitude, MainActivity.currentUser.getUid());

                                        db.collection(MainActivity.FOUND_COLLECTION)
                                                .add(foundPost)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        progressBar.setVisibility(View.GONE);
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
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
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
            foundDateText.setError("Date is invalid");
            foundDateText.requestFocus();
            valid = false;
        }

        if(foundPoint == null) {
            mapLabel.setError("Add a location of where it went lost");
            mapLabel.requestFocus();
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

        if(colors.isEmpty()) {
            checkboxError.setVisibility(View.VISIBLE);
            checkboxError.setText("You should select at least one color");
        }
        else {
            checkboxError.setVisibility(View.GONE);
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

    private void getInputValues() {
        type = typeSpinner.getSelectedItem().toString().trim();
        sex = sexSpinner.getSelectedItem().toString().trim();
        neutered = neuteredSpinner.getSelectedItem().toString().trim();
        collar = collarSpinner.getSelectedItem().toString().trim();
        status = statusSpinner.getSelectedItem().toString().trim();
        situation = situationSpinner.getSelectedItem().toString().trim();
        date = foundDateText.getText().toString().trim();
        contactName = contactNameInput.getText().toString().trim();
        contactPhone = contactPhoneInput.getText().toString().trim();
        contactExtension = extensionInput.getText().toString().trim();
        description = descriptionInput.getText().toString().trim();
        if(petMarker != null) {
            foundPoint = petMarker.getPosition();
        }
        else {
            foundPoint = null;
        }

        colors = new ArrayList<String>();

        if(blackColor.isChecked()) {
            colors.add(blackColor.getText().toString());
        }
        else if(colors.contains(blackColor.getText().toString())) {
            colors.remove(blackColor.getText().toString());
        }

        if(brownColor.isChecked()) {
            colors.add(brownColor.getText().toString());
        }
        else if(colors.contains(brownColor.getText().toString()))  {
            colors.remove(brownColor.getText().toString());
        }

        if(whiteColor.isChecked()) {
            colors.add(whiteColor.getText().toString());
        }
        else if(colors.contains(whiteColor.getText().toString())){
            colors.remove(whiteColor.getText().toString());
        }

        if(greyColor.isChecked()) {
            colors.add(greyColor.getText().toString());
        }
        else if(colors.contains(greyColor.getText().toString())) {
            colors.remove(greyColor.getText().toString());
        }

        if(goldenColor.isChecked()) {
            colors.add(goldenColor.getText().toString());
        }
        else if(colors.contains(goldenColor.getText().toString())) {
            colors.remove(goldenColor.getText().toString());
        }

        if(tanColor.isChecked()) {
            colors.add(tanColor.getText().toString());
        }
        else if(colors.contains(tanColor.getText().toString())){
            colors.remove(tanColor.getText().toString());
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
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        db = FirebaseFirestore.getInstance();

        postImage = getView().findViewById(R.id.post_found_image);
        typeSpinner = getView().findViewById(R.id.found_spinner_pet_type);
        sexSpinner = getView().findViewById(R.id.found_spinner_pet_sex);
        neuteredSpinner = getView().findViewById(R.id.found_spinner_pet_neutered);
        collarSpinner = getView().findViewById(R.id.found_spinner_pet_collar);
        statusSpinner = getView().findViewById(R.id.found_spinner_pet_status);
        situationSpinner = getView().findViewById(R.id.found_spinner_pet_situation);
        foundDateButton = getView().findViewById(R.id.found_date_button);
        foundDateText = getView().findViewById(R.id.found_date_text);
        contactNameInput = getView().findViewById(R.id.found_post_name);
        contactPhoneInput = getView().findViewById(R.id.found_post_contact);
        extensionInput = getView().findViewById(R.id.found_post_extension);
        post = getView().findViewById(R.id.found_post_button);
        progressBar = getView().findViewById(R.id.found_post_progressbar);
        descriptionInput = getView().findViewById(R.id.pet_description_input_found);
        mapLabel = getView().findViewById(R.id.found_map_label);

        blackColor = getView().findViewById(R.id.found_black_color);
        brownColor = getView().findViewById(R.id.found_brown_color);
        whiteColor = getView().findViewById(R.id.found_white_color);
        goldenColor = getView().findViewById(R.id.found_golden_color);
        greyColor = getView().findViewById(R.id.found_grey_color);
        tanColor = getView().findViewById(R.id.found_tan_color);
        checkboxError = getView().findViewById(R.id.checkboxes_error_found_post);

        checkboxError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        selectedImageUri = Uri.parse("android.resource://" + getContext().getPackageName() +  "/" + R.drawable.profile_default);
        Picasso.with(getActivity())
                .load(selectedImageUri)
                .fit()
                .centerCrop()
                .into(postImage);
        postImage.setImageURI(selectedImageUri);

        setCalendar();
    }

    private void setCalendar() {
        foundDateButton.setOnClickListener(new View.OnClickListener() {
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
                foundDateText.setText(date);

                yearSelected = year;
                monthSelected = month;
                daySelected = dayOfMonth;
            }
        };
    }

    private void SelectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add profile picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Camera")) {
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
                    }
                    else {
                        cameraIntent();
                    }
                }
                else if(items[which].equals("Gallery")) {
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SELECT_FILE);
                    }
                    else {
                        galleryIntent();
                    }
                }
                else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == SELECT_FILE) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                galleryIntent();
            }
            else {
                Toast toast= Toast.makeText(getActivity(), "Gallery permission not granted", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if(requestCode == REQUEST_CAMERA) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();
            }
            else {
                Toast toast = Toast.makeText(getActivity(), "Camera permission not granted", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else {
            Toast toast = Toast.makeText(getActivity(), "is neither", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == REQUEST_CAMERA) {
                Picasso.with(getActivity())
                        .load(file)
                        .fit()
                        .centerCrop()
                        .into(postImage);
                postImage.setImageURI(file);
                selectedImageUri = file;
            }
            else if(requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                Picasso.with(getActivity())
                        .load(selectedImageUri)
                        .fit()
                        .centerCrop()
                        .into(postImage);
                postImage.setImageURI(selectedImageUri);
            }
        }
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        requestWritingPermission();
        file = getOutputMediaFileUri(WRITE_EXTERNAL);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    public Uri getOutputMediaFileUri(int type) {
        return FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", getOutputMediaFile());
    }

    private void requestWritingPermission() {
        if(Build.VERSION.SDK_INT >= 23) {
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL);
            }
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PetFinder");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                System.out.println("Failed creating PetFinder directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getAbsolutePath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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
}
