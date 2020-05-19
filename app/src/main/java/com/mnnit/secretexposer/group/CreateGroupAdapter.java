package com.mnnit.secretexposer.group;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.annotations.NotNull;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;

import java.util.ArrayList;
import java.util.HashMap;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.UserListHolder> {
    private ArrayList<User> data ;
    private HashMap<String,String> selectedUser ;
    private Context context;
    public CreateGroupAdapter(Context context,ArrayList<User> data){
        this.context = context;
        this.data = data;
        selectedUser = new HashMap<String,String>();
    }
    public void addAll(ArrayList<User> moreUser){
        int count=data.size();
        data.addAll(moreUser);
        notifyItemRangeInserted(count,moreUser.size());
    }
    public void clearAll(){
        data.clear();
        notifyDataSetChanged();
    }
    public void setSelectedUser(HashMap<String,String> users){
        selectedUser.putAll(users);
    }
    public HashMap<String, String> getSelectedUser(){
        return selectedUser;
    }
    @NonNull
    @Override
    public UserListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.user_list_container,parent,false);
        return new UserListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserListHolder holder, int position) {
        User user = data.get(position);
        holder.userName.setText(user.getFullname());
        holder.userDetail.setText(user.getEmail());
        if(selectedUser.containsKey(user.getKey()))
            holder.selected.setVisibility(View.VISIBLE);
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedUser.containsKey(user.getKey())){
                    selectedUser.remove(user.getKey());
                    holder.selected.setVisibility(View.GONE);
                }
                else{
                    if(user.getKey()==null)
                        return;
                    selectedUser.put(user.getKey(),"true");
                    holder.selected.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ArrayList< User> getUserList(){
        return data;
    }


    public class UserListHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        ImageView profileImage;
        TextView userName;
        TextView userDetail;
        ImageView selected;

        public UserListHolder(@NotNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.user_detail_container);
            profileImage = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_name);
            userDetail = itemView.findViewById(R.id.user_detail);
            selected = itemView.findViewById(R.id.select_user);
        }
    }
}
