package com.mnnit.secretexposer.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ShowMemberAdapter extends RecyclerView.Adapter<ShowMemberAdapter.MemberHolder>{
    private ArrayList< User > users;
    private Context context;
    public ShowMemberAdapter( Context context, ArrayList<User> users ){
        this.users = users;
        this.context = context;
    }
    @NonNull
    @Override
    public MemberHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.user_list_container,null);
        return new MemberHolder(view);
    }
    public void addAll(ArrayList<User> newUsers){
        int count=users.size();
        users.addAll(newUsers);
        notifyItemRangeInserted(count,newUsers.size());
    }
    public void clearAll(){
        users.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder( @NonNull MemberHolder holder, int position ){
        User user=users.get(position);
        Toast.makeText(context,user.getFullname(),Toast.LENGTH_SHORT).show();
        holder.userName.setText(user.getFullname());
        holder.userDetail.setText(user.getEmail());
        if(!user.getProfileImageUrl().isEmpty())
        Picasso.with(context)
                .load(user.getProfileImageUrl())
                .into(holder.profileImage);
    }
    @Override
    public int getItemCount(){
        return users.size();
    }

    public class MemberHolder extends RecyclerView.ViewHolder{
        TextView userName;
        ImageView profileImage;
        TextView userDetail;
        public MemberHolder( @NonNull View itemView ){
            super(itemView);
            userName = itemView.findViewById(R.id.user_name);
            userDetail = itemView.findViewById(R.id.user_detail);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
