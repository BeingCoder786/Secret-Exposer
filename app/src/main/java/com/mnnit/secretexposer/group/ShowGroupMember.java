package com.mnnit.secretexposer.group;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShowGroupMember extends AppCompatActivity{
    private Group group;
    private ImageView groupIcon;
    private TextView groupName;
    private TextView groupDetail;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ArrayList < User > users;
    private boolean isLoading = false;
    private boolean isMaxUser = false;
    private int lastUser = - 1;
    private String lastUserId = "";
    private final int MAX_USER = 20;
    private ShowMemberAdapter userAdapter;
    private ArrayList < String > members;

    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_group_member);
        group = (Group) getIntent().getExtras().get("group");
        groupIcon = findViewById(R.id.group_image);
        groupName = findViewById(R.id.group_name);
        groupDetail = findViewById(R.id.group_detail);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                isLoading = false;
                isMaxUser = false;
                lastUserId = "";
                lastUser = - 1;
                getLastUserId();
                userAdapter.clearAll();
                getUser();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        setGroup();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(layoutManager);
        userAdapter = new ShowMemberAdapter(getBaseContext(), new ArrayList <>());
        recyclerView.setAdapter(userAdapter);
        members = new ArrayList < String >(group.getMembers().keySet());
        users = new ArrayList <>();
        getLastUserId();
        getUser();
    }

    private void setGroup(){
        if(group == null) return;
        groupName.setText(group.getGroupName());
        groupDetail.setText(group.getTime());
        Picasso.with(getBaseContext())
                .load(group.getGroupImageUri())
                .into(groupIcon);
    }

    private void getLastUserId(){
        FirebaseDatabase.getInstance().getReference("Groups")
                .child("members")
                .orderByKey()
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren())
                            lastUserId = userSnapshot.getKey();
                    }

                    @Override
                    public void onCancelled( @NonNull DatabaseError databaseError ){

                    }
                });
    }

    private void getUser(){
            for (int i = lastUser + 1; i < members.size(); i++) {
                String id = members.get(i);
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(id)
                                .addListenerForSingleValueEvent(new ValueEventListener(){
                                    @Override
                                    public void onDataChange( @NonNull DataSnapshot userSnapshot ){
                                        User user = userSnapshot.getValue(User.class);
                                        users.add(user);
                                    }
                                    @Override
                                    public void onCancelled( @NonNull DatabaseError databaseError ){

                                    }
                                });
                    }
                });

            }
            userAdapter.addAll(users);
    }
}