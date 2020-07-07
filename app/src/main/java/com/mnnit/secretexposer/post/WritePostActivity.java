package com.mnnit.secretexposer.post;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.group.Group;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.loginSignup.User;
import com.mnnit.secretexposer.ui.notification.Notification;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WritePostActivity extends AppCompatActivity{
    private User user;
    private Group group;
    private String userName;
    private ImageView imageView;
    private VideoView videoView;
    private TextView textView;
    private ProgressBar progressBar;
    private Uri fileUri = null;
    private CheckBox anonymousBox;
    private boolean anonymous;
    private int postType = - 1;
    private String id;
    private String path;
    private String postContent;
    private String owner;
    private String groupName;
    private Post post;
    private long counter;
    private final int CAMERA_REQUEST = 1;
    private final int CAMERA_PERMISSION_CODE = 100;
    private final int PICK_IMAGE_REQUEST_CODE = 2;
    private final int PICK_FILE_REQUEST_CODE = 3;
    private final int PICK_VIDEO_REQUEST_CODE = 4;
    private final int PICK_AUDIO_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        imageView = findViewById(R.id.image_post);
        videoView = findViewById(R.id.video_post);
        textView = findViewById(R.id.file_post);
        anonymousBox = findViewById(R.id.anonymous);
        progressBar = findViewById(R.id.progress_bar);
        groupName = getIntent().getStringExtra("GroupName");
        group = (Group) getIntent().getExtras().get("group");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendPost(View view) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseDatabase.getInstance().getReference("Posts/publicGroup").addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                counter=-1*dataSnapshot.getChildrenCount();
            }
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ){

            }
        });
        Instant time = Instant.now();
        id = time.getEpochSecond() + "" + FirebaseAuth.getInstance().getCurrentUser().getUid();
        if(fileUri != null){
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference("posts/" + groupName);
            storageReference.child(path + "/" + id).putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener< UploadTask.TaskSnapshot >(){
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.child(path + "/" + id).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener< Uri >(){
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            EditText editText = findViewById(R.id.post_content);
                                            postContent = editText.getText().toString();
                                            if(anonymousBox.isChecked()){
                                                Toast.makeText(getBaseContext(),"Checked",Toast.LENGTH_SHORT).show();
                                                anonymous = true;
                                            }
                                            else
                                                anonymous = false;

                                            owner = FirebaseAuth.getInstance().getCurrentUser()
                                                    .getUid();
                                            post = new Post(id, postContent, owner, groupName, uri
                                                    .toString(), postType, anonymous);
                                            post.setCounter(counter);
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Posts/" + groupName)
                                                    .child(id).setValue(post)
                                                    .addOnSuccessListener(new OnSuccessListener< Void >(){
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            progressBar.setVisibility(View.GONE);
                                                            notifyMembers();
                                                            startActivity(new Intent(getBaseContext(), HomeActivity.class));
                                                        }
                                                    });
                                        }
                                    });
                        }
                    });
        } else {
            EditText editText = findViewById(R.id.post_content);
            postContent = editText.getText().toString();
            owner = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(anonymousBox.isChecked()){
                anonymous = true;
            }
            else
                anonymous = false;
            post = new Post(id, postContent, owner, groupName, "", postType, anonymous);
            post.setCounter(counter);
            FirebaseDatabase.getInstance().getReference("Posts/publicGroup").child(id)
                    .setValue(post).addOnSuccessListener(new OnSuccessListener< Void >(){
                @Override
                public void onSuccess(Void aVoid) {
                    progressBar.setVisibility(View.GONE);
                    notifyMembers();
                    startActivity(new Intent(getBaseContext(), HomeActivity.class));
                }
            });
        }
    }

    public void chooseImage(View view) {
        final CharSequence[] options = {"Take Photo", "Select From Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(WritePostActivity.this);
        builder.setTitle("Add Photo !");
        builder.setItems(options, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Take Photo")){
                    if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    } else {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                } else if(options[which].equals("Select From Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE);
                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void chooseDocument(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), PICK_FILE_REQUEST_CODE);
    }

    public void chooseMusic(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select Music File"), PICK_FILE_REQUEST_CODE);
    }

    public void chooseVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == CAMERA_REQUEST){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            path = "image";
            postType = PICK_IMAGE_REQUEST_CODE;
            fileUri = data.getData();
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(photo);
        } else if(resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data != null && data
                .getData() != null){
            Uri selectedImage = data.getData();
            //File file = ReduceImageSize.getCompressedImageFile(new File(selectedImage.getPath()),getBaseContext() );
            //if(file==null)
            //  return;
            fileUri = selectedImage;//Uri.fromFile(file);
            path = "image";
            postType = PICK_IMAGE_REQUEST_CODE;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
                                                                  fileUri);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            } catch(IOException e) {
                e.printStackTrace();
            }
        } else if(resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE && data != null && data
                .getData() != null){
            Uri uri = data.getData();
            path = "files";
            File file = new File(uri.toString());
            fileUri = uri;
            postType = PICK_FILE_REQUEST_CODE;
            textView.setText(file.getAbsolutePath());
            textView.setBackgroundColor(16777172);
            textView.setVisibility(View.VISIBLE);

        } else if(resultCode == RESULT_OK && requestCode == PICK_AUDIO_REQUEST_CODE && data != null && data
                .getData() != null){
            Uri uri = data.getData();
            path = "audio";
            postType = PICK_AUDIO_REQUEST_CODE;
            File file = new File(uri.toString());
            fileUri = uri;
            textView.setText(file.getName());
            textView.setBackgroundColor(16777172);
            textView.setVisibility(View.VISIBLE);
        } else if(resultCode == RESULT_OK && requestCode == PICK_VIDEO_REQUEST_CODE && data != null && data
                .getData() != null){
            if(data == null || data.getData() == null)
                return;

            path = "videos";
            postType = PICK_VIDEO_REQUEST_CODE;
            Uri selectedVideo = data.getData();
            fileUri = selectedVideo;
            videoView.setVideoURI(selectedVideo);
            videoView.seekTo(1);
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        }
    }
    private void notifyMembers(){
        userName="";
        if(group==null)
            return;
        if(anonymous){
            userName = "Someone";
        }
        FirebaseDatabase.getInstance().getReference("Users")
                .child(owner).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                user=dataSnapshot.getValue(User.class);
                if(userName.isEmpty())
                userName=user.getFullname();
                String notificationContent =user.getFullname()+" posted in "+groupName;
                String time= Calendar.getInstance().getTime().toString();
                Notification notification=new Notification(notificationContent,time,user.getProfileImageUrl());
                HashMap<String,String> members=group.getMembers();
                DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("Users");
                String nid=System.currentTimeMillis()+owner;
                for (Map.Entry<String,String> entry:members.entrySet()){
                    if(entry.getKey().equals(owner))continue;
                    dbReference.child(entry.getKey())
                            .child("Notification")
                            .child(nid).setValue(notification);
                }
            }
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ){

            }
        });
    }
}

