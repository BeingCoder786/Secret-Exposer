package com.mnnit.secretexposer.init;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.loginSignup.LoginActivity;

public class InitActivity extends AppCompatActivity{

    private SharedPreferences sp;
    private String uid;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        sp=getSharedPreferences("login",MODE_PRIVATE);
        if(sp.getBoolean("logged",false)){
            String email=sp.getString("email","");
            String password=sp.getString("password","");
            mAuth=FirebaseAuth.getInstance();
            login(email,password);
        }
        else{
            startLoginActivity();
        }
    }
    private void login(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(InitActivity.this, new OnCompleteListener <AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task <AuthResult> task) {
                        if (task.isSuccessful()) {
                            //progressBar.setVisibility(View.GONE);
                            uid= mAuth.getCurrentUser().getUid();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            intent.putExtra("uid",uid);
                            Toast.makeText(InitActivity.this, "login Succesfully", Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        } else {
                            sp.edit().clear().apply();
                            sp.edit().commit();
                            startLoginActivity();
                        }
                    }
                });

    }
    private void startLoginActivity(){
        Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(intent);
    }

}
