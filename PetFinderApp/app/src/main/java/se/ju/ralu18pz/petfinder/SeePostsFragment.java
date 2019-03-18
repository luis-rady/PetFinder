package se.ju.ralu18pz.petfinder;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SeePostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    private List<LostPost> lostPosts;
    private List<FoundPost> foundPosts;

    private FirebaseFirestore db;

    private int type;
    private TextView header;

    public SeePostsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_see_posts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setInputs();

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            type = bundle.getInt("type");
        }

        if(type == 0) {
            header.setText("Your lost pets");
            lostPosts = new ArrayList<>();

            db.collection(MainActivity.LOST_COLLECTION)
                    .whereEqualTo("userId", MainActivity.currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                LostPost newLostPost = documentSnapshot.toObject(LostPost.class);
                                lostPosts.add(newLostPost);
                            }

                            postAdapter = new PostAdapter(getActivity(), lostPosts, 0);
                            recyclerView.setAdapter(postAdapter);
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
            header.setText("Your found pets");
            foundPosts = new ArrayList<>();

            db.collection(MainActivity.FOUND_COLLECTION)
                    .whereEqualTo("userId", MainActivity.currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                FoundPost newFoundPost = documentSnapshot.toObject(FoundPost.class);
                                foundPosts.add(newFoundPost);
                            }

                            postAdapter = new PostAdapter(foundPosts, getActivity(), 1);
                            recyclerView.setAdapter(postAdapter);
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

    private void setInputs() {
        MainActivity.currentUser = MainActivity.auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerView = getView().findViewById(R.id.posts_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        header = getView().findViewById(R.id.your_post_header);

    }
}
