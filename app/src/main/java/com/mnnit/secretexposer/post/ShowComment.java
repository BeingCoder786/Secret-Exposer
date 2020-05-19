package com.mnnit.secretexposer.post;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class ShowComment extends AppCompatActivity{
    private ImageView profileImage;
    private ImageView imagePost;
    private ImageView neutralLike;
    private ImageView like;
    private ImageView comment;
    private ImageView share;
    private VideoView videoPost;
    private TextView textPost;
    private TextView groupName;
    private LinearLayout commentContainer;
    private TextView likeComment;
    private ProgressBar progressBar;
    private Context context;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);
        context = getBaseContext();
        post = (Post) getIntent().getExtras().get("post");
        profileImage = (ImageView) findViewById(R.id.profile_image);
        imagePost = (ImageView) findViewById(R.id.image_post);
        videoPost = (VideoView) findViewById(R.id.video_post);
        textPost = (TextView) findViewById(R.id.text_post);
        groupName = (TextView) findViewById(R.id.group_name);
        neutralLike = findViewById(R.id.neutral_like);
        like = findViewById(R.id.like);
        comment = findViewById(R.id.comment);
        share = findViewById(R.id.share);
        progressBar = findViewById(R.id.progress_bar);
        commentContainer = findViewById(R.id.comment_container);
        load();
        loadComment();
    }

    public void load(){
        getPostOwnerProfileImage(post, groupName, profileImage);
        groupName.setVisibility(View.VISIBLE);
        profileImage.setVisibility(View.VISIBLE);
        if(post.getPostContent() != null){
            textPost.setText(post.getPostContent());
            textPost.setVisibility(View.VISIBLE);
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference databaseReference = FirebaseDatabase
                .getInstance().getReference("PostLikes/" + post.getId() + "/" + uid);
        databaseReference.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                if(dataSnapshot.getValue() != null){
                    neutralLike.setVisibility(View.GONE);
                    like.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
        if(post.getPostType() == 2 && post.getUri() != null){
            Picasso.with(context).load(post.getUri())
                    .into(imagePost);
            imagePost.setVisibility(View.VISIBLE);
            imagePost.setMaxHeight(300);
        } else if(post.getPostType() == 4){
            MediaController mediaController = new MediaController(context);
            textPost.setVisibility(View.VISIBLE);
            mediaController.setMediaPlayer(videoPost);
            mediaController.setAnchorView(videoPost);
            videoPost.setMediaController(mediaController);
            Uri uri = Uri.parse(post.getUri());
            videoPost.setVideoURI(uri);
            videoPost.seekTo(1);
            videoPost.setVisibility(View.VISIBLE);
            imagePost.setVisibility(View.GONE);
        } else
            imagePost.setVisibility(View.GONE);

        neutralLike.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String postId = post.getId();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference("PostLikes/");
                HashMap< String, Like > hashMap = new HashMap< String, Like >();
                hashMap.put(uid, new Like(uid, postId, System.currentTimeMillis() + ""));
                databaseReference.child(postId).setValue(hashMap);
                neutralLike.setVisibility(View.GONE);
                like.setVisibility(View.VISIBLE);
            }
        });
        like.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference("PostLikes/");
                databaseReference.child(post.getId()).removeValue();
                neutralLike.setVisibility(View.VISIBLE);
                like.setVisibility(View.GONE);
            }
        });

    }

    private void getPostOwnerProfileImage(Post post, TextView textView, ImageView profileImage){
        if(post.isAnonymous()){
            textView.setText(post.getGroupName() + "\n" + "Anonymous");
            return;
        }
        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .child(post.getOwner());
        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                User owner = dataSnapshot.getValue(User.class);
                textView.setText(post.getGroupName() + "\n" + owner.getFullname());
                if(owner.getProfileImageUrl() != null)
                    Picasso.with(context)
                            .load(owner.getProfileImageUrl())
                            .into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
    }
    public void loadComment(){
        String path="Posts/"+post.getGroupName()+"/"+post.getId();
        Query query = FirebaseDatabase.getInstance().getReference(path)
                .child("Comments");
        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                for(DataSnapshot commentSnapshot : dataSnapshot.getChildren()){
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    View commentView = getLayoutInflater().inflate(R.layout.comment_container,null);
                    setComment(comment,commentView);
                    commentContainer.addView(commentView);
                }
                View newCommentView = getLayoutInflater().inflate(R.layout.new_comment,null);
                LinearLayout layout = newCommentView.findViewById(R.id.linear_layout);
                ImageView imageView = newCommentView.findViewById(R.id.profile_image);
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Query imageUrlQuery=FirebaseDatabase.getInstance().getReference("Users")
                        .child(uid);
                imageUrlQuery.addListenerForSingleValueEvent(new ValueEventListener(){
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                        User user = dataSnapshot.getValue(User.class);
                        Picasso.with(ShowComment.this)
                                .load(user.getProfileImageUrl())
                                .into(imageView);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError){

                    }
                });
                TextView textView = newCommentView.findViewById(R.id.comment);
                EditText newComment = newCommentView.findViewById(R.id.new_comment);
                textView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        newComment.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.GONE);
                    }
                });
                TextView saveButton = newCommentView.findViewById(R.id.save_comment);
                saveButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(newComment.getText()==null)
                            return;
                        progressBar.setVisibility(View.VISIBLE);
                        Date currTime = Calendar.getInstance().getTime();
                        String commentId=System.currentTimeMillis()+uid;
                        Comment cmt = new Comment(commentId,newComment.getText().toString(),uid,currTime.toString());
                        String path = "Posts/"+post.getGroupName()+"/"+post.getId()+"/"+"Comments";
                        FirebaseDatabase.getInstance().getReference(path)
                                .child(commentId).setValue(cmt).addOnSuccessListener(new OnSuccessListener< Void >(){
                            @Override
                            public void onSuccess(Void aVoid){
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(ShowComment.this, ShowComment.class));
                            }
                        });
                    }
                });
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layout.getLayoutParams();
                if(params!=null){
                    params.setMargins(130, 20, 0, 0);
                    layout.setLayoutParams(params);
                }
                commentContainer.addView(newCommentView);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });

    }
    public void setComment(Comment comment,View commentView){
        LinearLayout layout = commentView.findViewById(R.id.layout);
        TextView commentContent = commentView.findViewById(R.id.comment_content);
        commentContent.setText(comment.getCommentContent());
        TextView commenter = commentView.findViewById(R.id.commenter);
        ImageView profileImage = commentView.findViewById(R.id.profile_image);

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .child(comment.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot){
                User user = dataSnapshot.getValue(User.class);
                commenter.setText(user.getFullname());
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)layout.getLayoutParams();
                if(!user.getKey().equals(post.getOwner()))
                    params.setMargins(130, 20, 0, 0);
                else
                    params.setMargins(10, 20, 0, 0);
                layout.setLayoutParams(params);
                if(user.getProfileImageUrl() != null)
                    Picasso.with(context)
                            .load(user.getProfileImageUrl())
                            .into(profileImage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError){

            }
        });
    }
}
