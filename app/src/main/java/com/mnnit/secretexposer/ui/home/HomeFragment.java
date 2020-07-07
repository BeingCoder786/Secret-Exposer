package com.mnnit.secretexposer.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;
import com.mnnit.secretexposer.post.Post;
import com.mnnit.secretexposer.post.WritePostActivity;

import java.util.ArrayList;

public class HomeFragment extends Fragment{
    private HomeViewModel homeViewModel;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textView;
    final int MAX_POST_COUNT = 10;
    private HomeAdapter postAdapter;
    private boolean isLoading = false;
    private boolean isMaxPost = false;
    private String uid;
    private User user;
    private String lastPost;
    private String lastPostId;
    private int totalPost = 0, lastVisiblePost = 0;

    public View onCreateView( @NonNull LayoutInflater inflater,
                              ViewGroup container, Bundle savedInstanceState ){
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        textView = (TextView) root.findViewById(R.id.write_post);
        textView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick( View v ){
                Intent intent = new Intent(getContext(), WritePostActivity.class);
                intent.putExtra("GroupName", "publicGroup");
                intent.putExtra("user", user);
                startActivity(intent);
            }
        });
        LinearLayout layout = (LinearLayout) root.findViewById(R.id.container);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                if(swipeRefreshLayout.isRefreshing() == false)
                    swipeRefreshLayout.setRefreshing(true);
                totalPost = 0;
                lastVisiblePost = 0;
                lastPost = "";
                lastPostId = "";
                isMaxPost=false;
                postAdapter.clearAll();
                getLastKeyFromFirebase("publicGroup");
                if(lastPostId==null){
                    startActivity(new Intent(getContext(),WritePostActivity.class));
                }
                if(swipeRefreshLayout.isRefreshing() == true)
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
        recyclerView = (RecyclerView) root.findViewById(R.id.home_recycler);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView
                                                                                        .getContext(), DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled( @NonNull RecyclerView recyclerView, int dx, int dy ){
                super.onScrolled(recyclerView, dx, dy);
                totalPost = layoutManager.getItemCount();
                lastVisiblePost = layoutManager.findLastCompletelyVisibleItemPosition();
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        getPost("publicGroup");
                    }
                }).start();

            }
        });
        recyclerView.setHasFixedSize(false);
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                        user = dataSnapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError databaseError ){

                    }
                });
        postAdapter = new HomeAdapter(getContext(), new ArrayList <Post>());
        getLastKeyFromFirebase("publicGroup");
        getPost("publicGroup");
        recyclerView.setAdapter(postAdapter);
        return root;
    }
    private void getLastKeyFromFirebase( String groupName ){
        Query getLastKey = FirebaseDatabase.getInstance().getReference("Posts/")
                .child(groupName)
                .orderByChild("counter")
                .limitToLast(1);
        getLastKey.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                for (DataSnapshot lastKey : dataSnapshot.getChildren()) {
                    lastPostId = lastKey.getKey();
                }
            }
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ){

            }
        });
    }
    private void getPost( String groupName ){
        if(! isMaxPost){
            Query query;
            if(TextUtils.isEmpty(lastPost)){
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByChild("counter")
                        .limitToLast(MAX_POST_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByChild("counter")
                        .startAt(lastPost)
                        .limitToLast(MAX_POST_COUNT);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener(){
                @Override
                public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                    if(dataSnapshot.hasChildren()){
                        ArrayList <Post> posts = new ArrayList <Post>();
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post = postSnapshot.getValue(Post.class);
                            posts.add(post);
                        }
                        if(posts.size() > 0)
                            lastPost = posts.get(posts.size()-1).getId();
                        if(! lastPost.equals(lastPostId))
                            posts.remove(posts.size()-1);
                        else
                            lastPost = "end";
                        postAdapter.addAll(posts);
                        isLoading = false;
                    } else {
                        isMaxPost = true;
                        isLoading = false;
                    }
                }
                @Override
                public void onCancelled( @NonNull DatabaseError databaseError ){
                    isLoading = false;
                }
            });
        }
    }
}