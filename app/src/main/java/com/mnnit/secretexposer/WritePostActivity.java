package com.mnnit.secretexposer;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
public class WritePostActivity extends AppCompatActivity {
    private ImageView imageView;
    private VideoView videoView;
    private TextView textView;
    private Uri fileUri=null;
    private RadioButton anonymousButton;
    private boolean anonymous;
    private int postType=-1;
    private String id;
    private String path;
    private String postContent;
    private String owner;
    private String groupName="publicGroup";
    private Post post;
    private final int CAMERA_REQUEST=1;
    private final int CAMERA_PERMISSION_CODE=100;
    private final int PICK_IMAGE_REQUEST_CODE=2;
    private final int PICK_FILE_REQUEST_CODE=3;
    private final int PICK_VIDEO_REQUEST_CODE=4;
    private final int PICK_AUDIO_REQUEST_CODE=5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        imageView = findViewById(R.id.image_post);
        videoView = findViewById(R.id.video_post);
        textView = findViewById(R.id.file_post);
        anonymousButton = findViewById(R.id.anonymous);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void sendPost(View view){
            Toast.makeText(getBaseContext(),"sendpost",Toast.LENGTH_SHORT).show();
            Instant time=Instant.now();
            id = time.getEpochSecond()+""+FirebaseAuth.getInstance().getCurrentUser().getUid();
            if(fileUri!=null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("posts/" + groupName);
                storageReference.child(path + "/" + id).putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.child(path + "/" + id).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                EditText editText = findViewById(R.id.post_content);
                                postContent = editText.getText().toString();
                                owner = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                post = new Post(id, postContent, owner, groupName, uri.toString(), postType, anonymous);
                                if (post == null) {
                                    Toast.makeText(getBaseContext(), "post is Null", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                FirebaseDatabase.getInstance().getReference("Posts/" + groupName)
                                        .child(id).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                                    }
                                });
                            }
                        });
                    }
                });
            }
            else {
                EditText editText = findViewById(R.id.post_content);
                postContent = editText.getText().toString();
                owner = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if(anonymousButton.isActivated())
                    anonymous=true;
                else
                    anonymous=false;
                post = new Post(id, postContent, owner, groupName, "",postType,anonymous);
                if (post == null) {
                    Toast.makeText(getBaseContext(), "post is Null", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseDatabase.getInstance().getReference("Posts/publicGroup").child(id).setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                    }
                });
            }
    }
    public void chooseImage(View view) {
        final CharSequence[] options={"Take Photo","Select From Gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder(WritePostActivity.this);
        builder.setTitle("Add Photo !");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Take Photo")){
                    if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                    }
                    else {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                }
                else if(options[which].equals("Select From Gallery")){
                    Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,PICK_IMAGE_REQUEST_CODE);
                }
                else{
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERMISSION_CODE)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST);
            } else{
                Toast.makeText(this,"Camera Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void chooseDocument(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent,"Select File"),PICK_FILE_REQUEST_CODE);
    }

    public void chooseMusic(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent,"Select Music File"),PICK_FILE_REQUEST_CODE);
    }

    public void chooseVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),PICK_VIDEO_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CAMERA_REQUEST){
                Bitmap photo=(Bitmap) data.getExtras().get("data");
                path="image";
                postType = PICK_IMAGE_REQUEST_CODE;
                fileUri=data.getData();
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(photo);
        } else if(resultCode==RESULT_OK && requestCode==PICK_IMAGE_REQUEST_CODE && data!=null && data.getData()!=null){
                Uri selectedImage= data.getData();
                path="image";
                postType = PICK_IMAGE_REQUEST_CODE;
                fileUri = selectedImage;
                try{
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),
                            selectedImage);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
        } else if (resultCode == RESULT_OK && requestCode == PICK_FILE_REQUEST_CODE && data!=null && data.getData()!=null) {
            Uri uri=data.getData();
            path="files";
            File file=new File(uri.toString());
            fileUri=uri;
            postType=PICK_FILE_REQUEST_CODE;
            textView.setText(file.getAbsolutePath());
            textView.setBackgroundColor(16777172);
            textView.setVisibility(View.VISIBLE);

        } else if(resultCode == RESULT_OK && requestCode == PICK_AUDIO_REQUEST_CODE && data!=null && data.getData()!=null){
            Uri uri=data.getData();
            path="audio";
            postType=PICK_AUDIO_REQUEST_CODE;
            File file=new File(uri.toString());
            fileUri=uri;
            textView.setText(file.getName());
            textView.setBackgroundColor(16777172);
            textView.setVisibility(View.VISIBLE);
        } else if(resultCode == RESULT_OK && requestCode == PICK_VIDEO_REQUEST_CODE && data!=null && data.getData()!=null){
            if(data==null||data.getData()==null)
                return;

            path="videos";
            postType=PICK_VIDEO_REQUEST_CODE;
            Uri selectedVideo = data.getData();
            fileUri=selectedVideo;
            videoView.setVideoURI(selectedVideo);
            videoView.seekTo(1);
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        }
    }
}

