package com.mnnit.secretexposer.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mnnit.secretexposer.R;
import com.mnnit.secretexposer.loginSignup.User;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private ImageView profileImage;
    private TextView userName;
    private TextView email;
    private User user;
    private String uid;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage=root.findViewById(R.id.profile_image);
        userName=root.findViewById(R.id.user_name);
        email=root.findViewById(R.id.email);
        uid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Users/"+uid).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ){
                user=dataSnapshot.getValue(User.class);
                if(!user.getProfileImageUrl().isEmpty())
                    Picasso.with(getContext()).load(user.getProfileImageUrl())
                            .into(profileImage);
                userName.setText(user.getFullname());
                email.setText(user.getEmail());
            }
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ){

            }
        });
        return root;
    }
}