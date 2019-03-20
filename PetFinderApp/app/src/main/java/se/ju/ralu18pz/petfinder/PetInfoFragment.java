package se.ju.ralu18pz.petfinder;


import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.squareup.picasso.Picasso;

import io.opencensus.internal.StringUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class PetInfoFragment extends Fragment {
    public static Pet selectedPet;

    private TextView petName;
    private TextView petType;
    private TextView petSex;
    private TextView petDescription;
    private TextView petAge;
    private TextView petNeutered;
    private TextView petCollar;
    private TextView petColors;
    private TextView statusText;
    private ImageView petImage;
    private ProgressBar progressBar;

    private Button editInfo;
    private Button deletePet;
    private EditPetFragment editPetFragment;
    private PetsFragment petsFragment;

    private FirebaseFirestore db;
    private Uri selectedImageUri;

    public PetInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pet_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        petName = getView().findViewById(R.id.pet_name_info);
        petType = getView().findViewById(R.id.pet_type_info);
        petSex = getView().findViewById(R.id.pet_sex_info);
        petDescription = getView().findViewById(R.id.pet_description_info);
        petAge = getView().findViewById(R.id.pet_age_info);
        petNeutered = getView().findViewById(R.id.pet_neutered_info);
        petCollar = getView().findViewById(R.id.pet_collar_info);
        petColors = getView().findViewById(R.id.pet_colors_info);
        petImage = getView().findViewById(R.id.pet_image_info);
        editInfo = getView().findViewById(R.id.edit_info_pet_button);
        deletePet = getView().findViewById(R.id.delete_pet_button);
        statusText = getView().findViewById(R.id.pet_status_info);
        progressBar = getView().findViewById(R.id.pet_info_progressbar);


        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPetFragment = new EditPetFragment();
                editPetFragment.currentPet = selectedPet;
                setFragment(editPetFragment);
            }
        });

        deletePet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyItHasPosts();
            }
        });

        progressBar.setVisibility(View.GONE);
        petName.setText(selectedPet.name);

        selectedImageUri = Uri.parse(selectedPet.petImageURL);
        Picasso.with(getContext())
                .load(selectedPet.petImageURL)
                .fit()
                .centerCrop()
                .into(petImage);

        petType.setText(getString(R.string.type_label) +" "+ selectedPet.type);
        petSex.setText(getString(R.string.sex_label) +" "+ selectedPet.sex);
        petDescription.setText(getString(R.string.description_label) +" "+ selectedPet.description);
        petAge.setText(getString(R.string.years_label) + ": " + selectedPet.years + " - " + getString(R.string.months_label) + ": " + selectedPet.months);
        petNeutered.setText(getString(R.string.neutered_label) +" "+ selectedPet.neutered);
        petCollar.setText(getString(R.string.collar_label) +" "+ selectedPet.collar);

        String col = "";
        for (int i = 0; i < selectedPet.colors.size(); i++) {
            if(i == selectedPet.colors.size() -1) {
                col = col + selectedPet.colors.get(i);
            }
            else {
                col = col + selectedPet.colors.get(i) + " - ";
            }
        }

        petColors.setText(getString(R.string.color_label) +" "+ col);
        if(!selectedPet.lost) {
            statusText.setText(getString(R.string.pet_is_not_lost));
        }
        else {
            statusText.setText(getString(R.string.pet_is_lost));
        }
    }

    private void verifyItHasPosts() {
        db.collection(MainActivity.LOST_COLLECTION)
                .whereEqualTo("petId", selectedPet.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size() == 0) {
                            progressBar.setVisibility(View.VISIBLE);
                            deletePet();
                        }
                        else {
                            deletePetPosts();
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

    private void deletePet() {
        final StorageReference pictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(selectedPet.petImageURL);
        pictureRef.delete();

        db.collection(MainActivity.PET_CLASS)
                .document(selectedPet.id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), getString(R.string.pet_delete_message), Toast.LENGTH_LONG).show();
                        petsFragment = new PetsFragment();
                        setFragmentWithoutBackStack(petsFragment);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deletePetPosts() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection(MainActivity.LOST_COLLECTION)
                .whereEqualTo("petId", selectedPet.id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        LostPost lostPost = new LostPost();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            db.collection(MainActivity.LOST_COLLECTION)
                                    .document(doc.getId())
                                    .delete();
                        }

                        deletePet();
                    }
                });
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setFragmentWithoutBackStack(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }
}
