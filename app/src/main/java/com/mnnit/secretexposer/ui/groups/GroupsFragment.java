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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    private int totalGroup;
    private int lastVisibleGroup;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(GroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_groups, container, false);
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
        Query query = FirebaseDatabase.getInstance().getReference()
                .child("Groups")
                .orderByKey()
                .limitToLast(1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastGroup = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void getGroups(){
        if(!isMaxGroup){
            Query query;
            if(TextUtils.isEmpty(lastGroup)){
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Groups")
                        .orderByKey()
                        .limitToFirst(MAX_Group_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Groups")
                        .orderByKey()
                        .startAt(currLastGroup)
                        .limitToFirst(MAX_Group_COUNT);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        ArrayList<Group> groups = new ArrayList<Group>();
                        for( DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            Group group = userSnapshot.getValue(Group.class);
                            groups.add(group);
                        }
                        if(groups.size()>0)
                            currLastGroup = groups.get(groups.size()-1).getGroupName();
                        if(!currLastGroup.equals(lastGroup))
                            groups.remove(groups.size()-1);
                        else
                            currLastGroup = "end";
                        groupsAdapter.addAll(groups);
                        isLoading = false;
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
