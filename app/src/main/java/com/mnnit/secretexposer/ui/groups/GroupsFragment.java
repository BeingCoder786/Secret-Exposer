package com.mnnit.secretexposer.ui.groups;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mnnit.secretexposer.CreateGroup;
import com.mnnit.secretexposer.R;

public class GroupsFragment extends Fragment {

    private GroupsViewModel toolsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        toolsViewModel =
                ViewModelProviders.of(this).get(GroupsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_groups, container, false);
        RecyclerView recyclerView=root.findViewById(R.id.group_recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        TextView textView=root.findViewById(R.id.create_group_button);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),CreateGroup.class);
                startActivity(intent);

            }
        });
        return root;
    }
}