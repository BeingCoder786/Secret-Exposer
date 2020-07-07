package com.mnnit.secretexposer.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.LoginActivity;
import com.mnnit.secretexposer.loginSignup.User;
import com.mnnit.secretexposer.profile.UpdateProfile;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private TextView email;
    private TextView user_name;
    private ImageView profileImage;
    private User user;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        loadUserInformation(navigationView.getHeaderView(0));
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_notification,
                R.id.nav_groups, R.id.nav_share, R.id.nav_about)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.post, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public  void loadUserInformation(View headerView) {
        email=(TextView) headerView.findViewById(R.id.email);
        profileImage = (ImageView) headerView.findViewById ( R.id.profile_image );
        if(email==null) {
            Toast.makeText(getBaseContext(),"Null",Toast.LENGTH_SHORT).show();
            return;
        }
        user_name=(TextView)headerView.findViewById(R.id.user_name);
        uid = FirebaseAuth.getInstance().getUid();
        DatabaseReference realRef= FirebaseDatabase.getInstance().getReference("Users/"+uid);
        realRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user=dataSnapshot.getValue(User.class);
                user.getFullname();
                user_name.setText(user.getFullname());
                email.setText(user.getEmail());
                if(!user.getProfileImageUrl().isEmpty())
                Picasso.with ( getBaseContext () )
                        .load ( user.getProfileImageUrl () )
                        .into(profileImage);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void logout(MenuItem item) {
          FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
          firebaseAuth.signOut();
          SharedPreferences sp=getSharedPreferences("login",MODE_PRIVATE);
          sp.edit().clear().apply();
          sp.edit().commit();
          finish();
          Intent intent=new Intent(getBaseContext(), LoginActivity.class);
          startActivity(intent);
    }

    public void updateProfile ( View view ) {
        Intent intent = new Intent(getBaseContext(), UpdateProfile.class);
        intent.putExtra("user",user);
        startActivity(intent);

    }
}
