package com.mnnit.secretexposer.group;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.loginSignup.User;
import com.mnnit.secretexposer.post.Post;
import com.mnnit.secretexposer.post.WritePostActivity;
import com.mnnit.secretexposer.ui.home.HomeAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ShowGroupContent extends AppCompatActivity {
    private Group group;
    private User groupOwner;
    private String currUid;
    private ImageView groupIcon;
    private TextView groupNameView;
    private TextView groupDetailView;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int totalPost;
    private int lastVisiblePost;
    private String lastPost;
    private String lastPostId;
    private String groupName;
    private HomeAdapter postAdapter;
    private LinearLayoutManager layoutManager;
    private boolean isLoading;
    private int MAX_POST_COUNT=20;
    private boolean isMaxPost;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group_content);
        group = (Group) getIntent().getExtras().get("group");
        groupName = group.getGroupName();
        groupOwner = (User) getIntent().getExtras().get("groupOwner");
        groupIcon = findViewById(R.id.group_image);
        groupNameView = findViewById(R.id.group_name);
        groupDetailView = findViewById(R.id.group_detail);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(swipeRefreshLayout.isRefreshing()==false)
                    swipeRefreshLayout.setRefreshing(true);
                totalPost=0;
                lastVisiblePost=0;
                lastPost="";
                lastPostId="";
                getLastKeyFromFirebase();
                postAdapter.clearAll();
                getPost();
                if(swipeRefreshLayout.isRefreshing()==true)
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
        recyclerView = findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalPost = layoutManager.getItemCount();
                lastVisiblePost = layoutManager.findLastCompletelyVisibleItemPosition();
                if(!isLoading && totalPost <= lastVisiblePost+MAX_POST_COUNT){
                    getPost();
                    isLoading = true;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        setGroup();
        postAdapter = new HomeAdapter(this,new ArrayList<Post>());
        getLastKeyFromFirebase();
        getPost();
        recyclerView.setAdapter(postAdapter);
    }

    public void setGroup(){
        Picasso.with(ShowGroupContent.this)
                .load(group.getGroupImageUri())
                .into(groupIcon);
        groupNameView.setText(group.getGroupName());
        groupDetailView.setText(groupOwner.getFullname()+"  "+group.getTime());
    }
    private void getLastKeyFromFirebase() {
        Query getLastKey = FirebaseDatabase.getInstance().getReference("Posts/")
                .child(groupName)
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
            if(TextUtils.isEmpty(lastPost)){
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByKey()
                        .limitToFirst(MAX_POST_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference("Posts")
                        .child(groupName)
                        .orderByKey()
                        .startAt(lastPost)
                        .limitToFirst(MAX_POST_COUNT);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        ArrayList<Post> posts = new ArrayList<>();
                        for( DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Post post=postSnapshot.getValue(Post.class);
                            Query nameQuery=FirebaseDatabase.getInstance().getReference("Users")
                                    .child(post.getOwner());
                            nameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot!=null) {
                                        User user = dataSnapshot.getValue(User.class);
                                        post.setOwner(user.getFullname());

                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            posts.add(post);
                        }
                        if(posts.size()>0)
                            lastPost = posts.get(posts.size()-1).getId();
                        if(!lastPost.equals(lastPostId))
                            posts.remove(posts.size()-1);
                        else
                            lastPost = "end";
                        postAdapter.addAll(posts);
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
    public void writePost(MenuItem item) {
        Intent intent = new Intent(getBaseContext(), WritePostActivity.class);
        intent.putExtra("GroupName",groupName);
        intent.putExtra("group",group);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        currUid = FirebaseAuth.getInstance().getUid();
        if(currUid.equals(group.getGroupOwner()))
            getMenuInflater().inflate(R.menu.group_owner_menu,menu);
        else
        getMenuInflater().inflate(R.menu.group_menu,menu);
        return true;
    }

    public void updateGroup(MenuItem item){
        Intent intent=new Intent(ShowGroupContent.this,UpdateGroup.class);
        intent.putExtra("group",group);
        intent.putExtra("groupOwner",groupOwner);
        startActivity(intent);
    }

    public void showMember(MenuItem item){
        Intent intent=new Intent(ShowGroupContent.this,ShowGroupMember.class);
        intent.putExtra("groupOwner",groupOwner);
        intent.putExtra("group",group);
        startActivity(intent);
        startActivity(intent);
    }
    public void deleteGroup(MenuItem item){
        FirebaseDatabase.getInstance().getReference("Groups")
                .child(groupName).removeValue();;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        HashMap<String,String> members=group.getMembers();
        for(Map.Entry<String,String> entry:members.entrySet()){
            databaseReference.child(entry.getKey()+"/JoinedGroup").child(groupName).removeValue();
        }
        databaseReference.child(group.getGroupOwner()+"/OwnerOfGroup").child(groupName).removeValue();
        FirebaseDatabase.getInstance().getReference("Posts")
                .child(groupName)
                .removeValue();
        FirebaseStorage.getInstance().getReference("posts")
                .child(groupName).delete();
        FirebaseStorage.getInstance().getReference("GroupIcons")
                .child(groupName)
                .delete();
        startActivity(new Intent(ShowGroupContent.this, HomeActivity.class));
    }
}
