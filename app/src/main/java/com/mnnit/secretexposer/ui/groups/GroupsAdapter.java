package com.mnnit.secretexposer.ui.groups;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.group.Group;
import com.mnnit.secretexposer.group.ShowGroupContent;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupContentHolder> {
    private Context context;
    @NonNull
    private ArrayList<Group> groups;
    private User groupOwner;

    public GroupsAdapter(Context context, ArrayList<Group> groups) {
        this.groups = groups;
        this.context = context;
    }
    @Override
    public GroupContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.group_list_container,parent,false);
        return new GroupContentHolder(view);
    }
    public void addAll(ArrayList<Group> groupArrayList){
        int groupCount = groups.size();
        groups.addAll(groupArrayList);
        notifyItemRangeInserted(groupCount,groupArrayList.size());
    }
    public void clearAll(){
        groups.clear();
        notifyDataSetChanged();
    }
    @Override
    public void onBindViewHolder(@NonNull GroupContentHolder holder, int position) {
        Group group = groups.get(position);
        if(group.getGroupImageUri()!=null)
        Picasso.with(context)
                .load(group.getGroupImageUri())
                .into(holder.groupImage);
        holder.groupName.setText(group.getGroupName());
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .child(group.getGroupOwner());
        if(group.getGroupOwner()!=null)
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupOwner = dataSnapshot.getValue(User.class);
                if(groupOwner!=null)
                holder.groupDetail.setText(groupOwner.getFullname()+" "+group.getTime());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShowGroupContent.class);
                intent.putExtra("group",group);
                intent.putExtra("groupOwner",groupOwner);
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return groups.size();
    }

    public class GroupContentHolder extends RecyclerView.ViewHolder{
        ImageView groupImage;
        TextView groupName;
        TextView groupDetail;
        ConstraintLayout constraintLayout;
        public GroupContentHolder(@NonNull View itemView) {
            super(itemView);
            groupImage=(ImageView)itemView.findViewById(R.id.group_image);
            groupName=(TextView)itemView.findViewById(R.id.group_name);
            groupDetail=(TextView)itemView.findViewById(R.id.group_detail);
            constraintLayout = itemView.findViewById(R.id.container);
        }
    }
}
