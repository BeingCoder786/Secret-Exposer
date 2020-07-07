package com.mnnit.secretexposer.group;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.home.HomeActivity;
import com.mnnit.secretexposer.loginSignup.User;
import com.mnnit.secretexposer.ui.notification.Notification;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class UpdateGroup extends AppCompatActivity{
    private Group group;
    private User currUser;
    private ImageView groupIcon;
    private TextView groupName;
    private TextView groupDetail;
    private TextView searchTextView;
    private EditText searchEditText;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String lastUser="";
    private String lastUserId="";
    private int totalUser;
    private int lastVisibleUser;
    private CreateGroupAdapter userAdapter;
    boolean isLoading = false;
    private int MAX_USER_COUNT=20;
    private ArrayList< User > allUsers;
    private boolean isMaxUser= false;
    private Uri fileUri;
    private String url;
    private HashMap<String,String> oldList;
    private String path;
    private final int PICK_IMAGE_REQUEST_CODE = 2;
    private final int CAMERA_PERMISSION_CODE = 22;
    private final int CAMERA_REQUEST = 1;
    @Override
    protected void onCreate( Bundle savedInstanceState ){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_group);
        group= (Group) getIntent().getExtras().get("group");
        oldList=group.getMembers();
        groupIcon= findViewById(R.id.group_image);
        groupName= findViewById(R.id.group_name);
        searchTextView = findViewById(R.id.search_text_view);
        searchEditText = findViewById(R.id.search_edit_text);
        recyclerView = findViewById(R.id.group_recycleView);
        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL);
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
            public void onScrolled( @NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalUser = layoutManager.getItemCount();
                lastVisibleUser = layoutManager.findLastCompletelyVisibleItemPosition();
                if(!isLoading && totalUser <= lastVisibleUser+MAX_USER_COUNT){
                    getUser();
                    isLoading = true;
                }
            }
        });
        setGroup();
        recyclerView.setLayoutManager(layoutManager);
        allUsers = new ArrayList <>();
        userAdapter = new CreateGroupAdapter(this,new ArrayList<>());
        userAdapter.setSelectedUser(group.getMembers());

        getLastKeyFromFirebase();
        getUser();
        recyclerView.setAdapter(userAdapter);
    }
    public void setGroup(){
        Picasso.with(getBaseContext())
                .load(group.getGroupImageUri())
                .into(groupIcon);
        url=group.getGroupImageUri();
        groupName.setText(group.getGroupName());
    }
    @Override
    public boolean onCreateOptionsMenu( Menu menu) {
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
                        ArrayList<User> newUsers = new ArrayList<User>();
                        for( DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            User user = userSnapshot.getValue(User.class);
                            newUsers.add(user);
                            if(user.getKey().equals(group.getGroupOwner()))
                                currUser=user;
                        }
                        if(newUsers.size()>0)
                            lastUser = newUsers.get(newUsers.size()-1).getKey();
                        if(!lastUser.equals(lastUserId))
                            newUsers.remove(newUsers.size()-1);
                        else
                            lastUser = "end";
                        userAdapter.addAll(newUsers);
                        allUsers.addAll(newUsers);
                        //users = userAdapter.getUserList();
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
    public void saveGroup( MenuItem item){
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("GroupIcons");
        if(fileUri!=null){
            storageReference.child(group.getGroupName()).putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener < UploadTask.TaskSnapshot >(){
                        @Override
                        public void onSuccess( UploadTask.TaskSnapshot taskSnapshot ){
                            storageReference.child(group.getGroupName()).getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener < Uri >(){
                                        @Override
                                        public void onSuccess( Uri uri ){
                                            fileUri = uri;
                                            url = fileUri.toString();
                                        }
                                    });
                            updateMembers();
                        }
                    });
        }
        else{
            updateMembers();
        }
    }
    private void updateMembers(){
        HashMap < String, String > updatedMembers = userAdapter
                .getSelectedUser();
        group.setMembers(updatedMembers);
        group.setGroupImageUri(url);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("Groups");
        databaseReference.child(group.getGroupName()).setValue(group)
                .addOnSuccessListener(new OnSuccessListener < Void >(){
                    @Override
                    public void onSuccess( Void aVoid ){
                        DatabaseReference dbReference = FirebaseDatabase
                                .getInstance().getReference("Users");
                        String text = currUser.getFullname()+" removed from "+group.getGroupName();
                        String time = Calendar.getInstance().getTime().toString();
                        String id = currUser.getKey()+System.currentTimeMillis();
                        Notification notification=new Notification(text,time,currUser.getProfileImageUrl());
                        for (Map.Entry < String, String > entry : oldList
                                .entrySet()) {
                            if(! updatedMembers.containsKey(entry.getKey())){
                                dbReference.child(entry.getKey())
                                        .child("JoinedGroup")
                                        .child(group.getGroupName())
                                        .removeValue();
                                dbReference.child(entry.getKey())
                                        .child("Notification")
                                        .child(id)
                                        .setValue(notification);
                            }
                        }
                        text = currUser.getFullname()+" added to "+group.getGroupName();
                        notification=new Notification(text,time,currUser.getProfileImageUrl());
                        for (Map.Entry < String, String > user : updatedMembers
                                .entrySet()) {
                            if(! oldList.containsKey(user.getKey())){
                                dbReference.child(user.getKey())
                                        .child("JoinedGroup")
                                        .child(group.getGroupName())
                                        .setValue("true");
                                dbReference.child(user.getKey())
                                        .child("Notification")
                                        .child(id)
                                        .setValue(notification);
                            }
                        }
                        startActivity(new Intent(getBaseContext(), HomeActivity.class));
                    }
                });
    }
    public void selectImage( View view) {
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
            groupIcon.setImageBitmap(photo);
        } else if(resultCode==RESULT_OK && requestCode==PICK_IMAGE_REQUEST_CODE && data!=null && data.getData()!=null){
            Uri selectedImage= data.getData();
            path="image";
            fileUri = selectedImage;
            try{
                Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),
                                                                selectedImage);
                groupIcon.setImageBitmap(bitmap);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void searchUser(View view){
        searchTextView.setVisibility(View.GONE);
        searchEditText.setVisibility(View.VISIBLE);
        searchEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after){

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count){

            }

            @Override
            public void afterTextChanged( Editable s){
                String searchUserName = searchEditText.getText().toString();
                if(searchUserName.length()==0&&userAdapter.getUserList().size()!=allUsers.size()){
                    userAdapter.clearAll();
                    userAdapter.addAll(allUsers);
                }
                ArrayList<User> updatedList = new ArrayList<>();
                for(User user : allUsers){
                    String  userName= user.getFullname();
                    if(matchUserName(searchUserName,userName))
                        updatedList.add(user);
                }
                userAdapter.clearAll();
                userAdapter.addAll(updatedList);
            }
        });
    }
    private boolean matchUserName(String searchUserName,String userName){
        userName = userName.toLowerCase();
        StringTokenizer stringTokenizer=new StringTokenizer(userName, " ");
        searchUserName=searchUserName.toLowerCase();
        while(stringTokenizer.hasMoreTokens()){
            String token=stringTokenizer.nextToken();
            boolean flag=true;
            if(searchUserName.length()>token.length())
                continue;
            for(int j=0;j<searchUserName.length();j++){
                if(token.charAt(j)!=searchUserName.charAt(j)){
                    flag=false;
                    break;
                }
            }
            if(flag)return flag;
        }
        return false;
    }

}
