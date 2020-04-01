package com.mnnit.secretexposer.post;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.mnnit.secretexposer.R;

public class ShowComment extends AppCompatActivity {
    private ImageView profileImage;
    private ImageView imagePost;
    private VideoView videoPost;
    private TextView textPost;
    private TextView userName;
    private TextView likeComment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);
        String postId = getIntent().getStringExtra("post");
        profileImage=(ImageView)findViewById(R.id.profile_image);
        imagePost=(ImageView)findViewById(R.id.image_post);
        videoPost=(VideoView)findViewById(R.id.video_post);
        textPost=(TextView)findViewById(R.id.text_post);
        userName=(TextView)findViewById(R.id.user_name);
        likeComment=(TextView)findViewById(R.id.like_comment);
        load(postId);
    }
    public void load(String postId){

    }
}
