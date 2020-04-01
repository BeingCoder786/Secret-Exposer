package com.mnnit.secretexposer.loginSignup;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mnnit.secretexposer.R;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class SignupActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST_CODE = 2;
    private final int CAMERA_REQUEST = 1;
    private final int CAMERA_PERMISSION_CODE = 22;
    private EditText txtName, txtEmail, txtPwd, txtrePwd;
    private Button btnsignup;
    //RadioGroup btnGenderGroup;
    private RadioButton btnGender1;
    private RadioButton btnGender2;
    int genderBtnId;
    // FirebaseDatabase database;//for realtime
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private Uri fileUri;
    private ImageView profileImage;

    @Override
    protected void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        requestWindowFeature ( Window.FEATURE_NO_TITLE );
        setContentView ( R.layout.activity_signup );
        profileImage = (ImageView ) findViewById ( R.id.profile_image );
        txtName = ( EditText ) findViewById ( R.id.name );
        txtEmail = ( EditText ) findViewById ( R.id.email );
        txtPwd = ( EditText ) findViewById ( R.id.password );
        txtrePwd = ( EditText ) findViewById ( R.id.repassword );
        btnsignup = ( Button ) findViewById ( R.id.signup );
        //btnGenderGroup=(RadioGroup) findViewById(R.id.gender_button);
        //genderBtnId=btnGenderGroup.getCheckedRadioButtonId();
        btnGender1 = ( RadioButton ) findViewById ( R.id.female );
        btnGender2 = ( RadioButton ) findViewById ( R.id.male );
        myRef = FirebaseDatabase.getInstance ( ).getReference ( "User" );//real user node for signup entry
        mAuth = FirebaseAuth.getInstance ( );
        btnsignup.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public void onClick ( View v ) {
                final String fullname = txtName.getText ( ).toString ( );
                final String email = txtEmail.getText ( ).toString ( );
                final String password = txtPwd.getText ( ).toString ( );
                final String repassword = txtrePwd.getText ( ).toString ( );
                String gender1 = "";

                //Toast.makeText(SignupActivity.this, gender,Toast.LENGTH_SHORT).show();
                if ( btnGender1.isChecked ( ) )
                    gender1 = btnGender1.getText ( ).toString ( );
                else if ( btnGender2.isChecked ( ) )
                    gender1 = btnGender2.getText ( ).toString ( );
                else {
                    Toast.makeText ( SignupActivity.this, "please checked Radio Button", Toast.LENGTH_SHORT ).show ( );
                    return;
                }
                final String gender = gender1;
                if ( TextUtils.isEmpty ( fullname ) ) {
                    Toast.makeText ( SignupActivity.this, "Fill Name Field", Toast.LENGTH_SHORT ).show ( );

                    return;
                }
                if ( TextUtils.isEmpty ( email ) ) {
                    Toast.makeText ( SignupActivity.this, "Fill Email Field", Toast.LENGTH_SHORT ).show ( );
                    return;
                }
                if ( TextUtils.isEmpty ( password ) ) {
                    Toast.makeText ( SignupActivity.this, "Fill Password Field", Toast.LENGTH_SHORT ).show ( );
                    return;
                }
                if ( TextUtils.isEmpty ( repassword ) ) {
                    Toast.makeText ( SignupActivity.this, "Fill rePassword Field", Toast.LENGTH_SHORT ).show ( );
                    return;
                }
                if ( ! password.equals ( repassword ) ) {
                    Toast.makeText ( SignupActivity.this, "Password Mismatch", Toast.LENGTH_SHORT ).show ( );
                }
                if ( gender.equals ( null ) ) {
                    Toast.makeText ( SignupActivity.this, "Gender", Toast.LENGTH_SHORT ).show ( );
                }

                final ProgressBar progressBar = findViewById ( R.id.progress_circular );
                progressBar.setVisibility ( View.VISIBLE );
                onPause ( );
                mAuth.createUserWithEmailAndPassword ( email, password )
                        .addOnCompleteListener ( SignupActivity.this, new OnCompleteListener < AuthResult > ( ) {
                            @Override
                            public void onComplete ( @NonNull Task < AuthResult > task ) {
                                if ( task.isSuccessful ( ) ) {
                                    User information;
                                    if ( fileUri != null ) {
                                        Date currTime = Calendar.getInstance ( ).getTime ( );
                                        StorageReference storageReference = FirebaseStorage
                                                .getInstance ( ).getReference ( "UserImages" )
                                                .child ( FirebaseAuth.getInstance ( )
                                                                 .getCurrentUser ( ).getUid ( ) );
                                        storageReference.child ( currTime.toString ( ) )
                                                .putFile ( fileUri )
                                                .addOnSuccessListener ( new OnSuccessListener < UploadTask.TaskSnapshot > ( ) {
                                                    @Override
                                                    public void onSuccess ( UploadTask.TaskSnapshot taskSnapshot ) {
                                                        storageReference
                                                                .child ( currTime.toString ( ) )
                                                                .getDownloadUrl ( )
                                                                .addOnSuccessListener ( new OnSuccessListener < Uri > ( ) {
                                                                    @Override
                                                                    public void onSuccess ( Uri uri ) {
                                                                        User user = new User ( fullname, email, password, gender, mAuth
                                                                                .getCurrentUser ( )
                                                                                .getUid ( ), uri.toString ( ) );
                                                                        FirebaseDatabase
                                                                                .getInstance ( )
                                                                                .getReference ( "Users" )
                                                                                .child ( FirebaseAuth
                                                                                                 .getInstance ( )
                                                                                                 .getCurrentUser ( )
                                                                                                 .getUid ( ) )
                                                                                .setValue ( user ).addOnSuccessListener ( new OnSuccessListener < Void > ( ) {
                                                                            @Override
                                                                            public void onSuccess ( Void aVoid ) {
                                                                                progressBar.setVisibility ( View.GONE );
                                                                                Toast.makeText ( SignupActivity.this, "Registration completed", Toast.LENGTH_SHORT )
                                                                                        .show ( );
                                                                                startActivity ( new Intent ( getApplicationContext ( ), LoginActivity.class ) );
                                                                                onDestroy ( );
                                                                            }
                                                                        } );
                                                                    }
                                                                } );
                                                    }
                                                } );
                                    } else {
                                        User user = new User(fullname,email,password,gender,mAuth.getCurrentUser ().getUid (),"");
                                        FirebaseDatabase.getInstance ( ).getReference ( "Users" )
                                                .child ( FirebaseAuth.getInstance ( )
                                                                 .getCurrentUser ( )
                                                                 .getUid ( ) )
                                                .setValue ( user )
                                                .addOnCompleteListener ( new OnCompleteListener < Void > ( ) {
                                                    @Override
                                                    public void onComplete ( @NonNull Task < Void > task ) {
                                                        progressBar.setVisibility ( View.GONE );
                                                        Toast.makeText ( SignupActivity.this, "Registration completed", Toast.LENGTH_SHORT )
                                                                .show ( );
                                                        startActivity ( new Intent ( getApplicationContext ( ), LoginActivity.class ) );
                                                        onDestroy ( );
                                                    }
                                                } );


                                    }
                                } else{
                                        progressBar.setVisibility ( View.GONE );
                                        Toast.makeText ( SignupActivity.this, "Registration Failed", Toast.LENGTH_SHORT )
                                                .show ( );
                                    }
                                }
                } );
            }
        } );
    }
    public void chooseImage ( View view ) {
        final CharSequence[] options={"Take Photo","Select From Gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder( SignupActivity.this);
        builder.setTitle("Add Photo !");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Take Photo")){
                    if(checkSelfPermission( Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                    }
                    else {
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA_REQUEST);
                    }
                }
                else if(options[which].equals("Select From Gallery")){
                    Intent intent=new Intent( Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult ( requestCode, resultCode, data );
        if ( resultCode == RESULT_OK && requestCode == CAMERA_REQUEST ) {
            Bitmap photo = ( Bitmap ) data.getExtras ( ).get ( "data" );
            fileUri = data.getData ( );
            profileImage.setImageBitmap ( photo );
        } else if ( resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data != null && data
                .getData ( ) != null ) {
            Uri selectedImage = data.getData ( );
            fileUri = selectedImage;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap ( getContentResolver ( ), selectedImage );
                profileImage.setVisibility ( View.VISIBLE );
                profileImage.setImageBitmap ( bitmap );
            } catch (IOException e) {
                e.printStackTrace ( );
            }
        }
    }
}

