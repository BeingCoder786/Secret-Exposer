package com.mnnit.secretexposer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    EditText txtName,txtEmail,txtPwd,txtrePwd;
    Button btnsignup;
    //RadioGroup btnGenderGroup;
    RadioButton btnGender1;
    RadioButton btnGender2;
    int genderBtnId;
    // FirebaseDatabase database;//for realtime
    DatabaseReference myRef;
    FirebaseAuth  mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signup);
        txtName=(EditText)findViewById(R.id.name);
        txtEmail=(EditText)findViewById(R.id.email);
        txtPwd=(EditText)findViewById(R.id.password);
        txtrePwd=(EditText)findViewById(R.id.repassword);
        btnsignup=(Button) findViewById(R.id.signup);
        //btnGenderGroup=(RadioGroup) findViewById(R.id.gender_button);
        //genderBtnId=btnGenderGroup.getCheckedRadioButtonId();
        btnGender1=(RadioButton)findViewById(R.id.female);
        btnGender2=(RadioButton)findViewById(R.id.male);
        myRef= FirebaseDatabase.getInstance().getReference("User");//real user node for signup entry
        mAuth= FirebaseAuth.getInstance();
        btnsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullname=txtName.getText().toString();
                final String email=txtEmail.getText().toString();
                final String password=txtPwd.getText().toString();
                final String repassword=txtrePwd.getText().toString();
                String gender1="";

                //Toast.makeText(SignupActivity.this, gender,Toast.LENGTH_SHORT).show();
                if(btnGender1.isChecked())
                    gender1=btnGender1.getText().toString();
                else if(btnGender2.isChecked())
                    gender1=btnGender2.getText().toString();
                else
                {
                    Toast.makeText(SignupActivity.this, "please checked Radio Button", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String gender=gender1;
                if(TextUtils.isEmpty(fullname))
                {
                    Toast.makeText(SignupActivity.this, "Fill Name Field", Toast.LENGTH_SHORT).show();

                    return;
                }
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(SignupActivity.this, "Fill Email Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(SignupActivity.this, "Fill Password Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(repassword))
                {
                    Toast.makeText(SignupActivity.this, "Fill rePassword Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!password.equals(repassword))
                {
                    Toast.makeText(SignupActivity.this, "Password Mismatch", Toast.LENGTH_SHORT).show();
                }
                if(gender.equals(null))
                {
                    Toast.makeText(SignupActivity.this, "Gender", Toast.LENGTH_SHORT).show();
                }

/*                final Handler handel=new Handler()
                handel.post(new Runnable() {
                    @Override
                    public void run() {
                        ProgressBar progressBar= findViewById(R.id.progress_circular);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                });*/
                final ProgressBar progressBar=findViewById(R.id.progress_circular);
                progressBar.setVisibility(View.VISIBLE);
                onPause();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    com.mnnit.secretexposer.User information=new com.mnnit.secretexposer.User(fullname,email,password,repassword,gender);

                                    FirebaseDatabase.getInstance().getReference("Users")
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(SignupActivity.this, "Registration completed", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                            onDestroy();
                                        }
                                    });


                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                }

                                // ...
                            }
                        });
            }
        });
    }
}
