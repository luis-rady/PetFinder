package se.ju.ralu18pz.petfinder;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddPetFragment extends Fragment {
    private int REQUEST_CAMERA = 1;
    private int SELECT_FILE = 0;

    private ImageView petImage;
    private EditText petNameInput;
    private Spinner typePetSpinner;
    private Spinner sexPetSpinner;
    private Spinner neuteredPetSpinner;
    private Spinner collarPetSpinner;
    private EditText yearsPetInput;
    private EditText montsPetInput;
    private EditText descriptionPetInput;

    private CheckBox blackColor;
    private CheckBox brownColor;
    private CheckBox whiteColor;
    private CheckBox goldenColor;
    private CheckBox greyColor;
    private CheckBox tanColor;

    private Button registerPet;
    private Button cancel;

    private PetsFragment petsFragment;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseFirestore db;

    //private int color_counter = 0;
    private String name, type, sex, neutered, collar, years, months, description, imageUri;
    private ArrayList<String> colors;

    public AddPetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_pet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setInputs();

        registerPet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = petNameInput.getText().toString().trim();
                type = typePetSpinner.getSelectedItem().toString().trim();
                sex = sexPetSpinner.getSelectedItem().toString().trim();
                neutered = neuteredPetSpinner.getSelectedItem().toString().trim();
                collar = collarPetSpinner.getSelectedItem().toString().trim();
                years = yearsPetInput.getText().toString().trim();
                months = montsPetInput.getText().toString().trim();
                description = descriptionPetInput.getText().toString().trim();

                if(blackColor.isChecked()) { colors.add(blackColor.getText().toString()); }
                if(brownColor.isChecked()) { colors.add(brownColor.getText().toString()); }
                if(whiteColor.isChecked()) { colors.add(whiteColor.getText().toString()); }
                if(greyColor.isChecked()) { colors.add(greyColor.getText().toString()); }
                if(goldenColor.isChecked()) { colors.add(goldenColor.getText().toString()); }
                if(tanColor.isChecked()) { colors.add(tanColor.getText().toString()); }

                uploadData();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                petsFragment = new PetsFragment();
                setFragment(petsFragment);
            }
        });


        petImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
    }

    private void uploadData() {
        petImage.setDrawingCacheEnabled(true);
        petImage.buildDrawingCache();
        Bitmap bitmap = petImage.getDrawingCache();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();

        imageUri = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        StorageReference userPetsRef = storageReference.child("users/" + MainActivity.currentUser.getUid() + "/pets/" + imageUri);
        UploadTask uploadTask = userPetsRef.putBytes(byteFormat);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), getString(R.string.pet_register_success), Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                //Toast.makeText(getActivity(), getString(R.string.error_title), Toast.LENGTH_LONG).show();
            }
        });
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
                Bundle bundle = data.getExtras();
                final Bitmap bitmap = (Bitmap) bundle.get("data");
                petImage.setImageBitmap(bitmap);
            }
            else if(requestCode == SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                Picasso.with(getContext()).load(selectedImageUri).into(petImage);
                petImage.setImageURI(selectedImageUri);
            }
        }
    }

    private void setInputs() {
        petImage = getView().findViewById(R.id.pet_image_register);
        petNameInput = getView().findViewById(R.id.pet_name_input_register);
        typePetSpinner = getView().findViewById(R.id.register_spinner_pet_type);
        sexPetSpinner = getView().findViewById(R.id.register_spinner_pet_sex);
        neuteredPetSpinner = getView().findViewById(R.id.register_spinner_pet_neutered);
        collarPetSpinner = getView().findViewById(R.id.register_spinner_pet_collar);
        yearsPetInput = getView().findViewById(R.id.pet_years_input_register);
        montsPetInput = getView().findViewById(R.id.pet_months_input_register);
        descriptionPetInput = getView().findViewById(R.id.pet_description_input_register);

        blackColor = getView().findViewById(R.id.register_black_color);
        brownColor = getView().findViewById(R.id.register_brown_color);
        whiteColor = getView().findViewById(R.id.register_white_color);
        goldenColor = getView().findViewById(R.id.register_golden_color);
        greyColor = getView().findViewById(R.id.register_grey_color);
        tanColor = getView().findViewById(R.id.register_tan_color);

        registerPet = getView().findViewById(R.id.register_post_pet_button);
        cancel = getView().findViewById(R.id.cancel_register_pet_button);
    }

    private void galleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
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
