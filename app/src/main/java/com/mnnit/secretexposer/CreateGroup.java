package com.mnnit.secretexposer;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class CreateGroup extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        recyclerView = findViewById(R.id.group_recycleView);

    }
}
