package com.mnnit.secretexposer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class WritePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
    }
    public void sendPost(View view) {
        EditText editText=(EditText)findViewById(R.id.post_content);
        String postContent=editText.getText().toString();
        Toast.makeText(WritePostActivity.this,postContent, Toast.LENGTH_SHORT).show();
    }
}

