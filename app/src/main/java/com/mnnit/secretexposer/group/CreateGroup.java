package com.mnnit.secretexposer.group;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
public class CreateGroup extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText groupNameContainer;
    private ImageView profileImageContainer;
    private ArrayList<User> selectedUser;
    final int MAX_USER_COUNT=20;
    private String lastUserId = "";
    private String lastUser = "";
    private CreateGroupAdapter userAdapter;
    private boolean isLoading = false;
    private boolean isRefreshing = false;
    private int totalUser =0, lastVisibleUser = 0;
    private boolean isMaxUser;
    private String path;
    private Uri fileUri;
    private final int CAMERA_PERMISSION_CODE = 22;
    private final int CAMERA_REQUEST = 1;
    private final int PICK_IMAGE_REQUEST_CODE = 2;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        groupNameContainer = (EditText) findViewById(R.id.group_name);
        profileImageContainer = (ImageView) findViewById(R.id.group_image);
        recyclerView = findViewById(R.id.group_recycleView);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,DividerItemDecoration.HORIZONTAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_container);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(swipeRefreshLayout.isRefreshing()==false)
                    swipeRefreshLayout.setRefreshing(true);
                totalUser=0;
                lastVisibleUser=0;
                lastUser="";
                lastUserId="";
                getLastKeyFromFirebase();
                userAdapter.clearAll();
                getUser();
                if(swipeRefreshLayout.isRefreshing()==true)
                    swipeRefreshLayout.setRefreshing(false);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalUser = layoutManager.getItemCount();
                lastVisibleUser = layoutManager.findLastCompletelyVisibleItemPosition();
                if(!isLoading && totalUser <= lastVisibleUser+MAX_USER_COUNT){
                    getUser();
                    isLoading = true;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        userAdapter = new CreateGroupAdapter(this,new ArrayList<User>());
        getLastKeyFromFirebase();
        getUser();
        recyclerView.setAdapter(userAdapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save,menu);
        return true;
    }

    private void getLastKeyFromFirebase() {
        Query getLastKey = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .orderByKey()
                .limitToLast(1);
        getLastKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot lastNode : dataSnapshot.getChildren())
                    lastUserId = lastNode.getKey();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void getUser() {
        if(!isMaxUser){
            Query query;
            if(TextUtils.isEmpty(lastUser)){
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .orderByKey()
                        .limitToFirst(MAX_USER_COUNT);
            } else {
                query = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .orderByKey()
                        .startAt(lastUser)
                        .limitToFirst(MAX_USER_COUNT);
            }
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChildren()){
                        ArrayList<User> users = new ArrayList<User>();
                        for( DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            users.add(user);
                        }
                        if(users.size()>0)
                        lastUser = users.get(users.size()-1).getKey();
                        if(!lastUser.equals(lastUserId))
                            users.remove(users.size()-1);
                        else
                            lastUser = "end";
                        userAdapter.addAll(users);
                        isLoading = false;
                    } else {
                        isLoading = false;
                        isMaxUser = true;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    isLoading = false;
                }
            });
        }
    }
    public void saveGroup(MenuItem item){
        String groupName = groupNameContainer.getText().toString();
        if(groupName==null){
            Toast.makeText(getBaseContext(),"Name is Required", Toast.LENGTH_SHORT).show();
            return;
        }
        String owner = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentTime = new SimpleDateFormat("HH:mm:ss DD-MM-YYYY", Locale.getDefault()).format(new Date());
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("GroupIcons");
        storageReference.child(groupName).putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.child(groupName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        fileUri =uri;
                    }
                });
                HashMap<String,String> members = userAdapter.getSelectedUser();
                Group group = new Group(groupName,owner,fileUri.toString(),currentTime,members);
                if(members.size()>0)
                    Toast.makeText(getBaseContext(),"Members"+members.size(),Toast.LENGTH_SHORT).show();
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("Groups");
                if(members.size()>0)
                databaseReference.child(groupName).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                    }
                });
            }
        });
    }
    public void selectImage(View view) {
        final CharSequence[] options={"Take Photo","Select From Gallery","Cancel"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Add Photo !");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(options[which].equals("Take Photo")){
                    if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
                    }
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, CAMERA_REQUEST);
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
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST);
            } else {
                Toast.makeText(this,"Camera Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK && requestCode== CAMERA_REQUEST){
            Bitmap photo=(Bitmap) data.getExtras().get("data");
            path="image";
            fileUri=data.getData();
            profileImageContainer.setImageBitmap(photo);
        } else if(resultCode==RESULT_OK && requestCode==PICK_IMAGE_REQUEST_CODE && data!=null && data.getData()!=null){
            Uri selectedImage= data.getData();
            path="image";
            fileUri = selectedImage;
            try{
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),
                        selectedImage);
                profileImageContainer.setImageBitmap(bitmap);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
