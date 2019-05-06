package se.ju.ralu18pz.petfinder;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder> {

    private Context context;
    private List<LostPost> lostPosts;
    private List<FoundPost> foundPosts;
    private int type;
    private FirebaseFirestore db;

    public PostAdapter(Context context, List<LostPost> lostPosts, int type) {
        this.context = context;
        this.lostPosts = lostPosts;
        this.type = type;
    }

    public PostAdapter(List<FoundPost> foundPosts, Context context, int type) {
        this.context = context;
        this.foundPosts = foundPosts;
        this.type = type;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.post_item, viewGroup, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder imageViewHolder, int i) {
        db = FirebaseFirestore.getInstance();
        if(type == 0) {
            final LostPost lostPost = lostPosts.get(i);
            db.collection(MainActivity.PET_CLASS)
                    .document(lostPost.petId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Pet pet = documentSnapshot.toObject(Pet.class);

                            imageViewHolder.title.setText(pet.name);
                            imageViewHolder.date.setText(lostPost.date);

                            Picasso.with(context)
                                    .load(Uri.parse(pet.petImageURL))
                                    .fit()
                                    .centerCrop()
                                    .into(imageViewHolder.petImage);
                        }
                    });


            imageViewHolder.deletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    db.collection(MainActivity.LOST_COLLECTION)
                            .whereEqualTo("petId", lostPost.petId)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        db.collection(MainActivity.LOST_COLLECTION)
                                                .document(documentSnapshot.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        db.collection(MainActivity.PET_CLASS)
                                                                .document(lostPost.petId)
                                                                .update(
                                                                        "lost", false
                                                                )
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Toast.makeText(context, R.string.post_deleted_successfull, Toast.LENGTH_LONG).show();
                                                                        ((FragmentActivity) v.getContext()).getSupportFragmentManager().popBackStack();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                }
                            });
                }
            });
        }
        else {
            final FoundPost foundPost = foundPosts.get(i);
            imageViewHolder.title.setText(foundPost.petType);
            imageViewHolder.date.setText(foundPost.date);
            Picasso.with(context)
                    .load(Uri.parse(foundPost.postImage))
                    .fit()
                    .centerCrop()
                    .into(imageViewHolder.petImage);


            imageViewHolder.deletePost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    String url = foundPost.postImage;
                    final StorageReference pictureRef = FirebaseStorage.getInstance().getReferenceFromUrl(foundPost.postImage);
                    pictureRef.delete();

                    db.collection(MainActivity.FOUND_COLLECTION)
                            .whereEqualTo("postImage", url)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                        db.collection(MainActivity.FOUND_COLLECTION)
                                                .document(documentSnapshot.getId())
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(context, R.string.post_deleted_successfull, Toast.LENGTH_LONG).show();
                                                        ((FragmentActivity) v.getContext()).getSupportFragmentManager().popBackStack();
                                                    }
                                                });
                                    }
                                }
                            });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if(type == 0) {
            return lostPosts.size();
        }
        else {
            return foundPosts.size();
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView date;
        public ImageView petImage;
        public Button deletePost;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.post_name_list);
            date = itemView.findViewById(R.id.post_date_list);
            petImage = itemView.findViewById(R.id.post_image_list);
            deletePost = itemView.findViewById(R.id.delete_post_button);


        }
    }
}
