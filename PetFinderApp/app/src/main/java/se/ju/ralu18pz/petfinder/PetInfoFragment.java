package se.ju.ralu18pz.petfinder;


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
import android.widget.TextView;

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
    private ImageView petImage;

    private Button editInfo;
    private Button deletePet;
    private EditPetFragment editPetFragment;

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

        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPetFragment = new EditPetFragment();
                setFragment(editPetFragment);
            }
        });

        petName.setText(selectedPet.name);

        Picasso.with(getContext())
                .load(selectedPet.petImageURL)
                .fit()
                .centerCrop()
                .into(petImage);

        petType.setText("Type of pet: " + selectedPet.type);
        petSex.setText("Sex of the pet: " + selectedPet.sex);
        petDescription.setText(selectedPet.description);
        petAge.setText("Pet has " + selectedPet.years + " years and " + selectedPet.months + " months");
        petNeutered.setText("Pet is neutered: " + selectedPet.neutered);
        petCollar.setText("Pet has collar: " + selectedPet.collar);

        String col = "";
        for (int i = 0; i < selectedPet.colors.size(); i++) {
            if(i == selectedPet.colors.size() -1) {
                col = col + selectedPet.colors.get(i);
            }
            else {
                col = col + selectedPet.colors.get(i) + " - ";
            }
        }

        petColors.setText("Colors of the pet: " + col);
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
