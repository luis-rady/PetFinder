package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditPetFragment extends Fragment {

    private int REQUEST_CAMERA = 1;
    private int SELECT_FILE = 0;
    private int WRITE_EXTERNAL = 2;

    private Pet currentPet = PetInfoFragment.selectedPet;

    private ImageView petImage;
    private EditText petNameInput;
    private Spinner typePetSpinner;
    private Spinner sexPetSpinner;
    private Spinner neuteredPetSpinner;
    private Spinner collarPetSpinner;
    private EditText yearsPetInput;
    private EditText monthsPetInput;
    private EditText descriptionPetInput;
    private ProgressBar progressBar;

    private CheckBox blackColor;
    private CheckBox brownColor;
    private CheckBox whiteColor;
    private CheckBox goldenColor;
    private CheckBox greyColor;
    private CheckBox tanColor;
    private TextView checkboxError;

    private Button updatePet;

    private PetsFragment petsFragment;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;
    private Uri selectedImageUri;
    private Uri file;

    private String name, type, sex, neutered, collar, years, months, description;
    private ArrayList<String> colors;
    private boolean validForm;


    public EditPetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setInputs();
        setInputValues();

        updatePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValuesOfInputs();
                validForm = formValidation();

                if(validForm) {
                    progressBar.setVisibility(View.VISIBLE);
                    updateData();
                    eraseExistentPet();
                }
            }
        });


        petImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
    }

    private void eraseExistentPet() {
        if(selectedImageUri.toString() != currentPet.petImageURL) {
            final StorageReference pictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(currentPet.petImageURL);
            pictureRef.delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            uploadPet();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
        else {
            db.collection(MainActivity.PET_CLASS).document(currentPet.id)
                    .set(currentPet)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), getString(R.string.pet_updated), Toast.LENGTH_LONG).show();
                            petsFragment = new PetsFragment();
                            setFragment(petsFragment);
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

    private void uploadPet() {
        final StorageReference userPetsRef = storageReference.child("users/" + MainActivity.currentUser.getUid() + "/pets/" + System.currentTimeMillis() + "." + getFileExtension(selectedImageUri));
        userPetsRef.putFile(selectedImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        userPetsRef.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String pastPetId = currentPet.id;
                                        currentPet.id = taskSnapshot.getMetadata().getName();
                                        currentPet.petImageURL = uri.toString();
                                        db.collection(MainActivity.PET_CLASS).document(pastPetId)
                                                .set(currentPet)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(getActivity(), getString(R.string.pet_updated), Toast.LENGTH_LONG).show();
                                                        petsFragment = new PetsFragment();
                                                        setFragment(petsFragment);
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

    private void updateData() {
        currentPet.name = name;
        currentPet.type = type;
        currentPet.sex = sex;
        currentPet.neutered = neutered;
        currentPet.colors = colors;
        currentPet.collar = collar;
        currentPet.description = description;
        currentPet.years = years;
        currentPet.months = months;
    }

    private boolean formValidation() {
        boolean valid = true;

        if(name == "" || name.length() == 0) {
            petNameInput.setError("Pet should have a name");
            petNameInput.requestFocus();
            valid = false;
        }

        if(years == "" || years.length() == 0) {
            yearsPetInput.setError("If it doesn't have any years, put a 0");
            yearsPetInput.requestFocus();
            valid = false;
        }

        if(months == "" || years.length() == 0) {
            monthsPetInput.setError("Months should not be none");
            monthsPetInput.requestFocus();
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

    private void getValuesOfInputs() {
        name = petNameInput.getText().toString().trim();
        type = typePetSpinner.getSelectedItem().toString().trim();
        sex = sexPetSpinner.getSelectedItem().toString().trim();
        neutered = neuteredPetSpinner.getSelectedItem().toString().trim();
        collar = collarPetSpinner.getSelectedItem().toString().trim();
        years = yearsPetInput.getText().toString().trim();
        months = monthsPetInput.getText().toString().trim();
        description = descriptionPetInput.getText().toString().trim();
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

    private void setInputs() {
        petImage = getView().findViewById(R.id.pet_image_edit);
        petNameInput = getView().findViewById(R.id.pet_name_input_edit);
        typePetSpinner = getView().findViewById(R.id.edit_spinner_pet_type);
        sexPetSpinner = getView().findViewById(R.id.edit_spinner_pet_sex);
        neuteredPetSpinner = getView().findViewById(R.id.edit_spinner_pet_neutered);
        collarPetSpinner = getView().findViewById(R.id.edit_spinner_pet_collar);
        yearsPetInput = getView().findViewById(R.id.pet_years_input_edit);
        monthsPetInput = getView().findViewById(R.id.pet_months_input_edit);
        descriptionPetInput = getView().findViewById(R.id.pet_description_input_edit);

        progressBar = getView().findViewById(R.id.edit_pet_progress_bar);

        blackColor = getView().findViewById(R.id.edit_black_color);
        brownColor = getView().findViewById(R.id.edit_brown_color);
        whiteColor = getView().findViewById(R.id.edit_white_color);
        goldenColor = getView().findViewById(R.id.edit_golden_color);
        greyColor = getView().findViewById(R.id.edit_grey_color);
        tanColor = getView().findViewById(R.id.edit_tan_color);
        checkboxError = getView().findViewById(R.id.checkboxes_error_edit);

        updatePet = getView().findViewById(R.id.pet_edit_info_button);
    }

    private void setInputValues() {
        selectedImageUri = Uri.parse(currentPet.petImageURL);
        Picasso.with(getContext())
                .load(currentPet.petImageURL)
                .fit()
                .centerCrop()
                .into(petImage);
        petImage.setImageURI(selectedImageUri);

        petNameInput.setText(currentPet.name);

        ArrayAdapter arrayAdapter = (ArrayAdapter) typePetSpinner.getAdapter();
        typePetSpinner.setSelection(arrayAdapter.getPosition(currentPet.type));

        arrayAdapter = (ArrayAdapter) sexPetSpinner.getAdapter();
        sexPetSpinner.setSelection(arrayAdapter.getPosition(currentPet.sex));

        arrayAdapter = (ArrayAdapter) neuteredPetSpinner.getAdapter();
        neuteredPetSpinner.setSelection((arrayAdapter.getPosition(currentPet.neutered)));

        arrayAdapter = (ArrayAdapter) collarPetSpinner.getAdapter();
        collarPetSpinner.setSelection(arrayAdapter.getPosition(currentPet.collar));

        descriptionPetInput.setText(currentPet.description);
        yearsPetInput.setText(currentPet.years);
        monthsPetInput.setText(currentPet.months);

        for(int i = 0; i < currentPet.colors.size(); i++) {
            switch(currentPet.colors.get(i)) {
                case "Black":
                    blackColor.setChecked(true);
                    break;
                case "Brown":
                    brownColor.setChecked(true);
                    break;
                case "White":
                    whiteColor.setChecked(true);
                    break;
                case "Grey":
                    greyColor.setChecked(true);
                    break;
                case "Golden":
                    goldenColor.setChecked(true);
                    break;
                    default:
                        tanColor.setChecked(true);

            }
        }

        progressBar.setVisibility(View.GONE);
        checkboxError.setVisibility(View.GONE);
    }

    private void SelectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add picture to pet");
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
                        .into(petImage);
                petImage.setImageURI(file);
                selectedImageUri = file;
            }
            else if(requestCode == SELECT_FILE) {
                selectedImageUri = data.getData();
                Picasso.with(getActivity()).load(selectedImageUri).into(petImage);
                petImage.setImageURI(selectedImageUri);
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

}
