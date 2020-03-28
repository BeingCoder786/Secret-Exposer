package com.mnnit.secretexposer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {
    EditText txtEmail,txtPwd;
    Button btnlogin;
    TextView forgot;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        forgot=(TextView)findViewById(R.id.forgot);
        txtEmail=(EditText)findViewById(R.id.uid);
        txtPwd=(EditText)findViewById(R.id.password);
        btnlogin=(Button) findViewById(R.id.signin);
        mAuth= FirebaseAuth.getInstance();
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email=txtEmail.getText().toString();
                final String password=txtPwd.getText().toString();
                if(TextUtils.isEmpty(email))
                {
                    Toast.makeText(LoginActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
                if(TextUtils.isEmpty(password))
                {
                    Toast.makeText(LoginActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
                //now login begin
                progressBar=findViewById(R.id.progress_circular);
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {   //you have to privide class name
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.GONE);

                                Toast.makeText(LoginActivity.this, "login Succesfully", Toast.LENGTH_SHORT).show();
                               startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        ///recover password through email
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordialog();

            }
        });
    }
    private void showRecoverPasswordialog() {
        //alert
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Recover password");
        //set linear layout
        LinearLayout linearLayout=new LinearLayout(this);
        //view to set in dialog
        final EditText emailit=new EditText(this);
        emailit.setHint("enter email here");

        emailit.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailit.setMinEms(20);//fit to screen for entering email
        linearLayout.addView(emailit);
        linearLayout.setPadding(10,10,10,10);
        builder.setView(linearLayout);
        //button recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email=emailit.getText().toString().trim();
                beginrecoveryemail(email);
            }
        });
        builder.create().show();
    }
    private void beginrecoveryemail(String email) {

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "Check your Email", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override

            public void onFailure(@NonNull Exception e) {

            }
        });
    }
    //this following function for signup page
        public void changeToSignupActivity(View view) {
            Intent intent=new Intent(this, SignupActivity.class);
            startActivity(intent);
        }
    }

