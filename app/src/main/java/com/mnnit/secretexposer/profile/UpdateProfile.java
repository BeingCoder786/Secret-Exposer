package com.mnnit.secretexposer.profile;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class UpdateProfile extends AppCompatActivity{
    private ImageView profileImage;
    private EditText aboutUser;
    private EditText userName;
    private EditText email;
    private Uri fileUri;
    private final int CAMERA_REQUEST = 1;
    private final int CAMERA_PERMISSION_CODE = 100;
    private final int PICK_IMAGE_REQUEST_CODE = 2;
    private String path;
    private String uid;
    private User user;
    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        profileImage = findViewById(R.id.profile_image);
        aboutUser = findViewById(R.id.aboutUser);
        userName = findViewById(R.id.user_name);
        email = findViewById(R.id.email);
        user = (User) getIntent().getExtras().get("user");
        email.setText(user.getEmail());
        userName.setText(user.getFullname());
        if(!user.getProfileImageUrl().isEmpty())
        Picasso.with(UpdateProfile.this)
                .load(user.getProfileImageUrl())
                .into(profileImage);
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ){
        getMenuInflater().inflate(R.menu.update_profile, menu);
        return true;
    }

    public void updateProfile( MenuItem item ){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Date currTime = Calendar.getInstance().getTime();
        user.setAboutUser(aboutUser.getText().toString());
        FirebaseStorage.getInstance().getReference("UserImages")
                .child(uid)
                .child(currTime.toString()).putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener< UploadTask.TaskSnapshot >(){
                    @Override
                    public void onSuccess( UploadTask.TaskSnapshot taskSnapshot ){
                        FirebaseStorage.getInstance().getReference("UserImages").child(uid)
                                .child(currTime.toString()).getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener< Uri >(){
                                    @Override
                                    public void onSuccess( Uri uri ){
                                        user.setProfileImageUrl(uri.toString());
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(uid).setValue(user).addOnSuccessListener(new OnSuccessListener< Void >(){
                                            @Override
                                            public void onSuccess( Void aVoid ){
                                                Intent intent=new Intent(UpdateProfile.this,HomeActivity.class);
                                                intent.putExtra("user",user);
                                                startActivity(intent);
                                            }
                                        });
                                    }
                                });
                    }
                });
    }
public void selectImage(View view){
final CharSequence[]options={"Take Photo","Select From Gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder(UpdateProfile.this);
        builder.setTitle("Add Photo !");
        builder.setItems(options,new DialogInterface.OnClickListener(){
@Override
public void onClick(DialogInterface dialog,int which){
        if(options[which].equals("Take Photo")){
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
        requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
        }else{
        Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);
        }
        }else if(options[which].equals("Select From Gallery")){
        Intent intent=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent,PICK_IMAGE_REQUEST_CODE);
        }else{
        dialog.dismiss();
        }
        }
        });
        builder.show();

        }

@Override
public void onRequestPermissionsResult(int requestCode,@NonNull String[]permissions,@NonNull int[]grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==CAMERA_PERMISSION_CODE){
        if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_REQUEST);
        }else{
        Toast.makeText(this,"Camera Permission Denied",Toast.LENGTH_SHORT).show();
        }
        }
        }

@Override
protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode==RESULT_OK&&requestCode==CAMERA_REQUEST){
        Bitmap photo=(Bitmap)data.getExtras().get("data");
        fileUri=data.getData();
        profileImage.setVisibility(View.VISIBLE);
        profileImage.setImageBitmap(photo);
        }else if(resultCode==RESULT_OK&&requestCode==PICK_IMAGE_REQUEST_CODE&&data!=null&&data
        .getData()!=null){
        Uri selectedImage=data.getData();
        //File file = ReduceImageSize.getCompressedImageFile(new File(selectedImage.getPath()),getBaseContext() );
        //if(file==null)
        //  return;
        fileUri=selectedImage;//Uri.fromFile(file);
        try{
        Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),
        fileUri);
        profileImage.setImageBitmap(bitmap);
        }catch(IOException e){
        e.printStackTrace();
        }
        }
        }
public File saveBitmapToFile(File file){
        try{
        // BitmapFactory options to downsize the image
        BitmapFactory.Options o=new BitmapFactory.Options();
        o.inJustDecodeBounds=true;
        o.inSampleSize=6;
        // factor of downsizing the image

        FileInputStream inputStream=new FileInputStream(file);
        //Bitmap selectedBitmap = null;
        BitmapFactory.decodeStream(inputStream,null,o);
        inputStream.close();

// The new size we want to scale to
final int REQUIRED_SIZE=75;

        // Find the correct scale value. It should be the power of 2.
        int scale=1;
        while(o.outWidth/scale/2>=REQUIRED_SIZE&&
        o.outHeight/scale/2>=REQUIRED_SIZE){
        scale*=2;
        }

        BitmapFactory.Options o2=new BitmapFactory.Options();
        o2.inSampleSize=scale;
        inputStream=new FileInputStream(file);

        Bitmap selectedBitmap=BitmapFactory.decodeStream(inputStream,null,o2);
        inputStream.close();

        // here i override the original image file
        file.createNewFile();
        FileOutputStream outputStream=new FileOutputStream(file);

        selectedBitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);

        return file;
        }catch(Exception e){
        return null;
        }
        }
        }
