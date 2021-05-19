package com.example.gymguru;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gymguru.databinding.FragmentHomeBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding bind;
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference databaseReference, likesreference, dislikesreference, followsreference;
    Boolean likeChecker = false;
    Boolean dislikeChecker = false;
    Boolean followChecker = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind = FragmentHomeBinding.bind(view);

        bind.recyclerviewVideo.setHasFixedSize(true);
        bind.recyclerviewVideo.setLayoutManager(new LinearLayoutManager(getActivity()));
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("videos");
        likesreference = database.getReference("video_likes");
        dislikesreference = database.getReference("video_dislikes");
        followsreference = database.getReference("Followers");
    }


    //This is for search text
    /*private void firebaseSearch(String searchtext){
        String query = searchtext.toLowerCase();
        Query firebaseQuery = database.getReference().orderByChild("search").startAt(query).endAt(query + "\uf8ff");
    }*/


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<UploadMember, VideoHolder /*RegistrationModel*/> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<UploadMember, VideoHolder /*RegistrationModel*/>(
                        UploadMember.class,
                        //RegistrationModel.class,
                        R.layout.row,
                        VideoHolder.class,
                        reference

                ) {


                    @Override
                    protected void populateViewHolder(VideoHolder videoHolder, UploadMember model /*RegistrationModel registrationModel*/, int position) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String currentUserId = user.getUid();
                        final String postkey = getRef(position).getKey(); //It will currently get the key of the post


                        Log.d("Message", model.getUrl());

                        videoHolder.setVideo(requireActivity().getApplication(), model, /*registrationModel,*/ position);
                        String videoId = this.getRef(position).getKey();
                        videoHolder.initui(getActivity(), model, /*registrationModel,*/ position, videoId,getChildFragmentManager());

                        videoHolder.setLikesButtonStatus(postkey);
                        videoHolder.likeButton.setOnClickListener(v -> {
                            likeChecker = true;
                            likesreference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(likeChecker.equals(true)){
                                        if(snapshot.child(postkey).hasChild(currentUserId)){
                                            likesreference.child(postkey).child(currentUserId).removeValue(); //this is for that user could not like the same video again and again
                                            likeChecker = false;
                                        }else{
                                            likesreference.child(postkey).child(currentUserId).setValue(true);
                                            likeChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull  DatabaseError error) {
                                    Snackbar.make(bind.getRoot(),"Something went wrong", BaseTransientBottomBar.LENGTH_LONG).show();
                                }
                            });
                        });
                        videoHolder.setDislikeButtonStatus(postkey);
                        videoHolder.dislikeButton.setOnClickListener(v -> {
                            dislikeChecker = true;
                            dislikesreference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(dislikeChecker.equals(true)){
                                        if(snapshot.child(postkey).hasChild(currentUserId)){
                                            dislikesreference.child(postkey).child(currentUserId).removeValue(); //User already gives the dislike so they could not again
                                            dislikeChecker = false;
                                        }else{
                                            dislikesreference.child(postkey).child(currentUserId).setValue(true); //User can give dislike only once
                                            dislikeChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Snackbar.make(bind.getRoot(),"Something went wrong",BaseTransientBottomBar.LENGTH_LONG).show();
                                }
                            });
                        });
                        videoHolder.setFollowButtonStatus(postkey);
                        videoHolder.followButton.setOnClickListener(v -> {
                            followChecker = true;
                            followsreference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(followChecker.equals(true)){
                                        if(snapshot.child(postkey).hasChild(currentUserId)){
                                            followsreference.child(postkey).child(currentUserId).removeValue(); //User already follows the trainer so they could not again
                                            followChecker = false;
                                        }else{
                                            followsreference.child(postkey).child(currentUserId).setValue(true); //user follows a particular trainer only at once
                                            followChecker = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Snackbar.make(bind.getRoot(),"Something went wrong",BaseTransientBottomBar.LENGTH_LONG).show();
                                }
                            });
                        });
                    }

                };

        bind.recyclerviewVideo.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull  Menu menu, @NonNull  MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.search_firebase);

        SearchView searchView = (SearchView)item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                processSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                processSearch(newText);
                return false;
            }
        });


    }

    private void processSearch(String query) {
        //Code does not run due to the missing of FirebaseRecyclerOptions
        
    }



}