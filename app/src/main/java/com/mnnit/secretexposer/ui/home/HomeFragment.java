package com.mnnit.secretexposer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.Post;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.User;
import com.mnnit.secretexposer.WritePostActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private ArrayList<Post> posts;
    private HomeViewModel homeViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textView;
    final int MAX_POST_COUNT=10;
    private HomeRecyclerViewAdapter postAdapter;
    private boolean isLoading=false;
    private boolean isMaxPost=false;
    private String lastPost;
    private String lastPostId;
    private String groupName="publicGroup";
    private int totalPost,lastVisiblePost;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView=(TextView)root.findViewById(R.id.write_post);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(), WritePostActivity.class);
                startActivity(intent);
            }
        });
        //getLastKeyFromFirebase();
        LinearLayout layout = (LinearLayout) root.findViewById(R.id.container);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        swipeRefreshLayout=(SwipeRefreshLayout)root.findViewById(R.id.swipe_refresh_container);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPost();
            }
        });

        recyclerView=(RecyclerView)root.findViewById(R.id.home_recycler);
        //DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL);
        //recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalPost = layoutManager.getItemCount();
                lastVisiblePost = layoutManager.findLastCompletelyVisibleItemPosition();
                if(!isLoading && totalPost <= lastVisiblePost+MAX_POST_COUNT){
                    loadPost();
                    isLoading = true;
                }
            }
        });
        recyclerView.setHasFixedSize(true);
        loadPost();
        postAdapter = new HomeRecyclerViewAdapter(getContext(),new ArrayList<Post>());
        recyclerView.setAdapter(postAdapter);
        return root;
    }

    private void getLastKeyFromFirebase() {
        Query getLastKey = FirebaseDatabase.getInstance().getReference("Posts/")
                .child("publicGroup")
                .orderByKey()
                .limitToLast(1);
        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot lastKey : dataSnapshot.getChildren())
                    lastPostId = lastKey.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getPost(){
        if(!isMaxPost){
            Query query;
            if(TextUtils.isEmpty(lastPostId)){
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByKey()
                        .limitToFirst(MAX_POST_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByKey()
                        .limitToFirst(MAX_POST_COUNT);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        ArrayList<Post> posts = new ArrayList<>();
                        for( DataSnapshot postSnapshot : dataSnapshot.getChildren())
                            posts.add(postSnapshot.getValue(Post.class));
                        lastPost = posts.get(posts.size()-1).getId();
                        if(!lastPost.equals(lastPostId))
                            posts.remove(posts.size()-1);
                        else
                            lastPost = "end";
                        //adapter.addAll(posts);
                        isLoading = false;
                    } else {
                        isLoading = false;
                        isMaxPost = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isLoading = false;
                }
            });
        }
    }
    public void loadPost(){
        swipeRefreshLayout.setRefreshing(true);
        String groupName="publicGroup";
        posts=new ArrayList<Post>();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Posts/"+groupName);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Post post=snapshot.getValue(Post.class);
                    DatabaseReference realRef= FirebaseDatabase.getInstance().getReference("Users/"+post.getOwner());
                    realRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User currUser=dataSnapshot.getValue(User.class);
                            post.setOwner(currUser.getFullname());
                            posts.add(post);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
                //postAdapter.setData(posts);
                postAdapter = new HomeRecyclerViewAdapter(getContext(),posts);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(postAdapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(),databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}