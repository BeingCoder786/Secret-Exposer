package com.mnnit.secretexposer.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.SignupActivity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

    }
    public void changeToSignupActivity(View view) {
        Intent intent=new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
}
