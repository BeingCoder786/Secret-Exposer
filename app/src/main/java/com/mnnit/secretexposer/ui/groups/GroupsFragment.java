package com.mnnit.secretexposer.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.group.CreateGroup;
import com.mnnit.secretexposer.group.Group;

import java.util.ArrayList;

public class GroupsFragment extends Fragment {

    private GroupsViewModel toolsViewModel;
    private boolean isMaxGroup;
    private String currLastGroup="";
    private String lastGroup="";
    private int MAX_Group_COUNT=20;
    private boolean isLoading;
    private GroupsAdapter groupsAdapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String currUid;
    private int totalGroup;
    private int lastVisibleGroup;
    String lGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(GroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        currUid = FirebaseAuth.getInstance().getUid();
        recyclerView=root.findViewById(R.id.group_recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        swipeRefreshLayout = root.findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(swipeRefreshLayout.isRefreshing()==false)
                    swipeRefreshLayout.setRefreshing(true);
                totalGroup=0;
                lastVisibleGroup=0;
                currLastGroup="";
                lastGroup="";
                getLastGroupName();
                groupsAdapter.clearAll();
                getGroups();
                if(swipeRefreshLayout.isRefreshing()==true)
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
        TextView textView=root.findViewById(R.id.create_group_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),CreateGroup.class);
                startActivity(intent);

            }
        });
        getLastGroupName();
        groupsAdapter = new GroupsAdapter(getContext(),new ArrayList<Group>());
        getGroups();
        recyclerView.setAdapter(groupsAdapter);
        return root;
    }
    private void getLastGroupName(){
        Query query = FirebaseDatabase.getInstance().getReference("Users/"+currUid)
                .child("JoinedGroup")
                .orderByKey()
                .limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot groupSnapshot : dataSnapshot.getChildren())
                lastGroup = groupSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getGroups(){
        if(!isMaxGroup){
            Query query;
            Query getLastGroup;
            if(TextUtils.isEmpty(lastGroup)){
                query = FirebaseDatabase.getInstance().getReference("Users/"+currUid)
                        .child("JoinedGroup")
                        .orderByKey()
                        .limitToFirst(MAX_Group_COUNT);
                getLastGroup = FirebaseDatabase.getInstance().getReference("Users/"+currUid+"/JoinedGroup")
                        .orderByKey()
                        .limitToFirst(MAX_Group_COUNT);

            } else {
                query = FirebaseDatabase.getInstance().getReference("Users/"+currUid)
                        .child("JoinedGroup")
                        .orderByKey()
                        .startAt(currLastGroup)
                        .limitToFirst(MAX_Group_COUNT);
                getLastGroup = FirebaseDatabase.getInstance().getReference("Users/"+currUid+"/JoinedGroup")
                        .orderByKey()
                        .startAt(currLastGroup)
                        .limitToFirst(MAX_Group_COUNT);

            }
            getLastGroup.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                    for(DataSnapshot ds :dataSnapshot.getChildren()){
                        lGroup=ds.getKey();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError){
                }
            });
            query.addValueEventListener(new ValueEventListener(){
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        ArrayList<Group> groups = new ArrayList<Group>();

                        DatabaseReference getGroup = FirebaseDatabase.getInstance().getReference("Groups");
                        for( DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String groupName = userSnapshot.getKey();
                            getGroup.child(groupName).addListenerForSingleValueEvent(new ValueEventListener(){
                                @Override
                                public void onDataChange(@NonNull DataSnapshot groupSnapshot){
                                    Group group = groupSnapshot.getValue(Group.class);
                                    groups.add(group);
                                    if(groupName.equals(lGroup)){
                                        if(groups.size()>0)
                                            currLastGroup = groups.get(groups.size()-1).getGroupName();
                                        if((!currLastGroup.equals(lastGroup))&&groups.size()>0)
                                            groups.remove(groups.size()-1);
                                        else
                                            currLastGroup = "end";
                                        groupsAdapter.addAll(groups);
                                        isLoading = false;
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError){

                                }
                            });
                        }
                       // if(groups.size()>0)
                       // Toast.makeText(getContext(), "hello    "+groups.size(), Toast.LENGTH_SHORT).show();

                    } else {
                        isLoading = false;
                        isMaxGroup = true;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isLoading = false;
                }
            });
        }
    }

}
