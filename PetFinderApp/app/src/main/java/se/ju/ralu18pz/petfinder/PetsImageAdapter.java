package se.ju.ralu18pz.petfinder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PetsImageAdapter extends RecyclerView.Adapter<PetsImageAdapter.ImageViewHolder> {

    private Context context;
    private List<Pet> pets;
    private int action;

    public PetsImageAdapter(Context context, List<Pet> pets, int action) {
        this.context = context;
        this.pets = pets;
        this.action = action;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.pet_item, viewGroup, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder imageViewHolder, int i) {
        final Pet currentPet = pets.get(i);
        imageViewHolder.petName.setText(currentPet.name);
        imageViewHolder.petDescription.setText(currentPet.description);
        Picasso.with(context)
                .load(currentPet.petImageURL)
                .fit()
                .centerCrop()
                .into(imageViewHolder.petImage);

        imageViewHolder.petLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(action == 0) {
                    PetInfoFragment petInfoFragment = new PetInfoFragment();
                    petInfoFragment.selectedPet = currentPet;


                    ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, petInfoFragment)
                            .addToBackStack(null)
                            .commit();
                }
                else if(action == 1) {
                    LostPetPostFragment lostPetPostFragment = new LostPetPostFragment();
                    lostPetPostFragment.lostPetSelected = currentPet;

                    ((FragmentActivity) v.getContext()).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, lostPetPostFragment)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return pets.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public TextView petName, petDescription;
        public ImageView petImage;
        public LinearLayout petLinearLayout;

        public ImageViewHolder(View itemView) {
            super(itemView);

            petName = itemView.findViewById(R.id.pet_name_list);
            petDescription = itemView.findViewById(R.id.pet_description_list);
            petImage = itemView.findViewById(R.id.pet_image_list);
            petLinearLayout = itemView.findViewById(R.id.pet_card_info);

        }
    }

}
