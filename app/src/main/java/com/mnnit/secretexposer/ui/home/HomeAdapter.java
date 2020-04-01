package com.mnnit.secretexposer.ui.home;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.post.Like;
import com.mnnit.secretexposer.post.Post;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.post.ShowComment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ContentHolder> {
    @NonNull
    private  ArrayList<Post> data;
    Context context;
    public HomeAdapter(Context context , ArrayList<Post> data) {
        this.data = data;
        this.context = context;
    }
    public void addAll(ArrayList<Post> newPost){
        int count=data.size();
        data.addAll(newPost);
        notifyItemRangeInserted(count,newPost.size());
    }
    public void clearAll(){
        data.clear();
        notifyDataSetChanged();
    }
    @Override
    public ContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.post_container,parent,false);
        return new ContentHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ContentHolder holder, int position) {
        Post post=data.get(position);
        holder.groupName.setText(post.getGroupName()+"\n"+post.getOwner());
        holder.groupName.setVisibility(View.VISIBLE);
        holder.groupImageView.setVisibility(View.VISIBLE);
        holder.textPost.setText(post.getPostContent());
        holder.textPost.setVisibility(View.VISIBLE);
        holder.setIsRecyclable(false);
        holder.likeContainer.setVisibility(View.VISIBLE);
        String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("PostLikes/"+post.getId()+"/"+uid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){
                    holder.neutralLike.setVisibility(View.GONE);
                    holder.like.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(post.getPostType()==2) {
            Picasso.with(context).load(post.getUri())
                    .into(holder.imagePost);
            holder.imagePost.setVisibility(View.VISIBLE);
            holder.imagePost.setMaxHeight(300);
        }
        else if(post.getPostType()==4){
            MediaController mediaController=new MediaController(context);
            holder.textPost.setVisibility(View.VISIBLE);
            mediaController.setMediaPlayer(holder.videoPost);
            mediaController.setAnchorView(holder.videoPost);
            holder.videoPost.setMediaController(mediaController);
            Uri uri=Uri.parse(post.getUri());
            holder.videoPost.setVideoURI(uri);
            holder.videoPost.seekTo(1);
            holder.videoPost.setVisibility(View.VISIBLE);
        }
        holder.neutralLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                String postId=post.getId();
                String timeStamp=System.currentTimeMillis()+"";
                Like like=new Like(uid,postId,timeStamp);
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("PostLikes/");
                HashMap<String,Like> hashMap=new HashMap<String,Like>();
                hashMap.put(uid,new Like(uid,postId,System.currentTimeMillis()+""));
                databaseReference.child(postId).setValue(hashMap);
                holder.neutralLike.setVisibility(View.GONE);
                holder.like.setVisibility(View.VISIBLE);
            }
        });
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("PostLikes/");
                databaseReference.child(post.getId()).removeValue();
                holder.neutralLike.setVisibility(View.VISIBLE);
                holder.like.setVisibility(View.GONE);
            }
        });
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ShowComment.class);
                intent.putExtra("post", post.getId());
                context.startActivity(intent, new Bundle());
            }
        });
    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ContentHolder extends RecyclerView.ViewHolder{
        ImageView groupImageView;
        ImageView imagePost;
        ImageView neutralLike;
        ImageView like;
        ImageView comment;
        ImageView share;
        VideoView videoPost;
        TextView textPost;
        TextView groupName;
        MediaController mediaController;
        LinearLayout likeContainer;

        public ContentHolder(@NonNull View itemView) {
            super(itemView);
            mediaController=(MediaController)itemView.findViewById(R.id.media_actions);
            groupImageView=(ImageView)itemView.findViewById(R.id.group_image);
            groupName=(TextView)itemView.findViewById(R.id.group_name);
            imagePost=(ImageView)itemView.findViewById(R.id.image_post);
            videoPost=(VideoView)itemView.findViewById(R.id.video_post);
            textPost=(TextView)itemView.findViewById(R.id.text_post);
            likeContainer=(LinearLayout)itemView.findViewById(R.id.like_container);
            neutralLike=(ImageView)itemView.findViewById(R.id.neutral_like);
            like=(ImageView)itemView.findViewById(R.id.like);
            comment=(ImageView)itemView.findViewById(R.id.comment);
            share=(ImageView)itemView.findViewById(R.id.share);
        }
    }
}
